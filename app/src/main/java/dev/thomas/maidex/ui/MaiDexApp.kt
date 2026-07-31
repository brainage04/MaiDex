package dev.thomas.maidex.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.thomas.maidex.DxNetLoginActivity
import dev.thomas.maidex.CatalogUiState
import dev.thomas.maidex.ImportStatus
import dev.thomas.maidex.MainViewModel
import dev.thomas.maidex.data.AccountRegion
import dev.thomas.maidex.data.ChartFilters
import dev.thomas.maidex.data.ChartSort
import dev.thomas.maidex.data.ComboMedal
import dev.thomas.maidex.data.FilterOptions
import dev.thomas.maidex.data.SongChart
import dev.thomas.maidex.data.SongUnlockInfo
import dev.thomas.maidex.data.UnlockGuideEntry
import dev.thomas.maidex.data.UnlockGuideSection
import dev.thomas.maidex.data.UnlockMetadata
import dev.thomas.maidex.data.ConstantAvailability
import dev.thomas.maidex.data.Grade
import dev.thomas.maidex.data.JudgeCounts
import dev.thomas.maidex.data.PlayDetail
import dev.thomas.maidex.data.PlayerProfile
import java.util.Locale
import dev.thomas.maidex.data.SortOrder
import dev.thomas.maidex.data.SyncMedal
import dev.thomas.maidex.data.UserScore
import dev.thomas.maidex.rating.AchievementLossCalculator
import dev.thomas.maidex.rating.RatingCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaiDexApp(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    var selectedChart by remember { mutableStateOf<SongChart?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    var showUnlockGuide by remember { mutableStateOf(false) }
    var selectedUnlockGuideEntryId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Column {
                        Text("MaiDex", fontWeight = FontWeight.Black)
                        if (state.info != null) {
                            Text(
                                "${state.visibleSongCount} songs · ${state.charts.size} charts",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        selectedUnlockGuideEntryId = null
                        showUnlockGuide = true
                    }) {
                        Icon(Icons.Default.Map, contentDescription = "Chiho and class battle guides")
                    }
                    IconButton(onClick = { showAccount = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "DX NET account")
                    }
                    IconButton(onClick = { showAbout = true }) {
                        Icon(Icons.Default.Info, contentDescription = "About catalog")
                    }
                    BadgedBox(
                        badge = {
                            if (state.filters.activeCount > 0) {
                                Badge(
                                    modifier = Modifier.size(20.dp),
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ) {
                                    Text(
                                        state.filters.activeCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        },
                    ) {
                        IconButton(onClick = { showFilters = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Open filters")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            state.error != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            else -> CatalogContent(
                state = state,
                modifier = Modifier.padding(padding),
                onSearch = viewModel::setSearch,
                onSort = viewModel::setSort,
                onToggleSortOrder = viewModel::toggleSortOrder,
                onToggleUtage = viewModel::toggleUtageVisibility,
                onClearFilters = viewModel::clearFilters,
                onChart = { selectedChart = it },
                onUnlockGuide = { entryId ->
                    selectedUnlockGuideEntryId = entryId
                    showUnlockGuide = true
                },
            )
        }
    }

    if (showFilters) {
        FilterDialog(
            filters = state.filters,
            options = state.options,
            hasScores = state.scores.isNotEmpty(),
            onDismiss = { showFilters = false },
            onApply = {
                viewModel.applyFilters(it)
                showFilters = false
            },
        )
    }
    selectedChart?.let { chart ->
        ChartDetailDialog(
            chart = chart,
            score = state.scores[chart.chartKey],
            playDetail = state.playDetails[chart.chartKey],
            state = state,
            onDismiss = { selectedChart = null },
            onOpenGuide = { entryId ->
                selectedChart = null
                selectedUnlockGuideEntryId = entryId
                showUnlockGuide = true
            },
        )
    }
    if (showAccount) {
        AccountDialog(
            profile = state.profile,
            importStatus = state.importStatus,
            onImport = viewModel::importAccount,
            onClear = viewModel::clearAccount,
            onDismissStatus = viewModel::dismissImportStatus,
            onDismiss = { showAccount = false },
        )
    }
    if (showUnlockGuide) {
        UnlockGuideDialog(
            initialEntryId = selectedUnlockGuideEntryId,
            charts = state.allCharts,
            onDismiss = {
                showUnlockGuide = false
                selectedUnlockGuideEntryId = null
            },
        )
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Offline catalog") },
            text = {
                Text(
                    "${state.info?.songCount ?: 0} songs and ${state.info?.chartCount ?: 0} charts. " +
                        "Metadata: arcade-songs, updated ${state.info?.updateTime?.take(10).orEmpty()}. " +
                        "Community English aliases improve romanised search. Cover art is cached after loading. " +
                        "DX NET cookies and imported scores remain on this device.",
                )
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Close") } },
        )
    }
}

@Composable
private fun CatalogContent(
    state: CatalogUiState,
    modifier: Modifier,
    onSearch: (String) -> Unit,
    onSort: (ChartSort) -> Unit,
    onToggleSortOrder: () -> Unit,
    onToggleUtage: () -> Unit,
    onClearFilters: () -> Unit,
    onChart: (SongChart) -> Unit,
    onUnlockGuide: (String) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.filters.search,
            onValueChange = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.filters.search.isNotEmpty()) {
                    IconButton(onClick = { onSearch("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            label = { Text("Find a chart constant") },
            placeholder = { Text("Title, romaji, artist, or notes designer") },
            supportingText = { Text("Internal constants are shown in parentheses on every chart") },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SortMenu(
                selected = state.sort,
                onSelect = onSort,
                modifier = Modifier.fillMaxWidth(),
            )
            AssistChip(
                onClick = onToggleSortOrder,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(state.sortOrder.label) },
                leadingIcon = {
                    Icon(
                        if (state.sortOrder == SortOrder.ASCENDING) {
                            Icons.Default.ArrowUpward
                        } else {
                            Icons.Default.ArrowDownward
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            FilterChip(
                selected = state.filters.showUtage,
                onClick = onToggleUtage,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(if (state.filters.showUtage) "UTAGE: shown" else "UTAGE: hidden")
                },
            )
            if (state.filters.activeCount > 0) {
                TextButton(
                    onClick = onClearFilters,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Clear, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear filters")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (state.charts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No matching charts", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onClearFilters) { Text("Clear filters") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.charts, key = { it.id }) { chart ->
                    ChartCard(
                        chart = chart,
                        score = state.scores[chart.chartKey],
                        onClick = { onChart(chart) },
                        onUnlockGuide = onUnlockGuide,
                    )
                }
            }
        }
    }
}

@Composable
private fun SortMenu(
    selected: ChartSort,
    onSelect: (ChartSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        AssistChip(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Sort: ${selected.label}") },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ChartSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ConstantAvailabilityMenu(
    selected: ConstantAvailability,
    onSelect: (ConstantAvailability) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text("Constant availability", style = MaterialTheme.typography.labelMedium)
                Text(selected.label)
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ConstantAvailability.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
@Composable
private fun ChartCard(
    chart: SongChart,
    score: UserScore?,
    onClick: () -> Unit,
    onUnlockGuide: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = chart.imageUrl,
                contentDescription = "Cover art for ${chart.title}",
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titleWithRomanization(chart.title, chart.titleRomanized),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    chart.artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(7.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = difficultyColor(chart.difficulty),
                        shape = RoundedCornerShape(7.dp),
                    ) {
                        Text(
                            chart.displayDifficulty,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Text(
                        buildString {
                            append(chart.level ?: "?")
                            chart.constant?.let { append(" (${String.format(Locale.US, "%.1f", it)})") }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (chart.constant != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(chart.displayType, style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    listOfNotNull(chart.chartVersion, chart.bpm?.let { "$it BPM" }).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                chart.unlockInfo.forEach { info ->
                    UnlockTileInfo(
                        info = info,
                        onOpenGuide = info.guideEntryId?.let { entryId ->
                            { onUnlockGuide(entryId) }
                        },
                    )
                }
                if (score != null) {
                    HorizontalDivider(Modifier.padding(vertical = 7.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            String.format(Locale.US, "%.4f%%", score.achievement),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        MedalPill(score.grade.label, MaterialTheme.colorScheme.primaryContainer)
                        Spacer(Modifier.weight(1f))
                        RatingCalculator.chartRating(chart, score)?.let {
                            Text("Rt $it", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (score.comboMedal != ComboMedal.NONE) {
                            MedalPill(score.comboMedal.label, Color(0xFFFFD9E4))
                        }
                        if (score.syncMedal != SyncMedal.NONE) {
                            MedalPill(score.syncMedal.label, Color(0xFFD4E8FF))
                        }
                    }
                    DxScoreLine(score)
                }
            }
        }
    }
}
@Composable
private fun UnlockTileInfo(
    info: SongUnlockInfo,
    onOpenGuide: (() -> Unit)?,
) {
    Spacer(Modifier.height(6.dp))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onOpenGuide != null) Modifier.clickable(onClick = onOpenGuide) else Modifier),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
            Text(
                info.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                info.summary,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (onOpenGuide != null) {
                Text(
                    "View in unlock guides ›",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun UnlockDetailInfo(
    info: SongUnlockInfo,
    onOpenGuide: (() -> Unit)?,
    onOpenSource: () -> Unit,
) {
    Spacer(Modifier.height(12.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                info.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                info.summary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                info.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Row(modifier = Modifier.align(Alignment.End)) {
                if (onOpenGuide != null) {
                    TextButton(onClick = onOpenGuide) {
                        Text("Open guide entry")
                    }
                }
                TextButton(onClick = onOpenSource) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Source")
                }
            }
        }
    }
}

@Composable
private fun DxScoreLine(score: UserScore) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "DX ${dxScoreText(score)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (score.dxStarCount > 0) {
            Text(
                "★".repeat(score.dxStarCount),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF5A000),
            )
        }
    }
}

private fun dxScoreText(score: UserScore): String {
    val percentage = score.dxScorePercentage
        ?: return "${score.dxScore}/${score.maxDxScore}"
    return "${score.dxScore}/${score.maxDxScore} (${String.format(Locale.US, "%.2f", percentage)}%)"
}

@Composable
private fun MedalPill(label: String, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(7.dp)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun UnlockGuideDialog(
    initialEntryId: String?,
    charts: List<SongChart>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val initialEntry = remember(initialEntryId) {
        initialEntryId?.let(UnlockMetadata::guideEntry)
    }
    var section by remember(initialEntryId) {
        mutableStateOf(initialEntry?.section ?: UnlockGuideSection.CHIHOS)
    }
    val romanizedTitles = remember(charts) {
        charts.asSequence().associate { it.sourceSongId to it.titleRomanized }
    }
    val entries = UnlockMetadata.guideEntries.filter { it.section == section }
    val orderedEntries = if (initialEntry?.section == section) {
        listOf(initialEntry) + entries.filterNot { it.id == initialEntry.id }
    } else {
        entries
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Unlock guides", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "International Chihos and Friend Matching Gift Songs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnlockGuideSection.entries.forEach { option ->
                        FilterChip(
                            selected = section == option,
                            onClick = { section = option },
                            label = { Text(option.label) },
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(orderedEntries, key = UnlockGuideEntry::id) { entry ->
                        UnlockGuideEntryCard(
                            entry = entry,
                            isLinkedEntry = entry.id == initialEntryId,
                            romanizedTitles = romanizedTitles,
                            onOpenSource = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(entry.sourceUrl)))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnlockGuideEntryCard(
    entry: UnlockGuideEntry,
    isLinkedEntry: Boolean,
    romanizedTitles: Map<String, String>,
    onOpenSource: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isLinkedEntry) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        shape = RoundedCornerShape(14.dp),
        border = if (isLinkedEntry) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(Modifier.padding(12.dp)) {
            if (isLinkedEntry) {
                Text(
                    "Linked entry",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                titleWithRomanization(entry.title, entry.titleRomanized),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                entry.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(5.dp))
            Text(entry.details, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            entry.songs.forEach { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "• ${titleWithRomanization(song.title, romanizedTitles[song.title].orEmpty())}",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (song.requirement.isNotBlank()) {
                        Text(
                            song.requirement,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            TextButton(onClick = onOpenSource, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Source")
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterDialog(
    filters: ChartFilters,
    options: FilterOptions,
    hasScores: Boolean,
    onDismiss: () -> Unit,
    onApply: (ChartFilters) -> Unit,
) {
    var draft by remember(filters) { mutableStateOf(filters) }
    var minLevel by remember(filters) { mutableStateOf(filters.minLevel?.toString().orEmpty()) }
    var maxLevel by remember(filters) { mutableStateOf(filters.maxLevel?.toString().orEmpty()) }
    var minBpm by remember(filters) { mutableStateOf(filters.minBpm?.toString().orEmpty()) }
    var maxBpm by remember(filters) { mutableStateOf(filters.maxBpm?.toString().orEmpty()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Filters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        draft = ChartFilters(search = filters.search)
                        minLevel = ""
                        maxLevel = ""
                        minBpm = ""
                        maxBpm = ""
                    }) { Text("Reset") }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = draft.artist,
                        onValueChange = { draft = draft.copy(artist = it) },
                        label = { Text("Artist") },
                        placeholder = { Text("Original or romanised name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = draft.noteDesigner,
                        onValueChange = { draft = draft.copy(noteDesigner = it) },
                        label = { Text("Notes designer") },
                        placeholder = { Text("Original or romanised name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    RangeFields(
                        title = "Level / constant range",
                        minimum = minLevel,
                        maximum = maxLevel,
                        keyboardType = KeyboardType.Decimal,
                        onMinimum = { minLevel = it },
                        onMaximum = { maxLevel = it },
                    )
                    RangeFields(
                        title = "BPM range",
                        minimum = minBpm,
                        maximum = maxBpm,
                        keyboardType = KeyboardType.Number,
                        onMinimum = { minBpm = it.filter(Char::isDigit) },
                        onMaximum = { maxBpm = it.filter(Char::isDigit) },
                    )
                    ConstantAvailabilityMenu(
                        selected = draft.constantAvailability,
                        onSelect = { draft = draft.copy(constantAvailability = it) },
                    )
                    if (hasScores) {
                        Text(
                            "My scores",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Played charts only", modifier = Modifier.weight(1f))
                            Switch(
                                checked = draft.scoredOnly,
                                onCheckedChange = { draft = draft.copy(scoredOnly = it) },
                            )
                        }
                        MultiSelectSection(
                            "Rank",
                            Grade.entries,
                            draft.grades,
                            label = Grade::label,
                        ) { draft = draft.copy(grades = it) }
                        MultiSelectSection(
                            "Achievement medal",
                            ComboMedal.entries.filterNot { it == ComboMedal.NONE },
                            draft.comboMedals,
                            label = ComboMedal::label,
                        ) { draft = draft.copy(comboMedals = it) }
                        MultiSelectSection(
                            "Sync medal",
                            SyncMedal.entries.filterNot { it == SyncMedal.NONE },
                            draft.syncMedals,
                            label = SyncMedal::label,
                        ) { draft = draft.copy(syncMedals = it) }
                        HorizontalDivider()
                    } else {
                        Text(
                            "Sign in to DX NET to filter by rank, achievement medal, and sync medal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    MultiSelectSection("Category", options.categories, draft.categories) {
                        draft = draft.copy(categories = it)
                    }
                    MultiSelectSection(
                        "Difficulty",
                        options.difficulties,
                        draft.difficulties,
                        label = { difficultyLabel(it) },
                    ) { draft = draft.copy(difficulties = it) }
                    MultiSelectSection(
                        "Type",
                        options.types,
                        draft.types,
                        label = { typeLabel(it) },
                    ) { draft = draft.copy(types = it) }
                    MultiSelectSection(
                        "Region",
                        options.regions,
                        draft.regions,
                        label = { regionLabel(it) },
                    ) { draft = draft.copy(regions = it) }
                    MultiSelectSection("Version", options.versions, draft.versions) {
                        draft = draft.copy(versions = it)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        onApply(
                            draft.copy(
                                minLevel = minLevel.toDoubleOrNull(),
                                maxLevel = maxLevel.toDoubleOrNull(),
                                minBpm = minBpm.toIntOrNull(),
                                maxBpm = maxBpm.toIntOrNull(),
                            ),
                        )
                    }) { Text("Show results") }
                }
            }
        }
    }
}

@Composable
private fun RangeFields(
    title: String,
    minimum: String,
    maximum: String,
    keyboardType: KeyboardType,
    onMinimum: (String) -> Unit,
    onMaximum: (String) -> Unit,
) {
    Column {
        Text(title, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minimum,
                onValueChange = onMinimum,
                label = { Text("Min") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            )
            OutlinedTextField(
                value = maximum,
                onValueChange = onMaximum,
                label = { Text("Max") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> MultiSelectSection(
    title: String,
    options: List<T>,
    selected: Set<T>,
    label: (T) -> String = { it.toString() },
    onChange: (Set<T>) -> Unit,
) {
    Column {
        Text(title, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option in selected,
                    onClick = {
                        onChange(if (option in selected) selected - option else selected + option)
                    },
                    label = { Text(label(option)) },
                )
            }
        }
    }
}

@Composable
private fun AccountDialog(
    profile: PlayerProfile?,
    importStatus: ImportStatus,
    onImport: (AccountRegion) -> Unit,
    onClear: () -> Unit,
    onDismissStatus: () -> Unit,
    onDismiss: () -> Unit,
) {
    var region by remember(profile) { mutableStateOf(profile?.region ?: AccountRegion.INTERNATIONAL) }
    val context = LocalContext.current
    val isImporting = importStatus is ImportStatus.Running

    Dialog(onDismissRequest = {
        onDismissStatus()
        onDismiss()
    }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.96f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "maimai DX NET",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "Official account data, stored only on this device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = {
                        onDismissStatus()
                        onDismiss()
                    }) { Text("Close") }
                }

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AccountRegion.entries.forEach { option ->
                        FilterChip(
                            selected = region == option,
                            enabled = !isImporting,
                            onClick = { region = option },
                            label = { Text(option.label) },
                        )
                    }
                }

                if (profile != null) {
                    DxNetProfileCard(
                        profile = profile,
                        modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 8.dp),
                        color = Color(0xFFE5F6FD),
                        border = BorderStroke(1.dp, Color(0xFF73C8E8)),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF087BA8),
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("No DX NET profile imported", fontWeight = FontWeight.Bold)
                                Text(
                                    "Sign in, then import your scores.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                when (importStatus) {
                    is ImportStatus.Running -> Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 3.dp)
                        Spacer(Modifier.width(9.dp))
                        Text(importStatus.message)
                    }
                    is ImportStatus.Success -> Text(
                        "Imported ${importStatus.imported} scores and ${importStatus.recentDetails} recent details" +
                            if (importStatus.unmatched > 0) " · ${importStatus.unmatched} unmatched" else "",
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    is ImportStatus.Failure -> Text(
                        importStatus.message,
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                    ImportStatus.Idle -> Unit
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            "Sign-in opens the official DX NET site full screen. " +
                                "MaiDex reads your profile and scores only after DX NET confirms the session.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { context.startActivity(DxNetLoginActivity.intent(context, region)) },
                        enabled = !isImporting,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Sign in", maxLines = 1)
                    }
                    Button(
                        onClick = { onImport(region) },
                        enabled = !isImporting,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (profile == null) "Import scores" else "Refresh scores",
                            maxLines = 1,
                        )
                    }
                }
                if (profile != null) {
                    TextButton(
                        onClick = onClear,
                        enabled = !isImporting,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) { Text("Remove imported account data") }
                }
            }
        }
    }
}

@Composable
private fun DxNetProfileCard(
    profile: PlayerProfile,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF54C6EF),
        border = BorderStroke(2.dp, Color(0xFF126686)),
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF39B9EA),
                            Color(0xFF8DDEFA),
                            Color(0xFF48C4ED),
                        ),
                    ),
                )
                .padding(8.dp),
        ) {
            Surface(
                color = Color(0xFFFCFEFF),
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(1.dp, Color(0xFF7D929D)),
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DxNetAvatar(profile)
                    Spacer(Modifier.width(9.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color(0xFFF2A300)),
                            color = Color.Transparent,
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFFC800),
                                                Color(0xFFFFEB66),
                                                Color(0xFFFFC400),
                                            ),
                                        ),
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    profile.title.ifBlank {
                                        "DX NET profile · ${profile.region.label}"
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF20252A),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                shape = RoundedCornerShape(7.dp),
                                color = Color(0xFFFAFCFD),
                                border = BorderStroke(1.dp, Color(0xFFCCD2D5)),
                                shadowElevation = 1.dp,
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Text(
                                        profile.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            DxRatingBadge(profile.officialRating)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            profile.courseRankUrl.takeIf(String::isNotBlank)?.let {
                                DxNetRankImage(it, "Course")
                            }
                            profile.classRankUrl.takeIf(String::isNotBlank)?.let {
                                DxNetRankImage(it, "Class")
                            }
                            if (profile.starCount != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF7D7),
                                    border = BorderStroke(1.dp, Color(0xFFFFC64B)),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(21.dp),
                                            tint = Color(0xFFF4A900),
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            "×${profile.starCount}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                }
                            }
                            if (
                                profile.courseRankUrl.isBlank() &&
                                profile.classRankUrl.isBlank() &&
                                profile.starCount == null
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5FA),
                                ) {
                                    Text(
                                        profile.region.label,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF126686),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DxNetAvatar(profile: PlayerProfile) {
    Surface(
        modifier = Modifier.size(78.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFE5F6FD),
        border = BorderStroke(2.dp, Color(0xFF43BDE9)),
        shadowElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(58.dp),
                tint = Color(0xFF178AB6),
            )
            if (profile.avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = "${profile.name}'s DX NET icon",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = 38.dp, height = 7.dp)
                    .clip(RoundedCornerShape(bottomStart = 7.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF5EAE), Color(0xFF7A5CFF)),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun DxRatingBadge(rating: Int) {
    Surface(
        modifier = Modifier
            .width(80.dp)
            .height(38.dp),
        shape = RoundedCornerShape(7.dp),
        color = Color(0xFF505A60),
        border = BorderStroke(2.dp, Color(0xFF45D8E8)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFE544), Color(0xFFFF6C62)),
                        ),
                    )
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "RATING",
                    fontSize = 5.sp,
                    lineHeight = 6.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1D2930),
                )
            }
            Text(
                rating.toString(),
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DxNetRankImage(url: String, fallbackLabel: String) {
    Surface(
        modifier = Modifier
            .width(72.dp)
            .height(36.dp),
        shape = RoundedCornerShape(7.dp),
        color = Color(0xFFF2F5F7),
        border = BorderStroke(1.dp, Color(0xFFCBD3D7)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                fallbackLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF60717A),
            )
            AsyncImage(
                model = url,
                contentDescription = "$fallbackLabel rank",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun ChartDetailDialog(
    chart: SongChart,
    score: UserScore?,
    playDetail: PlayDetail?,
    state: CatalogUiState,
    onOpenGuide: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                AsyncImage(
                    model = chart.imageUrl,
                    contentDescription = "Cover art for ${chart.title}",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    titleWithRomanization(chart.title, chart.titleRomanized),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(12.dp))
                DetailRow("Artist", chart.artist)
                if (!chart.artistRomanized.equals(chart.artist, ignoreCase = true)) {
                    DetailRow("Romanised artist", chart.artistRomanized)
                }
                DetailRow("Category", chart.category)
                DetailRow("BPM", chart.bpm?.toString() ?: "Unknown")
                DetailRow(
                    "Released",
                    listOfNotNull(chart.releaseDate, chart.songVersion.takeIf(String::isNotBlank))
                        .joinToString("  ·  "),
                )
                chart.unlockInfo.forEach { info ->
                    UnlockDetailInfo(
                        info = info,
                        onOpenGuide = info.guideEntryId?.let { entryId ->
                            { onOpenGuide(entryId) }
                        },
                        onOpenSource = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.sourceUrl)))
                        },
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Text("Chart", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(color = difficultyColor(chart.difficulty), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            chart.displayDifficulty,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Text(
                        "${chart.level ?: "?"} (${chart.constant?.let { String.format(Locale.US, "%.1f", it) } ?: "unknown"})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                    Text(chart.displayType)
                }
                DetailRow("Chart version", chart.chartVersion.orEmpty())
                DetailRow("Notes designer", chart.noteDesigner.orEmpty())
                if (!chart.noteDesignerRomanized.equals(chart.noteDesigner, ignoreCase = true)) {
                    DetailRow("Romanised designer", chart.noteDesignerRomanized)
                }
                DetailRow("Regions", chart.regions.codes().joinToString { regionLabel(it) })
                Spacer(Modifier.height(8.dp))
                NoteCountTable(chart)
                if (score != null) {
                    ScoreSection(chart, score, playDetail, state)
                } else {
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    Text("My score", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Sign in to DX NET to import this chart's achievement, rank, and medals.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                chart.comment?.takeIf(String::isNotBlank)?.let {
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    Text(it)
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        val query = Uri.encode("${chart.title} maimai ${chart.displayDifficulty}")
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Search on YouTube")
                }
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun ScoreSection(
    chart: SongChart,
    score: UserScore,
    playDetail: PlayDetail?,
    state: CatalogUiState,
) {
    HorizontalDivider(Modifier.padding(vertical = 12.dp))
    Text("My score", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            String.format(Locale.US, "%.4f%%", score.achievement),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
        )
        MedalPill(score.grade.label, MaterialTheme.colorScheme.primaryContainer)
        if (score.comboMedal != ComboMedal.NONE) MedalPill(score.comboMedal.label, Color(0xFFFFD9E4))
        if (score.syncMedal != SyncMedal.NONE) MedalPill(score.syncMedal.label, Color(0xFFD4E8FF))
    }
    DetailRow(
        "DX score",
        dxScoreText(score) + if (score.dxStarCount > 0) "  ${"★".repeat(score.dxStarCount)}" else "",
    )
    DetailRow("Chart rating", RatingCalculator.chartRating(chart, score)?.toString() ?: "Unknown constant")

    val bestBreakJudgments = playDetail?.judgments?.breakNotes
    val milestones = remember(chart.chartKey, score, state.scores, bestBreakJudgments) {
        RatingCalculator.milestones(
            chart = chart,
            score = score,
            charts = state.allCharts,
            scores = state.scores,
            newVersions = state.newVersions,
            breakJudgments = bestBreakJudgments,
        )
    }
    if (milestones.isNotEmpty() && chart.constant != null) {
        Spacer(Modifier.height(12.dp))
        Text("Next rating milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "AP keeps the Critical Perfect/Perfect Break split from the imported detailed play. " +
                "The range covers every Perfect Break being in the near or far timing window.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        milestones.forEach { milestone ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Text(milestone.label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(achievementRangeText(milestone.minimumAchievement, milestone.maximumAchievement))
                }
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        "Chart Rt " + integerRangeText(
                            milestone.minimumChartRating,
                            milestone.maximumChartRating,
                        ),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        signedRatingRangeText(
                            milestone.minimumTotalChange,
                            milestone.maximumTotalChange,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
    if (playDetail != null) {
        JudgmentDetail(chart, playDetail)
    } else {
        Spacer(Modifier.height(12.dp))
        Text("Judgement breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "DX NET exposes detailed judgements for the 50 most recent plays. Play this chart and import again to attach a breakdown.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun achievementRangeText(minimum: Double, maximum: Double): String =
    if (kotlin.math.abs(maximum - minimum) < 0.00005) {
        String.format(Locale.US, "%.4f%%", minimum)
    } else {
        String.format(Locale.US, "%.4f–%.4f%%", minimum, maximum)
    }

private fun integerRangeText(minimum: Int?, maximum: Int?): String = when {
    minimum == null || maximum == null -> "—"
    minimum == maximum -> minimum.toString()
    else -> "$minimum–$maximum"
}

private fun signedRatingRangeText(minimum: Int, maximum: Int): String {
    fun signed(value: Int) = if (value >= 0) "+$value" else value.toString()
    return if (minimum == maximum) {
        "${signed(minimum)} total rating"
    } else {
        "${signed(minimum)}–${signed(maximum)} total rating"
    }
}

@Composable
private fun JudgmentDetail(chart: SongChart, detail: PlayDetail) {
    val losses = AchievementLossCalculator
        .groupedJudgmentLosses(chart.noteCounts, detail.judgments, detail.achievement)
        .associateBy { it.noteType }
    Spacer(Modifier.height(14.dp))
    Text("Latest detailed play", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text(
        "${detail.playedAt.ifBlank { "Recent play" }}  ·  " +
            String.format(Locale.US, "%.4f%%", detail.achievement) +
            "  ·  FAST ${detail.fast} / LATE ${detail.late}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
    ) {
        Row(Modifier.padding(vertical = 5.dp)) {
            TableCell("Note", 38, true)
            TableCell("CP", 30, true)
            TableCell("P", 30, true)
            TableCell("Gr", 62, true)
            TableCell("Go", 62, true)
            TableCell("M", 62, true)
        }
        JudgeRow("Tap", detail.judgments.tap, losses["Tap"])
        JudgeRow("Hold", detail.judgments.hold, losses["Hold"])
        JudgeRow("Slide", detail.judgments.slide, losses["Slide"])
        JudgeRow("Touch", detail.judgments.touch, losses["Touch"])
        JudgeRow("Break", detail.judgments.breakNotes, losses["Break"])
    }
}

@Composable
private fun JudgeRow(
    name: String,
    counts: JudgeCounts,
    losses: dev.thomas.maidex.rating.JudgmentRowLoss?,
) {
    Row(Modifier.padding(vertical = 3.dp)) {
        TableCell(name, 38, true)
        TableCell(counts.criticalPerfect.toString(), 30)
        TableCell(counts.perfect.toString(), 30)
        TableCell(judgmentCountText(counts.great, losses?.great), 62)
        TableCell(judgmentCountText(counts.good, losses?.good), 62)
        TableCell(judgmentCountText(counts.miss, losses?.miss), 62)
    }
}

private fun judgmentCountText(
    count: Int,
    loss: dev.thomas.maidex.rating.LossRange?,
): String {
    if (count == 0 || loss == null) return count.toString()
    return if (kotlin.math.abs(loss.maximum - loss.minimum) < 0.00005) {
        String.format(Locale.US, "%d%n(-%.2f%%)", count, loss.minimum)
    } else {
        String.format(Locale.US, "%d%n(-%.2f–%.2f%%)", count, loss.minimum, loss.maximum)
    }
}

@Composable
private fun TableCell(value: String, width: Int, bold: Boolean = false) {
    Text(
        value,
        modifier = Modifier.width(width.dp),
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            label,
            modifier = Modifier.width(136.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NoteCountTable(chart: SongChart) {
    val counts = chart.noteCounts
    val values = listOf(
        "Tap" to counts.tap,
        "Hold" to counts.hold,
        "Slide" to counts.slide,
        "Touch" to counts.touch,
        "Break" to counts.breakNotes,
        "Total" to counts.total,
    )
    Text("Note counts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(2) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(3) { columnIndex ->
                    val (name, count) = values[rowIndex * 3 + columnIndex]
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(name, style = MaterialTheme.typography.labelSmall)
                            Text(count?.toString() ?: "—", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

internal fun titleWithRomanization(original: String, romanized: String): String {
    if (original.isBlank()) return romanized
    if (romanized.isBlank() || romanized.equals(original, ignoreCase = true)) return original
    var offset = 0
    while (offset < original.length) {
        val codePoint = original.codePointAt(offset)
        if (
            Character.isLetter(codePoint) &&
            Character.UnicodeScript.of(codePoint) != Character.UnicodeScript.LATIN
        ) {
            return "$original ($romanized)"
        }
        offset += Character.charCount(codePoint)
    }
    return original
}

private fun difficultyLabel(value: String): String = when (value) {
    "basic" -> "BASIC"
    "advanced" -> "ADVANCED"
    "expert" -> "EXPERT"
    "master" -> "MASTER"
    "remaster" -> "Re:MASTER"
    else -> value
}

private fun typeLabel(value: String): String = when (value) {
    "dx" -> "DX"
    "std" -> "STD"
    "utage" -> "宴"
    else -> value
}

private fun regionLabel(value: String): String = when (value) {
    "jp" -> "Japan"
    "intl" -> "International"
    "usa" -> "USA"
    "cn" -> "China"
    else -> value
}

private fun difficultyColor(value: String): Color = when (value) {
    "basic" -> Color(0xFF189B4E)
    "advanced" -> Color(0xFFD87300)
    "expert" -> Color(0xFFD72F48)
    "master" -> Color(0xFF7D35BD)
    "remaster" -> Color(0xFF9D50D0)
    else -> Color(0xFF52636F)
}
