package io.github.brainage04.maidex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.CatalogInfo
import io.github.brainage04.maidex.data.CatalogRepository
import io.github.brainage04.maidex.data.CatalogUpdateScheduler
import io.github.brainage04.maidex.data.ChartFilters
import io.github.brainage04.maidex.data.CircleDailySnapshot
import io.github.brainage04.maidex.data.CircleData
import io.github.brainage04.maidex.data.ChartSort
import io.github.brainage04.maidex.data.ConstantAvailability
import io.github.brainage04.maidex.data.SortOrder
import io.github.brainage04.maidex.data.FilterPreset
import io.github.brainage04.maidex.data.FilterOptions
import io.github.brainage04.maidex.data.SongChart
import io.github.brainage04.maidex.data.PlayDetail
import io.github.brainage04.maidex.data.PlayerProfile
import io.github.brainage04.maidex.data.PlayCountSnapshot
import io.github.brainage04.maidex.data.UserDataRepository
import io.github.brainage04.maidex.data.TrackingSettings
import io.github.brainage04.maidex.data.UserScore
import io.github.brainage04.maidex.network.MaimaiDxClient
import io.github.brainage04.maidex.rating.RatingCalculator
import io.github.brainage04.maidex.data.normalizeSearch
import io.github.brainage04.maidex.tracking.CircleTrackingScheduler
import io.github.brainage04.maidex.data.builtInFilterPresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CatalogRepository(application)
    private val userRepository = UserDataRepository(application)
    private val dxClient by lazy(LazyThreadSafetyMode.NONE) { MaimaiDxClient() }
    private val snapshot = MutableStateFlow<LoadedCatalog?>(null)
    private val filters = MutableStateFlow(ChartFilters())
    private val sorting = MutableStateFlow(Sorting())
    private val error = MutableStateFlow<String?>(null)
    private val scores = MutableStateFlow<Map<String, UserScore>>(emptyMap())
    private val playDetails = MutableStateFlow<Map<String, PlayDetail>>(emptyMap())
    private val profile = MutableStateFlow<PlayerProfile?>(null)
    private val importStatus = MutableStateFlow<ImportStatus>(ImportStatus.Idle)
    private val circleHistory = MutableStateFlow<List<CircleData>>(emptyList())
    private val circleSnapshots = MutableStateFlow<List<CircleDailySnapshot>>(emptyList())
    private val playCountSnapshots = MutableStateFlow<List<PlayCountSnapshot>>(emptyList())
    private val trackingSettings = MutableStateFlow(TrackingSettings())
    private val customFilterPresets = MutableStateFlow<List<FilterPreset>>(emptyList())

    private val trackingData = combine(
        circleHistory,
        circleSnapshots,
        playCountSnapshots,
        trackingSettings,
    ) { history, circlePoints, playCounts, settings ->
        TrackingData(history, circlePoints, playCounts, settings)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TrackingData())

    private val playerData = combine(scores, playDetails, profile, importStatus, trackingData) {
            currentScores,
            currentDetails,
            currentProfile,
            currentImportStatus,
            currentTracking,
        ->
        PlayerData(
            currentScores,
            currentDetails,
            currentProfile,
            currentImportStatus,
            currentTracking,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlayerData())

    private var ratingCharts: List<SongChart>? = null
    private var ratingScores: Map<String, UserScore>? = null
    private var cachedRating = 0

    private fun calculatedRating(
        charts: List<SongChart>,
        currentScores: Map<String, UserScore>,
        newVersions: Set<String>,
    ): Int {
        if (ratingCharts !== charts || ratingScores !== currentScores) {
            cachedRating = RatingCalculator.totalRating(charts, currentScores, newVersions)
            ratingCharts = charts
            ratingScores = currentScores
        }
        return cachedRating
    }

    val filterPresets: StateFlow<List<FilterPreset>> = combine(snapshot, customFilterPresets) {
            loaded,
            custom,
        ->
        builtInFilterPresets(loaded?.options?.versions?.firstOrNull()) + custom
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = builtInFilterPresets(null),
    )

    val uiState: StateFlow<CatalogUiState> = combine(snapshot, filters, sorting, error, playerData) {
            loaded,
            activeFilters,
            activeSorting,
            failure,
            player,
        ->
        if (loaded == null) {
            CatalogUiState(isLoading = failure == null, error = failure)
        } else {
            val visible = filterAndSort(
                loaded.charts,
                activeFilters,
                activeSorting.field,
                activeSorting.order,
                player.scores,
            )
            val newVersions = loaded.newVersions
            CatalogUiState(
                charts = visible,
                allCharts = loaded.charts,
                filters = activeFilters,
                sort = activeSorting.field,
                sortOrder = activeSorting.order,
                options = loaded.options,
                info = loaded.info,
                visibleSongCount = visible.asSequence().map { it.sourceSongId }.distinct().count(),
                scores = player.scores,
                playDetails = player.playDetails,
                profile = player.profile,
                calculatedRating = calculatedRating(loaded.charts, player.scores, newVersions),
                newVersions = newVersions,
                importStatus = player.importStatus,
                circleHistory = player.tracking.circleHistory,
                circleSnapshots = player.tracking.circleSnapshots,
                playCountSnapshots = player.tracking.playCountSnapshots,
                trackingSettings = player.tracking.settings,
                error = failure,
            )
        }
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val loaded = repository.load()
                    LoadedCatalog(
                        charts = loaded.charts,
                        options = loaded.options,
                        info = loaded.info,
                        newVersions = loaded.options.versions.take(2).toSet(),
                    )
                }
            }.onSuccess { loaded ->
                snapshot.value = loaded
                CatalogUpdateScheduler.schedule(application)
            }
                .onFailure { failure -> error.value = failure.message ?: "Unable to load catalog" }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                scores.value = userRepository.loadScores()
                playDetails.value = userRepository.loadPlayDetails()
                profile.value = userRepository.loadProfile()
                customFilterPresets.value = userRepository.loadFilterPresets()
            }
            reloadTrackingData()
            if (profile.value != null) {
                CircleTrackingScheduler.schedule(application)
            }
        }
    }

    fun setSearch(value: String) {
        filters.value = filters.value.copy(search = value)
    }

    fun applyFilters(value: ChartFilters, sort: ChartSort, sortOrder: SortOrder) {
        filters.value = value
        sorting.value = Sorting(sort, sortOrder)
    }

    fun saveFilterPreset(
        name: String,
        value: ChartFilters,
        sort: ChartSort,
        sortOrder: SortOrder,
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        viewModelScope.launch {
            val preset = FilterPreset(
                id = UUID.randomUUID().toString(),
                name = trimmedName,
                filters = value.copy(search = ""),
                sort = sort,
                sortOrder = sortOrder,
            )
            customFilterPresets.value = withContext(Dispatchers.IO) {
                userRepository.saveFilterPreset(preset)
                userRepository.loadFilterPresets()
            }
        }
    }

    fun deleteFilterPreset(id: String) {
        viewModelScope.launch {
            customFilterPresets.value = withContext(Dispatchers.IO) {
                userRepository.deleteFilterPreset(id)
                userRepository.loadFilterPresets()
            }
        }
    }

    fun clearFilters() {
        filters.value = ChartFilters(search = filters.value.search)
    }

    fun setSort(value: ChartSort) {
        sorting.value = sorting.value.copy(field = value)
    }

    fun toggleSortOrder() {
        sorting.value = sorting.value.copy(
            order = when (sorting.value.order) {
                SortOrder.ASCENDING -> SortOrder.DESCENDING
                SortOrder.DESCENDING -> SortOrder.ASCENDING
            },
        )
    }


    fun importData(region: AccountRegion) {
        val charts = snapshot.value?.charts ?: return
        if (importStatus.value is ImportStatus.Running) return
        viewModelScope.launch {
            importStatus.value = ImportStatus.Running("Starting import…")
            runCatching {
                val result = dxClient.import(region, charts) { message ->
                    importStatus.value = ImportStatus.Running(message)
                }
                val cachedProfile = withContext(Dispatchers.IO) { userRepository.saveImport(result) }
                result.copy(profile = cachedProfile)
            }.onSuccess { result ->
                scores.value = result.scores.associateBy(UserScore::chartKey)
                playDetails.value = withContext(Dispatchers.IO) { userRepository.loadPlayDetails() }
                profile.value = result.profile
                reloadTrackingData()
                CircleTrackingScheduler.schedule(getApplication())
                importStatus.value = ImportStatus.Success(
                    imported = result.scores.size,
                    recentDetails = result.playDetails.size,
                    unmatched = result.unmatchedCharts,
                )
            }.onFailure { failure ->
                importStatus.value = ImportStatus.Failure(
                    failure.message ?: "Unable to import DX NET data",
                )
            }
        }
    }

    fun clearAccount() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { userRepository.clear() }
            scores.value = emptyMap()
            playDetails.value = emptyMap()
            profile.value = null
            importStatus.value = ImportStatus.Idle
            circleHistory.value = emptyList()
            circleSnapshots.value = emptyList()
            playCountSnapshots.value = emptyList()
            trackingSettings.value = TrackingSettings()
            CircleTrackingScheduler.cancel(getApplication())
        }
    }


    fun refreshTrackingData() {
        viewModelScope.launch { reloadTrackingData() }
    }

    private suspend fun reloadTrackingData() {
        val loaded = withContext(Dispatchers.IO) {
            TrackingData(
                circleHistory = userRepository.loadCircleHistory(),
                circleSnapshots = userRepository.loadCircleSnapshots(),
                playCountSnapshots = userRepository.loadPlayCountSnapshots(),
                settings = userRepository.loadTrackingSettings(),
            )
        }
        circleHistory.value = loaded.circleHistory
        circleSnapshots.value = loaded.circleSnapshots
        playCountSnapshots.value = loaded.playCountSnapshots
        trackingSettings.value = loaded.settings
    }

    fun dismissImportStatus() {
        if (importStatus.value !is ImportStatus.Running) importStatus.value = ImportStatus.Idle
    }
}

data class CatalogUiState(
    val isLoading: Boolean = false,
    val charts: List<SongChart> = emptyList(),
    val allCharts: List<SongChart> = emptyList(),
    val filters: ChartFilters = ChartFilters(),
    val sort: ChartSort = ChartSort.LEVEL,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val options: FilterOptions = FilterOptions(),
    val info: CatalogInfo? = null,
    val visibleSongCount: Int = 0,
    val scores: Map<String, UserScore> = emptyMap(),
    val playDetails: Map<String, PlayDetail> = emptyMap(),
    val profile: PlayerProfile? = null,
    val calculatedRating: Int = 0,
    val newVersions: Set<String> = emptySet(),
    val importStatus: ImportStatus = ImportStatus.Idle,
    val circleHistory: List<CircleData> = emptyList(),
    val circleSnapshots: List<CircleDailySnapshot> = emptyList(),
    val playCountSnapshots: List<PlayCountSnapshot> = emptyList(),
    val trackingSettings: TrackingSettings = TrackingSettings(),
    val error: String? = null,
)

private data class LoadedCatalog(
    val charts: List<SongChart>,
    val options: FilterOptions,
    val info: CatalogInfo,
    val newVersions: Set<String>,
)
private data class Sorting(
    val field: ChartSort = ChartSort.LEVEL,
    val order: SortOrder = SortOrder.DESCENDING,
)
private data class PlayerData(
    val scores: Map<String, UserScore> = emptyMap(),
    val playDetails: Map<String, PlayDetail> = emptyMap(),
    val profile: PlayerProfile? = null,
    val importStatus: ImportStatus = ImportStatus.Idle,
    val tracking: TrackingData = TrackingData(),
)
private data class TrackingData(
    val circleHistory: List<CircleData> = emptyList(),
    val circleSnapshots: List<CircleDailySnapshot> = emptyList(),
    val playCountSnapshots: List<PlayCountSnapshot> = emptyList(),
    val settings: TrackingSettings = TrackingSettings(),
)

sealed interface ImportStatus {
    data object Idle : ImportStatus
    data class Running(val message: String) : ImportStatus
    data class Success(val imported: Int, val recentDetails: Int, val unmatched: Int) : ImportStatus
    data class Failure(val message: String) : ImportStatus
}


internal fun filterAndSort(
    charts: List<SongChart>,
    filters: ChartFilters,
    sort: ChartSort,
    sortOrder: SortOrder,
    scores: Map<String, UserScore>,
): List<SongChart> {
    val query = normalizeSearch(filters.search)
    val artist = normalizeSearch(filters.artist)
    val designer = normalizeSearch(filters.noteDesigner)
    fun score(chart: SongChart) = scores[chart.chartKey]
    val filtered = charts.asSequence().filter { chart ->
        (query.isEmpty() || chart.searchableText.contains(query)) &&
            (artist.isEmpty() || chart.artistSearchText.contains(artist)) &&
            (designer.isEmpty() || chart.designerSearchText.contains(designer)) &&
            (filters.categories.isEmpty() || chart.category in filters.categories) &&
            (filters.difficulties.isEmpty() || chart.difficulty in filters.difficulties) &&
            (filters.versions.isEmpty() || chart.chartVersion in filters.versions) &&
            (filters.types.isEmpty() || chart.type in filters.types) &&
            (filters.showUtage || chart.type != "utage") &&
            (filters.regions.isEmpty() || chart.regions.matchesAny(filters.regions)) &&
            (filters.minLevel == null || (chart.effectiveLevel ?: Double.NEGATIVE_INFINITY) >= filters.minLevel) &&
            (filters.maxLevel == null || (chart.effectiveLevel ?: Double.POSITIVE_INFINITY) <= filters.maxLevel) &&
            (filters.minBpm == null || (chart.bpm ?: Int.MIN_VALUE) >= filters.minBpm) &&
            (filters.maxBpm == null || (chart.bpm ?: Int.MAX_VALUE) <= filters.maxBpm) &&
            when (filters.constantAvailability) {
                ConstantAvailability.BOTH -> true
                ConstantAvailability.KNOWN -> chart.constant != null
                ConstantAvailability.UNKNOWN -> chart.constant == null
            } &&
            (!filters.scoredOnly || score(chart) != null) &&
            (filters.grades.isEmpty() || score(chart)?.grade in filters.grades) &&
            (filters.comboMedals.isEmpty() || score(chart)?.comboMedal in filters.comboMedals) &&
            (filters.syncMedals.isEmpty() || score(chart)?.syncMedal in filters.syncMedals)
    }.toList()

    val ascending = sortOrder == SortOrder.ASCENDING
    val ratingValues = if (sort == ChartSort.RATING) {
        buildMap {
            filtered.forEach { chart ->
                scores[chart.chartKey]
                    ?.let { current -> RatingCalculator.chartRating(chart, current) }
                    ?.let { rating -> put(chart.chartKey, rating) }
            }
        }
    } else {
        emptyMap()
    }
    val comparator: Comparator<SongChart> = when (sort) {
        ChartSort.LEVEL -> if (ascending) {
            compareBy<SongChart> { it.effectiveLevel ?: Double.POSITIVE_INFINITY }
                .thenBy { it.titleSortKey }
        } else {
            compareByDescending<SongChart> { it.effectiveLevel ?: Double.NEGATIVE_INFINITY }
                .thenBy { it.titleSortKey }
        }
        ChartSort.TITLE -> if (ascending) {
            compareBy<SongChart> { it.titleSortKey }
                .thenBy { it.effectiveLevel ?: Double.POSITIVE_INFINITY }
        } else {
            compareByDescending<SongChart> { it.titleSortKey }
                .thenByDescending { it.effectiveLevel ?: Double.NEGATIVE_INFINITY }
        }
        ChartSort.RATING -> if (ascending) {
            compareBy<SongChart> { ratingValues[it.chartKey] ?: Int.MAX_VALUE }
                .thenBy { it.titleSortKey }
        } else {
            compareByDescending<SongChart> { ratingValues[it.chartKey] ?: Int.MIN_VALUE }
                .thenBy { it.titleSortKey }
        }
        ChartSort.ACHIEVEMENT -> if (ascending) {
            compareBy<SongChart> { scores[it.chartKey]?.achievement ?: Double.POSITIVE_INFINITY }
                .thenBy { it.titleSortKey }
        } else {
            compareByDescending<SongChart> { scores[it.chartKey]?.achievement ?: Double.NEGATIVE_INFINITY }
                .thenBy { it.titleSortKey }
        }
        ChartSort.GRADE -> if (ascending) {
            compareBy<SongChart> { scores[it.chartKey]?.grade?.threshold ?: Double.POSITIVE_INFINITY }
                .thenBy { scores[it.chartKey]?.achievement ?: Double.POSITIVE_INFINITY }
                .thenBy { it.titleSortKey }
        } else {
            compareByDescending<SongChart> { scores[it.chartKey]?.grade?.threshold ?: Double.NEGATIVE_INFINITY }
                .thenByDescending { scores[it.chartKey]?.achievement ?: Double.NEGATIVE_INFINITY }
                .thenBy { it.titleSortKey }
        }
        ChartSort.DX_SCORE -> if (ascending) {
            compareBy<SongChart> { scores[it.chartKey]?.dxScore ?: Int.MAX_VALUE }
                .thenBy { it.titleSortKey }
        } else {
            compareByDescending<SongChart> { scores[it.chartKey]?.dxScore ?: Int.MIN_VALUE }
                .thenBy { it.titleSortKey }
        }
        ChartSort.RELEASE -> if (ascending) {
            compareBy<SongChart> { it.releaseDate ?: "\uffff" }
                .thenBy { it.titleSortKey }
        } else {
            compareByDescending<SongChart> { it.releaseDate.orEmpty() }
                .thenBy { it.titleSortKey }
        }
        ChartSort.BPM -> if (ascending) {
            compareBy<SongChart> { it.bpm ?: Int.MAX_VALUE }
                .thenBy { it.titleSortKey }
        } else {
            compareByDescending<SongChart> { it.bpm ?: Int.MIN_VALUE }
                .thenBy { it.titleSortKey }
        }
    }
    return filtered.sortedWith(comparator)
}
