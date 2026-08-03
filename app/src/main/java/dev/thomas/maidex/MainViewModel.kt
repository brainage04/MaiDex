package dev.thomas.maidex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.thomas.maidex.data.AccountRegion
import dev.thomas.maidex.data.CatalogInfo
import dev.thomas.maidex.data.CatalogRepository
import dev.thomas.maidex.data.ChartFilters
import dev.thomas.maidex.data.CircleDailySnapshot
import dev.thomas.maidex.data.CircleData
import dev.thomas.maidex.data.ChartSort
import dev.thomas.maidex.data.ConstantAvailability
import dev.thomas.maidex.data.SortOrder
import dev.thomas.maidex.data.FilterOptions
import dev.thomas.maidex.data.SongChart
import dev.thomas.maidex.data.PlayDetail
import dev.thomas.maidex.data.PlayerProfile
import dev.thomas.maidex.data.PlayCountSnapshot
import dev.thomas.maidex.data.UserDataRepository
import dev.thomas.maidex.data.TrackingSettings
import dev.thomas.maidex.data.UserScore
import dev.thomas.maidex.network.MaimaiDxClient
import dev.thomas.maidex.rating.RatingCalculator
import dev.thomas.maidex.data.normalizeSearch
import dev.thomas.maidex.tracking.CircleTrackingScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val newVersions = loaded.options.versions.take(2).toSet()
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
                calculatedRating = RatingCalculator.totalRating(loaded.charts, player.scores, newVersions),
                newVersions = newVersions,
                importStatus = player.importStatus,
                circleHistory = player.tracking.circleHistory,
                circleSnapshots = player.tracking.circleSnapshots,
                playCountSnapshots = player.tracking.playCountSnapshots,
                trackingSettings = player.tracking.settings,
                error = failure,
            )
        }
    }.stateIn(
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
                    )
                }
            }.onSuccess { loaded ->
                snapshot.value = loaded
            }
                .onFailure { failure -> error.value = failure.message ?: "Unable to load catalog" }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                scores.value = userRepository.loadScores()
                playDetails.value = userRepository.loadPlayDetails()
                profile.value = userRepository.loadProfile()
            }
            reloadTrackingData()
            profile.value?.let {
                CircleTrackingScheduler.schedule(application, trackingSettings.value.syncHour)
            }
        }
    }

    fun setSearch(value: String) {
        filters.value = filters.value.copy(search = value)
    }

    fun applyFilters(value: ChartFilters) {
        filters.value = value
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


    fun importAccount(region: AccountRegion) {
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
                CircleTrackingScheduler.schedule(
                    getApplication(),
                    trackingSettings.value.syncHour,
                )
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

    fun setTrackingHour(hour: Int) {
        val safeHour = hour.coerceIn(0, 23)
        userRepository.setTrackingHour(safeHour)
        trackingSettings.value = userRepository.loadTrackingSettings()
        if (profile.value != null) {
            CircleTrackingScheduler.schedule(getApplication(), safeHour)
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
            (artist.isEmpty() || normalizeSearch("${chart.artist} ${chart.artistRomanized}").contains(artist)) &&
            (designer.isEmpty() || normalizeSearch(
                "${chart.noteDesigner.orEmpty()} ${chart.noteDesignerRomanized}",
            ).contains(designer)) &&
            (filters.categories.isEmpty() || chart.category in filters.categories) &&
            (filters.difficulties.isEmpty() || chart.difficulty in filters.difficulties) &&
            (filters.versions.isEmpty() || chart.chartVersion in filters.versions) &&
            (filters.types.isEmpty() || chart.type in filters.types) &&
            (filters.showUtage || chart.type != "utage") &&
            (filters.regions.isEmpty() || chart.regions.codes().any(filters.regions::contains)) &&
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
    val comparator: Comparator<SongChart> = when (sort) {
        ChartSort.LEVEL -> if (ascending) {
            compareBy<SongChart> { it.effectiveLevel ?: Double.POSITIVE_INFINITY }
                .thenBy { it.title.lowercase() }
        } else {
            compareByDescending<SongChart> { it.effectiveLevel ?: Double.NEGATIVE_INFINITY }
                .thenBy { it.title.lowercase() }
        }
        ChartSort.TITLE -> if (ascending) {
            compareBy<SongChart> { it.title.lowercase() }
                .thenBy { it.effectiveLevel ?: Double.POSITIVE_INFINITY }
        } else {
            compareByDescending<SongChart> { it.title.lowercase() }
                .thenByDescending { it.effectiveLevel ?: Double.NEGATIVE_INFINITY }
        }
        ChartSort.RATING -> if (ascending) {
            compareBy<SongChart> {
                scores[it.chartKey]?.let { current -> RatingCalculator.chartRating(it, current) }
                    ?: Int.MAX_VALUE
            }.thenBy { it.title.lowercase() }
        } else {
            compareByDescending<SongChart> {
                scores[it.chartKey]?.let { current -> RatingCalculator.chartRating(it, current) }
                    ?: Int.MIN_VALUE
            }.thenBy { it.title.lowercase() }
        }
        ChartSort.ACHIEVEMENT -> if (ascending) {
            compareBy<SongChart> { scores[it.chartKey]?.achievement ?: Double.POSITIVE_INFINITY }
                .thenBy { it.title.lowercase() }
        } else {
            compareByDescending<SongChart> { scores[it.chartKey]?.achievement ?: Double.NEGATIVE_INFINITY }
                .thenBy { it.title.lowercase() }
        }
        ChartSort.GRADE -> if (ascending) {
            compareBy<SongChart> { scores[it.chartKey]?.grade?.threshold ?: Double.POSITIVE_INFINITY }
                .thenBy { scores[it.chartKey]?.achievement ?: Double.POSITIVE_INFINITY }
                .thenBy { it.title.lowercase() }
        } else {
            compareByDescending<SongChart> { scores[it.chartKey]?.grade?.threshold ?: Double.NEGATIVE_INFINITY }
                .thenByDescending { scores[it.chartKey]?.achievement ?: Double.NEGATIVE_INFINITY }
                .thenBy { it.title.lowercase() }
        }
        ChartSort.DX_SCORE -> if (ascending) {
            compareBy<SongChart> { scores[it.chartKey]?.dxScore ?: Int.MAX_VALUE }
                .thenBy { it.title.lowercase() }
        } else {
            compareByDescending<SongChart> { scores[it.chartKey]?.dxScore ?: Int.MIN_VALUE }
                .thenBy { it.title.lowercase() }
        }
        ChartSort.RELEASE -> if (ascending) {
            compareBy<SongChart> { it.releaseDate ?: "\uffff" }
                .thenBy { it.title.lowercase() }
        } else {
            compareByDescending<SongChart> { it.releaseDate.orEmpty() }
                .thenBy { it.title.lowercase() }
        }
        ChartSort.BPM -> if (ascending) {
            compareBy<SongChart> { it.bpm ?: Int.MAX_VALUE }
                .thenBy { it.title.lowercase() }
        } else {
            compareByDescending<SongChart> { it.bpm ?: Int.MIN_VALUE }
                .thenBy { it.title.lowercase() }
        }
    }
    return filtered.sortedWith(comparator)
}
