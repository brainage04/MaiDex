package dev.thomas.maidex.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
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
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.thomas.maidex.BuildConfig
import dev.thomas.maidex.DxNetLoginActivity
import dev.thomas.maidex.CatalogUiState
import dev.thomas.maidex.ImportStatus
import dev.thomas.maidex.MainViewModel
import dev.thomas.maidex.data.AccountRegion
import dev.thomas.maidex.data.BestScore
import dev.thomas.maidex.data.BestScoreMetric
import dev.thomas.maidex.data.CatalogInfo
import dev.thomas.maidex.data.DanCourse
import dev.thomas.maidex.data.DanCourseGroup
import dev.thomas.maidex.data.DanCourseMetadata
import dev.thomas.maidex.data.DanTrack
import dev.thomas.maidex.data.ChartFilters
import dev.thomas.maidex.data.ChartSort
import dev.thomas.maidex.data.ComboMedal
import dev.thomas.maidex.data.FilterOptions
import dev.thomas.maidex.data.MedalLevelTable
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
import dev.thomas.maidex.data.ScoreAnalytics
import dev.thomas.maidex.data.ScoreAnalyticsSummary
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
    ReportDrawnWhen { !state.isLoading }
    var showFilters by remember { mutableStateOf(false) }
    var selectedChart by remember { mutableStateOf<SongChart?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    var showUnlockGuide by remember { mutableStateOf(false) }
    var showDanGuide by remember { mutableStateOf(false) }
    var showScoreStats by remember { mutableStateOf(false) }
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
                    IconButton(onClick = { showDanGuide = true }) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = "Dan course guides")
                    }
                    IconButton(onClick = {
                        selectedUnlockGuideEntryId = null
                        showUnlockGuide = true
                    }) {
                        Icon(Icons.Default.Map, contentDescription = "Chiho and class battle guides")
                    }
                    IconButton(onClick = { showAccount = true }) {
                        val profile = state.profile
                        if (profile?.avatarUrl.isNullOrBlank()) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "DX NET account")
                        } else {
                            DxNetAvatar(
                                profile = profile,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                    IconButton(onClick = { showAbout = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = { showFilters = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Open filters")
                        }
                        if (state.filters.activeCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(x = (-2).dp, y = (-6).dp)
                                    .size(20.dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Text(
                                    state.filters.activeCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                )
                            }
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
            hasScores = state.scores.isNotEmpty(),
            onImport = viewModel::importAccount,
            onClear = viewModel::clearAccount,
            onDismissStatus = viewModel::dismissImportStatus,
            onScoreStats = {
                showAccount = false
                showScoreStats = true
            },
            onDismiss = { showAccount = false },
        )
    }
    if (showScoreStats) {
        ScoreStatsDialog(
            charts = state.allCharts,
            scores = state.scores,
            onDismiss = { showScoreStats = false },
            onChart = { chart ->
                showScoreStats = false
                selectedChart = chart
            },
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
    if (showDanGuide) {
        val danChartLookup = remember(state.allCharts) {
            DanCourseMetadata.chartLookup(state.allCharts)
        }
        DanGuideDialog(
            chartLookup = danChartLookup,
            initialRegion = state.profile?.region ?: AccountRegion.INTERNATIONAL,
            onDismiss = { showDanGuide = false },
            onChart = { chart ->
                showDanGuide = false
                selectedChart = chart
            },
        )
    }
    if (showAbout) {
        InfoDialog(
            info = state.info,
            onDismiss = { showAbout = false },
        )
    }
}

@Composable
private fun InfoDialog(
    info: CatalogInfo?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Info")
                Text(
                    "MaiDex ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfoMetric(
                        value = (info?.songCount ?: 0).toString(),
                        label = "Songs",
                        modifier = Modifier.weight(1f),
                    )
                    InfoMetric(
                        value = (info?.chartCount ?: 0).toString(),
                        label = "Charts",
                        modifier = Modifier.weight(1f),
                    )
                }
                InfoSection(
                    title = "Offline catalog",
                    body = "Search, filters, chart constants, note counts, unlock guides, and Dan courses " +
                        "are bundled with the app and remain available without a connection.",
                )
                InfoSection(
                    title = "Metadata",
                    body = "Song data comes from arcade-songs and was updated " +
                        "${info?.updateTime?.take(10).orEmpty().ifBlank { "with this build" }}. " +
                        "Community romanisations and aliases improve title and artist search.",
                )
                InfoSection(
                    title = "DX NET and privacy",
                    body = "Sign-in happens on the official DX NET site. Imported scores, recent-play " +
                        "details, cookies, and cached profile artwork stay in app-private storage on this " +
                        "device. MaiDex does not store your SEGA ID password.",
                )
                InfoSection(
                    title = "Images and matching",
                    body = "Cover art is cached after viewing, and profile artwork is cached after Refresh. " +
                        "DX NET scores are matched by song, chart type, difficulty, and level. A newly added " +
                        "or renamed chart may remain unmatched until the offline catalog is updated.",
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://arcade-songs.zetaraku.dev/maimai/"),
                        ),
                    )
                },
            ) { Text("Data source") }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun InfoMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun InfoSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                .padding(start = 12.dp, top = 4.dp, end = 12.dp),
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
        )
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 40.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
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
        }
        Spacer(Modifier.height(2.dp))
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
                SongTitleIdentity(chart)
                Text(
                    titleWithRomanization(chart.artist, chart.artistRomanized),
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
private fun SongTitleIdentity(
    chart: SongChart,
    prominent: Boolean = false,
) {
    val titleStyle = if (prominent) {
        MaterialTheme.typography.headlineSmall
    } else {
        MaterialTheme.typography.titleMedium
    }
    if (!containsNonLatinText(chart.title)) {
        Text(
            chart.title.ifBlank { chart.titleRomanized },
            style = titleStyle,
            fontWeight = if (prominent) FontWeight.Black else FontWeight.Bold,
        )
        return
    }
    val labelWidth = if (prominent) 76.dp else 58.dp
    val metadataStyle = if (prominent) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodySmall
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        TitleIdentityLine(
            label = "Japanese",
            value = chart.title,
            labelWidth = labelWidth,
            valueStyle = titleStyle,
            valueWeight = if (prominent) FontWeight.Black else FontWeight.Bold,
            maxLines = 2,
        )
        TitleIdentityLine(
            label = "Reading",
            value = chart.titleRomanized,
            labelWidth = labelWidth,
            valueStyle = metadataStyle,
            maxLines = 2,
        )
        if (chart.titleMeaning.isNotBlank()) {
            TitleIdentityLine(
                label = "Meaning",
                value = chart.titleMeaning,
                labelWidth = labelWidth,
                valueStyle = metadataStyle,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun TitleIdentityLine(
    label: String,
    value: String,
    labelWidth: androidx.compose.ui.unit.Dp,
    valueStyle: TextStyle,
    valueWeight: FontWeight? = null,
    maxLines: Int,
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            label,
            modifier = Modifier
                .width(labelWidth)
                .padding(top = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            value,
            modifier = Modifier.weight(1f),
            style = valueStyle,
            fontWeight = valueWeight,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DanGuideDialog(
    chartLookup: Map<String, SongChart>,
    initialRegion: AccountRegion,
    onDismiss: () -> Unit,
    onChart: (SongChart) -> Unit,
) {
    val context = LocalContext.current
    var region by remember(initialRegion) { mutableStateOf(initialRegion) }
    var group by remember { mutableStateOf(DanCourseGroup.TRUE) }
    val courses = DanCourseMetadata.courses.filter { it.group == group }

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
                        Text("Dan courses", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "${DanCourseMetadata.version} · 22 courses · 88 charts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(DanCourseMetadata.sourceUrl(region))),
                            )
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Dan course source",
                        )
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountRegion.entries.forEach { option ->
                        FilterChip(
                            selected = region == option,
                            onClick = { region = option },
                            label = { Text(option.label) },
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DanCourseGroup.entries.forEach { option ->
                        FilterChip(
                            selected = group == option,
                            onClick = { group = option },
                            label = {
                                Text(
                                    when (option) {
                                        DanCourseGroup.NORMAL -> "Dan 1–10"
                                        DanCourseGroup.TRUE -> "Shin Dan"
                                        DanCourseGroup.URA -> "Ura Kaiden"
                                    },
                                )
                            },
                        )
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        "Play four fixed charts in order and finish with Life remaining. " +
                            "Normal courses continue at 0 Life; Shin Dan and Ura Kaiden end immediately. " +
                            "Clear Tenth Dan → Shin Dan; Shin Tenth → Shin Kaiden → Ura Kaiden.",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(courses, key = DanCourse::id) { course ->
                        DanCourseCard(
                            course = course,
                            region = region,
                            chartLookup = chartLookup,
                            onChart = onChart,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DanCourseCard(
    course: DanCourse,
    region: AccountRegion,
    chartLookup: Map<String, SongChart>,
    onChart: (SongChart) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = when (course.group) {
            DanCourseGroup.NORMAL -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            DanCourseGroup.TRUE -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            DanCourseGroup.URA -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
        },
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(course.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(
                        course.nameEnglish,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        "♥ ${course.life.maximum} Life",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Text(
                "Great −${course.life.greatDamage} · Good −${course.life.goodDamage} · " +
                    "Miss −${course.life.missDamage} · Track +${course.life.trackBonus}",
                modifier = Modifier.padding(vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            course.tracks.indices.forEach { index ->
                val track = course.track(index, region)
                val chart = chartLookup[DanCourseMetadata.chartKey(track)]
                DanTrackRow(
                    number = index + 1,
                    track = track,
                    chart = chart,
                    onChart = onChart,
                )
                if (index != course.tracks.lastIndex) {
                    Spacer(Modifier.height(5.dp))
                }
            }
        }
    }
}

@Composable
private fun DanTrackRow(
    number: Int,
    track: DanTrack,
    chart: SongChart?,
    onChart: (SongChart) -> Unit,
) {
    val chartLabel = buildString {
        append(typeLabel(track.type))
        append(" · ")
        append(difficultyLabel(track.difficulty))
        append(' ')
        append(chart?.level ?: "?")
        chart?.constant?.let {
            append(" (")
            append(String.format(Locale.US, "%.1f", it))
            append(')')
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = chart != null) { chart?.let(onChart) },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier.padding(7.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = chart?.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(7.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "TRACK $number",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    titleWithRomanization(track.title, chart?.titleRomanized.orEmpty()),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (chart == null) {
                    Text(
                        "Chart unavailable in this catalog",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Spacer(Modifier.height(3.dp))
                    Surface(
                        color = difficultyColor(track.difficulty).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(7.dp),
                    ) {
                        Text(
                            chartLabel,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = difficultyColor(track.difficulty),
                        )
                    }
                }
            }
        }
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "artist") {
                        OutlinedTextField(
                            value = draft.artist,
                            onValueChange = { draft = draft.copy(artist = it) },
                            label = { Text("Artist") },
                            placeholder = { Text("Original or romanised name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                    item(key = "designer") {
                        OutlinedTextField(
                            value = draft.noteDesigner,
                            onValueChange = { draft = draft.copy(noteDesigner = it) },
                            label = { Text("Notes designer") },
                            placeholder = { Text("Original or romanised name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                    item(key = "level-range") {
                        RangeFields(
                            title = "Level / constant range",
                            minimum = minLevel,
                            maximum = maxLevel,
                            keyboardType = KeyboardType.Decimal,
                            onMinimum = { minLevel = it },
                            onMaximum = { maxLevel = it },
                        )
                    }
                    item(key = "bpm-range") {
                        RangeFields(
                            title = "BPM range",
                            minimum = minBpm,
                            maximum = maxBpm,
                            keyboardType = KeyboardType.Number,
                            onMinimum = { minBpm = it.filter(Char::isDigit) },
                            onMaximum = { maxBpm = it.filter(Char::isDigit) },
                        )
                    }
                    item(key = "constant-availability") {
                        ConstantAvailabilityMenu(
                            selected = draft.constantAvailability,
                            onSelect = { draft = draft.copy(constantAvailability = it) },
                        )
                    }
                    if (hasScores) {
                        item(key = "score-heading") {
                            Text(
                                "My scores",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        item(key = "played-only") {
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
                        }
                        item(key = "rank") {
                            MultiSelectSection(
                                "Rank",
                                Grade.entries,
                                draft.grades,
                                label = Grade::label,
                            ) { draft = draft.copy(grades = it) }
                        }
                        item(key = "achievement-medal") {
                            MultiSelectSection(
                                "Achievement medal",
                                ComboMedal.entries.filterNot { it == ComboMedal.NONE },
                                draft.comboMedals,
                                label = ComboMedal::label,
                            ) { draft = draft.copy(comboMedals = it) }
                        }
                        item(key = "sync-medal") {
                            MultiSelectSection(
                                "Sync medal",
                                SyncMedal.entries.filterNot { it == SyncMedal.NONE },
                                draft.syncMedals,
                                label = SyncMedal::label,
                            ) { draft = draft.copy(syncMedals = it) }
                        }
                        item(key = "score-divider") { HorizontalDivider() }
                    } else {
                        item(key = "score-sign-in") {
                            Text(
                                "Sign in to DX NET to filter by rank, achievement medal, and sync medal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    item(key = "category") {
                        MultiSelectSection("Category", options.categories, draft.categories) {
                            draft = draft.copy(categories = it)
                        }
                    }
                    item(key = "difficulty") {
                        MultiSelectSection(
                            "Difficulty",
                            options.difficulties,
                            draft.difficulties,
                            label = { difficultyLabel(it) },
                        ) { draft = draft.copy(difficulties = it) }
                    }
                    item(key = "type") {
                        MultiSelectSection(
                            "Type",
                            options.types,
                            draft.types,
                            label = { typeLabel(it) },
                        ) { draft = draft.copy(types = it) }
                    }
                    item(key = "region") {
                        MultiSelectSection(
                            "Region",
                            options.regions,
                            draft.regions,
                            label = { regionLabel(it) },
                        ) { draft = draft.copy(regions = it) }
                    }
                    item(key = "version") {
                        MultiSelectSection("Version", options.versions, draft.versions) {
                            draft = draft.copy(versions = it)
                        }
                    }
                    item(key = "bottom-space") { Spacer(Modifier.height(4.dp)) }
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
    hasScores: Boolean,
    importStatus: ImportStatus,
    onImport: (AccountRegion) -> Unit,
    onClear: () -> Unit,
    onDismissStatus: () -> Unit,
    onScoreStats: () -> Unit,
    onDismiss: () -> Unit,
) {
    var region by remember(profile) { mutableStateOf(profile?.region ?: AccountRegion.INTERNATIONAL) }
    var showProfileCloseUp by remember(profile) { mutableStateOf(false) }
    val context = LocalContext.current
    val isImporting = importStatus is ImportStatus.Running

    Dialog(
        onDismissRequest = {
            onDismissStatus()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.88f),
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
                    if (profile != null && hasScores) {
                        IconButton(onClick = onScoreStats) {
                            Icon(Icons.Default.BarChart, contentDescription = "Score stats")
                        }
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
                        modifier = Modifier
                            .padding(top = 6.dp, bottom = 8.dp)
                            .clickable { showProfileCloseUp = true },
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
                    is ImportStatus.Success -> Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            "Imported ${importStatus.imported} scores and " +
                                "${importStatus.recentDetails} recent play details.",
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (importStatus.unmatched > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text(
                                    "${importStatus.unmatched} DX NET score entries could not be matched " +
                                        "to this catalog, so they are not shown. This usually means a chart " +
                                        "was added or renamed before the offline metadata was updated.",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }
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
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            if (profile == null) "Import scores" else "Refresh",
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
    if (profile != null && showProfileCloseUp) {
        ProfileCardCloseUpDialog(
            profile = profile,
            onDismiss = { showProfileCloseUp = false },
        )
    }
}

@Composable
private fun ScoreStatsDialog(
    charts: List<SongChart>,
    scores: Map<String, UserScore>,
    onDismiss: () -> Unit,
    onChart: (SongChart) -> Unit,
) {
    val summary = remember(charts, scores) { ScoreAnalytics.summarize(charts, scores) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 10.dp, end = 8.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Score stats",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "${summary.playedCharts} matched scores · exact best medals",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 10.dp,
                        end = 10.dp,
                        bottom = 18.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(key = "bests") {
                        BestScoresCard(summary, onChart)
                    }
                    item(key = "high-levels") {
                        MedalTableCard(summary.highLevels)
                    }
                    item(key = "lower-levels") {
                        MedalTableCard(summary.lowerLevels)
                    }
                    item(key = "utage-levels") {
                        MedalTableCard(
                            table = summary.utage,
                            note = "UTAGE uses estimated levels from the catalog; * means unrated.",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BestScoresCard(
    summary: ScoreAnalyticsSummary,
    onChart: (SongChart) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text("Personal bests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            summary.bestScores.forEach { best ->
                BestScoreRow(best, onChart)
            }
        }
    }
}

@Composable
private fun BestScoreRow(
    best: BestScore,
    onChart: (SongChart) -> Unit,
) {
    val chart = best.chart
    val score = best.score
    val modifier = if (chart == null) {
        Modifier
    } else {
        Modifier.clickable { onChart(chart) }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            best.metric.label.removePrefix("Best ").uppercase(Locale.ROOT),
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
        )
        if (chart == null || score == null) {
            Text(
                "No matching score",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Row
        }
        AsyncImage(
            model = chart.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(7.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                titleWithRomanization(chart.title, chart.titleRomanized),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "${chart.displayDifficulty} ${chart.level.orEmpty()} · ${bestScoreValue(best.metric, score)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun bestScoreValue(metric: BestScoreMetric, score: UserScore): String =
    if (metric == BestScoreMetric.DX_SCORE) {
        val percentage = score.dxScorePercentage
        if (percentage == null) "${score.dxScore}/${score.maxDxScore}"
        else "${String.format(Locale.US, "%.2f", percentage)}% · ${score.dxScore}/${score.maxDxScore}"
    } else {
        "${String.format(Locale.US, "%.4f", score.achievement)}%"
    }

@Composable
private fun MedalTableCard(
    table: MedalLevelTable,
    note: String? = null,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(vertical = 10.dp)) {
            Text(
                table.title,
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Counts are exact; higher medals are not counted again in lower rows.",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(48.dp))
                    table.levels.forEach { level ->
                        Text(
                            level,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                table.rows.forEachIndexed { index, row ->
                    if (index == 4) Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(medalRowColor(row.label)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            row.label,
                            modifier = Modifier
                                .width(48.dp)
                                .padding(start = 6.dp, top = 4.dp, bottom = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = medalLabelColor(row.label),
                        )
                        row.counts.forEach { count ->
                            Text(
                                count.takeIf { it > 0 }?.toString() ?: "·",
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
            note?.let {
                Text(
                    it,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun medalRowColor(label: String): Color = when (label) {
    "FC", "FC+" -> Color(0xFFE6F1FF)
    "AP", "AP+" -> Color(0xFFFFF0D8)
    "FS", "FS+" -> Color(0xFFE0F7F4)
    else -> Color(0xFFF2E8FF)
}

private fun medalLabelColor(label: String): Color = when (label) {
    "FC", "FC+" -> Color(0xFF086AA7)
    "AP", "AP+" -> Color(0xFFA65A00)
    "FS", "FS+" -> Color(0xFF00796B)
    else -> Color(0xFF6A36A8)
}

@Composable
private fun ProfileCardCloseUpDialog(
    profile: PlayerProfile,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DxNetProfileCard(
                    profile = profile,
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .widthIn(max = 720.dp),
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        "Tap anywhere to close",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.labelLarge,
                    )
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
    val density = LocalDensity.current
    val nameFontSize = with(density) { 12.dp.toSp() }
    val starFontSize = with(density) { 13.dp.toSp() }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2.9f),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFC5ECFA),
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFFCFEFF),
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(1.dp, Color(0xFFC7D1D6)),
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DxNetAvatar(
                        profile = profile,
                        modifier = Modifier.size(84.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        DxNetTitleBadge(
                            title = profile.title.ifBlank { "DX NET profile" },
                            imageUrl = profile.titleBackgroundUrl,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                shape = RoundedCornerShape(4.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFDEDEDE)),
                                shadowElevation = 1.dp,
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 7.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Text(
                                        profile.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = nameFontSize,
                                        lineHeight = nameFontSize,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 0.sp,
                                        color = Color(0xFF111111),
                                    )
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            DxRatingBadge(
                                rating = profile.officialRating,
                                imageUrl = profile.ratingBaseUrl,
                            )
                        }
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp),
                        ) {
                            drawLine(
                                color = Color(0xFFB7B7B7),
                                start = androidx.compose.ui.geometry.Offset.Zero,
                                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(1.dp.toPx(), 3.dp.toPx()),
                                ),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            profile.courseRankUrl.takeIf(String::isNotBlank)?.let {
                                DxNetRankImage(
                                    url = it,
                                    description = "Course rank",
                                    modifier = Modifier.size(width = 63.dp, height = 25.dp),
                                )
                            }
                            profile.classRankUrl.takeIf(String::isNotBlank)?.let {
                                Spacer(Modifier.width(7.dp))
                                DxNetRankImage(
                                    url = it,
                                    description = "Class rank",
                                    modifier = Modifier.size(width = 45.dp, height = 25.dp),
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            profile.starCount?.let { stars ->
                                AsyncImage(
                                    model = profile.starIconUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(width = 23.dp, height = 25.dp),
                                    contentScale = ContentScale.Fit,
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    "×$stars",
                                    maxLines = 1,
                                    fontSize = starFontSize,
                                    lineHeight = starFontSize,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.sp,
                                    color = Color(0xFF202020),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DxNetTitleBadge(title: String, imageUrl: String) {
    val fontSize = with(LocalDensity.current) { 11.dp.toSp() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(21.dp),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Text(
            title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color.Black,
                fontSize = fontSize,
                lineHeight = fontSize,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
                drawStyle = Stroke(width = 2f),
            ),
        )
        Text(
            title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color.White,
                fontSize = fontSize,
                lineHeight = fontSize,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            ),
        )
    }
}

@Composable
private fun DxNetAvatar(profile: PlayerProfile, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(7.dp),
        color = Color(0xFFE5F6FD),
        border = BorderStroke(2.dp, Color(0xFF9DD8F5)),
        shadowElevation = 1.dp,
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
        }
    }
}

@Composable
private fun DxRatingBadge(rating: Int, imageUrl: String) {
    val fontSize = with(LocalDensity.current) { 13.dp.toSp() }
    Box(
        modifier = Modifier
            .width(73.dp)
            .height(21.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Text(
            rating.toString(),
            modifier = Modifier.padding(end = 6.dp),
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            lineHeight = fontSize,
            letterSpacing = 0.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

@Composable
private fun DxNetRankImage(
    url: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = url,
        contentDescription = description,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
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
                SongTitleIdentity(chart, prominent = true)
                Spacer(Modifier.height(12.dp))
                DetailRow("Artist", titleWithRomanization(chart.artist, chart.artistRomanized))
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
                DetailRow(
                    "Notes designer",
                    titleWithRomanization(chart.noteDesigner.orEmpty(), chart.noteDesignerRomanized),
                )
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
    return if (containsNonLatinText(original)) "$original ($romanized)" else original
}

private fun containsNonLatinText(value: String): Boolean {
    var offset = 0
    while (offset < value.length) {
        val codePoint = value.codePointAt(offset)
        if (
            Character.isLetter(codePoint) &&
            Character.UnicodeScript.of(codePoint) != Character.UnicodeScript.LATIN
        ) {
            return true
        }
        offset += Character.charCount(codePoint)
    }
    return false
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
