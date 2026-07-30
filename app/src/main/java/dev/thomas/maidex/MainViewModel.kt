package dev.thomas.maidex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.thomas.maidex.data.CatalogInfo
import dev.thomas.maidex.data.CatalogRepository
import dev.thomas.maidex.data.ChartFilters
import dev.thomas.maidex.data.ChartSort
import dev.thomas.maidex.data.FilterOptions
import dev.thomas.maidex.data.SongChart
import dev.thomas.maidex.data.normalizeSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CatalogRepository(application)
    private val snapshot = MutableStateFlow<LoadedCatalog?>(null)
    private val filters = MutableStateFlow(ChartFilters())
    private val sort = MutableStateFlow(ChartSort.CONSTANT_DESC)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CatalogUiState> = combine(snapshot, filters, sort, error) {
            loaded,
            activeFilters,
            activeSort,
            failure,
        ->
        if (loaded == null) {
            CatalogUiState(isLoading = failure == null, error = failure)
        } else {
            val visible = filterAndSort(loaded.charts, activeFilters, activeSort)
            CatalogUiState(
                charts = visible,
                filters = activeFilters,
                sort = activeSort,
                options = loaded.options,
                info = loaded.info,
                visibleSongCount = visible.asSequence().map { it.sourceSongId }.distinct().count(),
                error = failure,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { repository.load() } }
                .onSuccess { loaded ->
                    snapshot.value = LoadedCatalog(loaded.charts, loaded.options, loaded.info)
                }
                .onFailure { failure -> error.value = failure.message ?: "Unable to load catalog" }
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
        sort.value = value
    }
}

data class CatalogUiState(
    val isLoading: Boolean = false,
    val charts: List<SongChart> = emptyList(),
    val filters: ChartFilters = ChartFilters(),
    val sort: ChartSort = ChartSort.CONSTANT_DESC,
    val options: FilterOptions = FilterOptions(),
    val info: CatalogInfo? = null,
    val visibleSongCount: Int = 0,
    val error: String? = null,
)

private data class LoadedCatalog(
    val charts: List<SongChart>,
    val options: FilterOptions,
    val info: CatalogInfo,
)

private fun filterAndSort(
    charts: List<SongChart>,
    filters: ChartFilters,
    sort: ChartSort,
): List<SongChart> {
    val query = normalizeSearch(filters.search)
    val artist = normalizeSearch(filters.artist)
    val designer = normalizeSearch(filters.noteDesigner)
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
            (filters.regions.isEmpty() || chart.regions.codes().any(filters.regions::contains)) &&
            (filters.minLevel == null || (chart.effectiveLevel ?: Double.NEGATIVE_INFINITY) >= filters.minLevel) &&
            (filters.maxLevel == null || (chart.effectiveLevel ?: Double.POSITIVE_INFINITY) <= filters.maxLevel) &&
            (filters.minBpm == null || (chart.bpm ?: Int.MIN_VALUE) >= filters.minBpm) &&
            (filters.maxBpm == null || (chart.bpm ?: Int.MAX_VALUE) <= filters.maxBpm) &&
            (!filters.knownConstantsOnly || chart.constant != null)
    }.toList()

    return when (sort) {
        ChartSort.CONSTANT_DESC -> filtered.sortedWith(
            compareByDescending<SongChart> { it.constant ?: Double.NEGATIVE_INFINITY }
                .thenBy { it.title.lowercase() }
                .thenBy { it.difficulty },
        )
        ChartSort.LEVEL_ASC -> filtered.sortedWith(
            compareBy<SongChart> { it.effectiveLevel ?: Double.POSITIVE_INFINITY }
                .thenBy { it.title.lowercase() },
        )
        ChartSort.TITLE_ASC -> filtered.sortedWith(
            compareBy<SongChart> { it.title.lowercase() }
                .thenBy { it.effectiveLevel ?: Double.POSITIVE_INFINITY },
        )
        ChartSort.RELEASE_DESC -> filtered.sortedWith(
            compareByDescending<SongChart> { it.releaseDate.orEmpty() }
                .thenBy { it.title.lowercase() },
        )
        ChartSort.BPM_ASC -> filtered.sortedWith(
            compareBy<SongChart> { it.bpm ?: Int.MAX_VALUE }
                .thenBy { it.title.lowercase() },
        )
    }
}
