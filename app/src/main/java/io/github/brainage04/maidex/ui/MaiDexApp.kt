package io.github.brainage04.maidex.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ReportDrawnWhen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.brainage04.maidex.BuildConfig
import io.github.brainage04.maidex.DxNetLoginActivity
import io.github.brainage04.maidex.CatalogUiState
import io.github.brainage04.maidex.ImportStatus
import io.github.brainage04.maidex.MainViewModel
import io.github.brainage04.maidex.R
import io.github.brainage04.maidex.data.AccountRegion
import io.github.brainage04.maidex.data.BestScore
import io.github.brainage04.maidex.data.BestScoreMetric
import io.github.brainage04.maidex.data.CatalogInfo
import io.github.brainage04.maidex.data.DanCourse
import io.github.brainage04.maidex.data.DanCourseGroup
import io.github.brainage04.maidex.data.DanCourseMetadata
import io.github.brainage04.maidex.data.DanTrack
import io.github.brainage04.maidex.data.ChartFilters
import io.github.brainage04.maidex.data.ChartSort
import io.github.brainage04.maidex.data.ComboMedal
import io.github.brainage04.maidex.data.ClassBattleMetadata
import io.github.brainage04.maidex.data.ClassBattleMilestone
import io.github.brainage04.maidex.data.FilterOptions
import io.github.brainage04.maidex.data.FilterPreset
import io.github.brainage04.maidex.data.CircleDailySnapshot
import io.github.brainage04.maidex.data.CircleData
import io.github.brainage04.maidex.data.CirclePageInfo
import io.github.brainage04.maidex.data.CirclePageType
import io.github.brainage04.maidex.data.MedalLevelTable
import io.github.brainage04.maidex.data.SongChart
import io.github.brainage04.maidex.data.SongUnlockInfo
import io.github.brainage04.maidex.data.DxNetAssets
import io.github.brainage04.maidex.data.UnlockGuideEntry
import io.github.brainage04.maidex.data.UnlockGuideSection
import io.github.brainage04.maidex.data.UnlockMetadata
import io.github.brainage04.maidex.data.ConstantAvailability
import io.github.brainage04.maidex.data.Grade
import io.github.brainage04.maidex.data.JudgeCounts
import io.github.brainage04.maidex.data.PlayDetail
import io.github.brainage04.maidex.data.PlayCountSnapshot
import io.github.brainage04.maidex.data.PlayerProfile
import io.github.brainage04.maidex.data.ScoreAnalytics
import io.github.brainage04.maidex.data.ScoreAnalyticsSummary
import java.util.Locale
import io.github.brainage04.maidex.data.SortOrder
import io.github.brainage04.maidex.data.SyncMedal
import io.github.brainage04.maidex.data.TrackingSettings
import io.github.brainage04.maidex.data.UserScore
import io.github.brainage04.maidex.data.normalizeSearch
import io.github.brainage04.maidex.rating.AchievementLossCalculator
import io.github.brainage04.maidex.rating.RatingCalculator
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaiDexApp(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filterPresets by viewModel.filterPresets.collectAsStateWithLifecycle()
    ReportDrawnWhen { !state.isLoading }
    var showFilters by remember { mutableStateOf(false) }
    var selectedChart by remember { mutableStateOf<SongChart?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    var unlockGuideSection by remember { mutableStateOf<UnlockGuideSection?>(null) }
    var showDanGuide by remember { mutableStateOf(false) }
    var showScoreStats by remember { mutableStateOf(false) }
    var showCircle by remember { mutableStateOf(false) }
    var selectedUnlockGuideEntryId by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawerAndOpen: (() -> Unit) -> Unit = { open ->
        scope.launch {
            drawerState.close()
            open()
        }
    }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                    AppDrawerContent(
                        profile = state.profile,
                        onClose = { scope.launch { drawerState.close() } },
                        onAccount = { closeDrawerAndOpen { showAccount = true } },
                        onCircle = { closeDrawerAndOpen { showCircle = true } },
                        onScoreStats = { closeDrawerAndOpen { showScoreStats = true } },
                        onDanGuide = { closeDrawerAndOpen { showDanGuide = true } },
                        onChihoGuide = {
                            closeDrawerAndOpen {
                                selectedUnlockGuideEntryId = null
                                unlockGuideSection = UnlockGuideSection.CHIHOS
                            }
                        },
                        onClassBattleGuide = {
                            closeDrawerAndOpen {
                                selectedUnlockGuideEntryId = null
                                unlockGuideSection = UnlockGuideSection.CLASS_BATTLES
                            }
                        },
                        onAbout = { closeDrawerAndOpen { showAbout = true } },
                    )
            },
        ) {
                Scaffold(
                    contentWindowInsets = WindowInsets(0),
                    topBar = {
                        CenterAlignedTopAppBar(
                            modifier = Modifier.statusBarsPadding(),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                            ),
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Open navigation menu")
                                }
                            },
                            title = {
                                Text(
                                    "MaiDex",
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                )
                            },
                            actions = {
                                if (state.info != null) {
                                    Text(
                                        "${state.visibleSongCount} songs\n${state.charts.size} charts",
                                        modifier = Modifier.padding(end = 12.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.End,
                                        maxLines = 2,
                                    )
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
                            onOpenFilters = { showFilters = true },
                            onClearFilters = viewModel::clearFilters,
                            onChart = { selectedChart = it },
                            onUnlockGuide = { entryId ->
                                selectedUnlockGuideEntryId = entryId
                                unlockGuideSection = UnlockMetadata.guideEntry(entryId)?.section
                            },
                        )
                    }
                }
        }

    if (showFilters) {
        FilterDialog(
            filters = state.filters,
            sort = state.sort,
            sortOrder = state.sortOrder,
            options = state.options,
            presets = filterPresets,
            hasScores = state.scores.isNotEmpty(),
            onDismiss = { showFilters = false },
            onApply = { appliedFilters, appliedSort, appliedSortOrder ->
                viewModel.applyFilters(appliedFilters, appliedSort, appliedSortOrder)
                showFilters = false
            },
            onSavePreset = viewModel::saveFilterPreset,
            onDeletePreset = viewModel::deleteFilterPreset,
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
                unlockGuideSection = UnlockMetadata.guideEntry(entryId)?.section
            },
        )
    }
    if (showAccount) {
        AccountDialog(
            profile = state.profile,
            importStatus = state.importStatus,
            playCountSnapshots = state.playCountSnapshots,
            onImportData = viewModel::importData,
            onClear = viewModel::clearAccount,
            onDismissStatus = viewModel::dismissImportStatus,
            onDismiss = { showAccount = false },
        )
    }
    if (showCircle) {
        CircleDialog(
            history = state.circleHistory,
            snapshots = state.circleSnapshots,
            trackingSettings = state.trackingSettings,
            importStatus = state.importStatus,
            onSync = {
                viewModel.importData(state.profile?.region ?: AccountRegion.INTERNATIONAL)
            },
            onDismiss = { showCircle = false },
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
    unlockGuideSection?.let { section ->
        UnlockGuideDialog(
            section = section,
            initialEntryId = selectedUnlockGuideEntryId,
            charts = state.allCharts,
            scores = state.scores,
            profile = state.profile,
            onDismiss = {
                unlockGuideSection = null
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
private fun AppDrawerContent(
    profile: PlayerProfile?,
    onClose: () -> Unit,
    onAccount: () -> Unit,
    onCircle: () -> Unit,
    onScoreStats: () -> Unit,
    onDanGuide: () -> Unit,
    onChihoGuide: () -> Unit,
    onClassBattleGuide: () -> Unit,
    onAbout: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(min = 280.dp, max = 360.dp),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 16.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "MaiDex",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close navigation menu")
            }
        }
        Text(
            profile?.name ?: "Offline catalog",
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        HorizontalDivider(Modifier.padding(vertical = 16.dp))
        NavigationDrawerItem(
            label = { Text("DX NET account") },
            selected = false,
            onClick = onAccount,
            icon = {
                if (profile?.avatarUrl.isNullOrBlank()) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                } else {
                    DxNetAvatar(profile = profile, modifier = Modifier.size(28.dp))
                }
            },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("Circle data") },
            selected = false,
            onClick = onCircle,
            icon = { Icon(Icons.Default.Groups, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("Score stats") },
            selected = false,
            onClick = onScoreStats,
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            label = { Text("Dan courses") },
            selected = false,
            onClick = onDanGuide,
            icon = { Icon(Icons.Default.MilitaryTech, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("Chiho guides") },
            selected = false,
            onClick = onChihoGuide,
            icon = { Icon(Icons.Default.Map, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("Class Battle guides") },
            selected = false,
            onClick = onClassBattleGuide,
            icon = { Icon(Icons.Default.Groups, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Spacer(Modifier.weight(1f))
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            label = { Text("About MaiDex") },
            selected = false,
            onClick = onAbout,
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
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
                    body = "MaiDex starts with a bundled catalog, then checks arcade-songs every hour. " +
                        "A changed source is rebuilt into a validated local database and becomes active " +
                        "on the next app launch; the current catalog always remains available offline.",
                )
                InfoSection(
                    title = "Catalog freshness",
                    body = "arcade-songs does not publish a fixed update schedule. " +
                        if ((info?.lastCheckedAt ?: 0L) > 0L) {
                            "MaiDex last checked the published dataset " +
                                "${formatTrackedTime(info?.lastCheckedAt ?: 0L)}."
                        } else {
                            "MaiDex will check the published dataset when Android next runs the hourly updater."
                        },
                )
                InfoSection(
                    title = "DX NET and privacy",
                    body = "Sign-in happens on the official DX NET site. Imported scores, recent-play " +
                        "details, circle history, play-count snapshots, cookies, and cached artwork stay " +
                        "in app-private storage on this device. MaiDex does not store your SEGA ID password.",
                )
                InfoSection(
                    title = "Hourly DX NET tracking",
                    body = "MaiDex asks Android to refresh circle, member, profile, and play-count data once " +
                        "per hour while the saved session remains valid. Android may defer exact timing for " +
                        "battery or network reasons; DX NET cannot backfill unavailable history.",
                )
                InfoSection(
                    title = "Images and matching",
                    body = "Cover art is cached after viewing, and profile/circle artwork is cached after import. " +
                        "DX NET scores are matched by song, chart type, difficulty, and level. A source change " +
                        "is downloaded hourly and used after the next app launch.",
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
    onOpenFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onChart: (SongChart) -> Unit,
    onUnlockGuide: (String) -> Unit,
) {
    var searchField by remember {
        mutableStateOf(
            TextFieldValue(
                text = state.filters.search,
                selection = TextRange(state.filters.search.length),
            ),
        )
    }
    LaunchedEffect(state.filters.search) {
        if (state.filters.search != searchField.text) {
            searchField = TextFieldValue(
                text = state.filters.search,
                selection = TextRange(state.filters.search.length),
            )
        }
    }
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchField,
            onValueChange = { value ->
                searchField = value
                onSearch(value.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 4.dp, end = 12.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchField.text.isNotEmpty()) {
                    IconButton(onClick = {
                        searchField = TextFieldValue("")
                        onSearch("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            label = { Text("Search") },
            placeholder = { Text("Title, romaji, artist, or notes designer") },
            shape = RoundedCornerShape(6.dp),
        )
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 40.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SortMenu(
                        selected = state.sort,
                        onSelect = onSort,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                    )
                    OutlinedButton(
                        onClick = onToggleSortOrder,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                    ) {
                        Box(Modifier.fillMaxWidth()) {
                            Icon(
                                if (state.sortOrder == SortOrder.ASCENDING) {
                                    Icons.Default.ArrowUpward
                                } else {
                                    Icons.Default.ArrowDownward
                                },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.CenterStart),
                            )
                            Text(
                                state.sortOrder.label,
                                modifier = Modifier.align(Alignment.Center),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(44.dp),
                    ) {
                        OutlinedButton(
                            onClick = onOpenFilters,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filters",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        if (state.filters.activeCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp),
                            ) {
                                Text(
                                    state.filters.activeCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
                if (state.filters.activeCount > 0) {
                    TextButton(
                        onClick = onClearFilters,
                        modifier = Modifier.align(Alignment.End),
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
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
        ) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    selected.label,
                    modifier = Modifier.align(Alignment.Center),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterEnd),
                )
            }
        }
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
    val accentColor = difficultyColor(chart.difficulty)
    val typeIconUrl = DxNetAssets.chartTypeIconUrl(chart.displayType)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFB)),
        border = BorderStroke(3.dp, accentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (typeIconUrl.isNotBlank()) {
                    AsyncImage(
                        model = typeIconUrl,
                        contentDescription = "${chart.displayType} chart",
                        modifier = Modifier
                            .width(54.dp)
                            .height(22.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Text(
                        chart.displayType,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    listOfNotNull(
                        chart.chartVersion,
                        chart.bpm?.let { "$it BPM" },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF17344E),
                    textAlign = TextAlign.End,
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = Color.White,
                    border = BorderStroke(3.dp, accentColor),
                    shadowElevation = 2.dp,
                ) {
                    AsyncImage(
                        model = chart.imageUrl,
                        contentDescription = "Cover art for ${chart.title}",
                        modifier = Modifier.size(156.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = accentColor,
                shape = RoundedCornerShape(4.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        chart.displayDifficulty,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                    Text(
                        buildString {
                            append("Lv")
                            append(chart.level ?: "?")
                            chart.constant?.let {
                                append(" · C")
                                append(String.format(Locale.US, "%.1f", it))
                            }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF102E4B),
                shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
            ) {
                SongTitleIdentity(
                    chart = chart,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF173F62),
                shape = RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp),
            ) {
                Text(
                    titleWithRomanization(chart.artist, chart.artistRomanized),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
            if (score != null) {
                DxNetScorePanel(chart = chart, score = score)
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFEEF1F3),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, Color(0xFFAEB9C0)),
                ) {
                    Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                        Text(
                            "No imported DX NET score",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF32434D),
                        )
                        chart.noteDesigner?.takeIf(String::isNotBlank)?.let { designer ->
                            Text(
                                "NOTES DESIGNER · " +
                                    titleWithRomanization(designer, chart.noteDesignerRomanized),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4E5E67),
                            )
                        }
                    }
                }
            }
            chart.unlockInfo.forEach { info ->
                UnlockTileInfo(
                    info = info,
                    onOpenGuide = info.guideEntryId?.let { entryId ->
                        { onUnlockGuide(entryId) }
                    },
                )
            }
        }
    }
}

@Composable
private fun DxNetScorePanel(
    chart: SongChart,
    score: UserScore,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scorePanelColor(score.grade),
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color(0xFF8D999F)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    String.format(Locale.US, "%.4f%%", score.achievement),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF172126),
                )
                AsyncImage(
                    model = DxNetAssets.gradeIconUrl(score.grade),
                    contentDescription = "${score.grade.label} rank",
                    modifier = Modifier
                        .width(52.dp)
                        .height(24.dp),
                    contentScale = ContentScale.Fit,
                )
                if (score.comboMedal != ComboMedal.NONE) {
                    AsyncImage(
                        model = DxNetAssets.comboIconUrl(score.comboMedal),
                        contentDescription = score.comboMedal.label,
                        modifier = Modifier
                            .width(32.dp)
                            .height(27.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                if (score.syncMedal != SyncMedal.NONE) {
                    AsyncImage(
                        model = DxNetAssets.syncIconUrl(score.syncMedal),
                        contentDescription = score.syncMedal.label,
                        modifier = Modifier
                            .width(32.dp)
                            .height(27.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            DxScoreLine(score = score)
            HorizontalDivider(color = Color.Black.copy(alpha = 0.16f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "NOTES DESIGNER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF16804A),
                    )
                    Text(
                        titleWithRomanization(
                            chart.noteDesigner.orEmpty(),
                            chart.noteDesignerRomanized,
                        ).ifBlank { "—" },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF26373F),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    RatingCalculator.chartRating(chart, score)?.let {
                        Text(
                            "Rt $it",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF172126),
                        )
                    }
                    chart.bpm?.let {
                        Text(
                            "BPM $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4D5A61),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongTitleIdentity(
    chart: SongChart,
    prominent: Boolean = false,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = songIdentityTitle(
            original = chart.title,
            romanized = chart.titleRomanized,
            meaning = chart.titleMeaning,
        ),
        modifier = modifier,
        style = if (prominent) {
            MaterialTheme.typography.headlineSmall
        } else {
            MaterialTheme.typography.titleMedium
        },
        fontWeight = if (prominent) FontWeight.Black else FontWeight.Bold,
        color = color,
        textAlign = textAlign,
    )
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
private fun DxScoreLine(
    score: UserScore,
    modifier: Modifier = Modifier,
) {
    val percentage = score.dxScorePercentage
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = Color(0xFF169B55),
                shape = RoundedCornerShape(3.dp),
            ) {
                Text(
                    "DX SCORE",
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
            }
            Text(
                "${score.dxScore} / ${score.maxDxScore}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF26373F),
            )
            DxStarImage(score.dxStarCount)
        }
        percentage?.let {
            Text(
                "${String.format(Locale.US, "%.2f", it)}% of maximum DX score",
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4D5A61),
            )
        }
    }
}

@Composable
private fun DxStarImage(stars: Int) {
    val resourceId = when (stars) {
        1 -> R.drawable.dxstar_1
        2 -> R.drawable.dxstar_2
        3 -> R.drawable.dxstar_3
        4 -> R.drawable.dxstar_4
        5 -> R.drawable.dxstar_5
        else -> return
    }
    Image(
        painter = painterResource(resourceId),
        contentDescription = "$stars DX stars",
        modifier = Modifier
            .width(94.dp)
            .height(16.dp),
        contentScale = ContentScale.Fit,
    )
}



@Composable
private fun DanGuideDialog(
    chartLookup: Map<String, SongChart>,
    initialRegion: AccountRegion,
    onDismiss: () -> Unit,
    onChart: (SongChart) -> Unit,
) {
    val context = LocalContext.current
    var region by remember(initialRegion) { mutableStateOf(initialRegion) }
    var version by remember { mutableStateOf(DanCourseMetadata.version) }
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
                    Text(
                        "Dan courses",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        LabeledDropdown(
                            label = "Version",
                            value = version,
                            options = listOf(DanCourseMetadata.version),
                            optionLabel = { it },
                            onSelect = { version = it },
                        )
                        LabeledDropdown(
                            label = "Region",
                            value = region,
                            options = AccountRegion.entries,
                            optionLabel = AccountRegion::label,
                            onSelect = { region = it },
                        )
                        LabeledDropdown(
                            label = "Dan Type",
                            value = group,
                            options = DanCourseGroup.entries,
                            optionLabel = { option ->
                                when (option) {
                                    DanCourseGroup.NORMAL -> "Dan 1–10"
                                    DanCourseGroup.TRUE -> "Shin Dan"
                                    DanCourseGroup.URA -> "Ura Kaiden"
                                }
                            },
                            onSelect = { group = it },
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
private fun <T> LabeledDropdown(
    label: String,
    value: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    optionLabel(value),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
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
    section: UnlockGuideSection,
    initialEntryId: String?,
    charts: List<SongChart>,
    scores: Map<String, UserScore>,
    profile: PlayerProfile?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val initialEntry = remember(initialEntryId, section) {
        initialEntryId
            ?.let(UnlockMetadata::guideEntry)
            ?.takeIf { it.section == section }
    }
    val romanizedTitles = remember(charts) {
        charts.asSequence().associate { it.sourceSongId to it.titleRomanized }
    }
    val (bestScoresBySong, bestScoresBySongAndLevel) = remember(charts, scores) {
        val bySong = mutableMapOf<String, UserScore>()
        val bySongAndLevel = mutableMapOf<String, UserScore>()
        charts.forEach { chart ->
            val score = scores[chart.chartKey] ?: return@forEach
            setOf(chart.sourceSongId, chart.title).forEach { title ->
                val songKey = unlockProgressSongKey(title)
                if ((bySong[songKey]?.achievement ?: -1.0) < score.achievement) {
                    bySong[songKey] = score
                }
                val milestoneKey = classBattleScoreKey(title, chart.level.orEmpty())
                if ((bySongAndLevel[milestoneKey]?.achievement ?: -1.0) < score.achievement) {
                    bySongAndLevel[milestoneKey] = score
                }
            }
        }
        bySong to bySongAndLevel
    }
    val entries = UnlockMetadata.guideEntries.filter { it.section == section }
    val orderedEntries = if (initialEntry != null) {
        listOf(initialEntry) + entries.filterNot { it.id == initialEntry.id }
    } else {
        entries
    }
    val currentClassVersion = ClassBattleMetadata.byVersion.keys.firstOrNull()

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
                    Text(
                        if (section == UnlockGuideSection.CHIHOS) {
                            "Chiho guides"
                        } else {
                            "Class Battle guides"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(onClick = onDismiss) { Text("Close") }
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
                            isCurrentClassVersion = entry.title == currentClassVersion,
                            romanizedTitles = romanizedTitles,
                            bestScoresBySong = bestScoresBySong,
                            bestScoresBySongAndLevel = bestScoresBySongAndLevel,
                            profile = profile,
                            onOpenSource = {
                                val sourceUrl = if (entry.section == UnlockGuideSection.CLASS_BATTLES) {
                                    ClassBattleMetadata.sourceUrlForVersion(entry.title)
                                } else {
                                    entry.sourceUrl
                                }
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)))
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
    isCurrentClassVersion: Boolean,
    romanizedTitles: Map<String, String>,
    bestScoresBySong: Map<String, UserScore>,
    bestScoresBySongAndLevel: Map<String, UserScore>,
    profile: PlayerProfile?,
    onOpenSource: () -> Unit,
) {
    val isChiho = entry.section == UnlockGuideSection.CHIHOS
    val milestones = ClassBattleMetadata.byVersion[entry.title].orEmpty()
    var showMilestones by remember(entry.id) { mutableStateOf(isLinkedEntry) }
    val completedChiho = isChiho && chihoIsCompleted(
        title = entry.title,
        romanized = entry.titleRomanized,
        completedNames = profile?.completedChihoNames.orEmpty(),
    )
    val playedUnlockSongs = entry.songs.count { song ->
        bestScoresBySong[unlockProgressSongKey(song.title)] != null
    }
    val clearedClasses = if (isCurrentClassVersion) {
        classBattleClearedCount(milestones, profile?.friendClass.orEmpty())
    } else {
        0
    }
    val milestoneScores = milestones.count { milestone ->
        bestScoresBySongAndLevel[
            classBattleScoreKey(milestone.songTitle, milestone.level)
        ] != null || bestScoresBySong[unlockProgressSongKey(milestone.songTitle)] != null
    }

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
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.68f),
                shape = RoundedCornerShape(10.dp),
            ) {
                Column(
                    modifier = Modifier.padding(9.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = when {
                                completedChiho -> Icons.Default.CheckCircle
                                isCurrentClassVersion && clearedClasses == milestones.size && milestones.isNotEmpty() ->
                                    Icons.Default.CheckCircle
                                else -> Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (
                                completedChiho ||
                                isCurrentClassVersion && clearedClasses == milestones.size && milestones.isNotEmpty()
                            ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                        )
                        Text(
                            text = when {
                                isChiho && completedChiho -> "DX NET map complete"
                                isChiho -> "$playedUnlockSongs/${entry.songs.size} unlock songs played · map not confirmed complete"
                                isCurrentClassVersion && profile?.friendClass.equals("LEGEND", ignoreCase = true) ->
                                    "LEGEND · all ${milestones.size} classes cleared"
                                isCurrentClassVersion && profile?.friendClass.isNullOrBlank() ->
                                    "Sync DX NET to track the current class"
                                isCurrentClassVersion ->
                                    "$clearedClasses/${milestones.size} classes cleared · current ${profile?.friendClass}"
                                else ->
                                    "Historical season · best scores imported for $milestoneScores/${milestones.size} bosses"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    val progress = when {
                        isChiho && completedChiho -> 1f
                        isChiho && entry.songs.isNotEmpty() ->
                            playedUnlockSongs.toFloat() / entry.songs.size
                        isCurrentClassVersion && milestones.isNotEmpty() ->
                            clearedClasses.toFloat() / milestones.size
                        milestones.isNotEmpty() -> milestoneScores.toFloat() / milestones.size
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
            entry.songs.forEach { song ->
                val played = bestScoresBySong[unlockProgressSongKey(song.title)] != null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = if (played) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(17.dp),
                        tint = if (played) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            titleWithRomanization(song.title, romanizedTitles[song.title].orEmpty()),
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            listOfNotNull(
                                song.requirement.takeIf(String::isNotBlank),
                                "Played".takeIf { played },
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (!isChiho && milestones.isNotEmpty()) {
                TextButton(
                    onClick = { showMilestones = !showMilestones },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (showMilestones) {
                            "Hide ${milestones.size} class milestones"
                        } else {
                            "Show ${milestones.size} class milestones"
                        },
                    )
                }
                if (showMilestones) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        milestones.forEachIndexed { index, milestone ->
                            ClassBattleMilestoneRow(
                                milestone = milestone,
                                score = bestScoresBySongAndLevel[
                                    classBattleScoreKey(milestone.songTitle, milestone.level)
                                ] ?: bestScoresBySong[unlockProgressSongKey(milestone.songTitle)],
                                cleared = isCurrentClassVersion && index < clearedClasses,
                                current = isCurrentClassVersion &&
                                    index == clearedClasses &&
                                    !profile?.friendClass.equals("LEGEND", ignoreCase = true),
                            )
                        }
                    }
                }
            }
            TextButton(onClick = onOpenSource, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (isChiho) "Source" else "Boss source")
            }
        }
    }
}

@Composable
private fun ClassBattleMilestoneRow(
    milestone: ClassBattleMilestone,
    score: UserScore?,
    cleared: Boolean,
    current: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = when {
            cleared -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            current -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f)
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        shape = RoundedCornerShape(9.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = if (cleared) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(17.dp),
                tint = if (cleared) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        milestone.className,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "Lv ${milestone.level} · strength ${milestone.opponentStrength}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                    )
                }
                Text(
                    milestone.songTitle,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    when {
                        cleared -> "Cleared on DX NET"
                        current -> score?.let {
                            "Current class · best ${String.format(Locale.US, "%.4f%%", it.achievement)}"
                        } ?: "Current class"
                        score != null -> "Imported best ${String.format(Locale.US, "%.4f%%", score.achievement)}"
                        else -> "No imported best score"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterDialog(
    filters: ChartFilters,
    sort: ChartSort,
    sortOrder: SortOrder,
    options: FilterOptions,
    presets: List<FilterPreset>,
    hasScores: Boolean,
    onDismiss: () -> Unit,
    onApply: (ChartFilters, ChartSort, SortOrder) -> Unit,
    onSavePreset: (String, ChartFilters, ChartSort, SortOrder) -> Unit,
    onDeletePreset: (String) -> Unit,
) {
    var draft by remember(filters) { mutableStateOf(filters) }
    var minLevel by remember(filters) { mutableStateOf(filters.minLevel?.toString().orEmpty()) }
    var maxLevel by remember(filters) { mutableStateOf(filters.maxLevel?.toString().orEmpty()) }
    var minBpm by remember(filters) { mutableStateOf(filters.minBpm?.toString().orEmpty()) }
    var maxBpm by remember(filters) { mutableStateOf(filters.maxBpm?.toString().orEmpty()) }
    var draftSort by remember(sort) { mutableStateOf(sort) }
    var draftSortOrder by remember(sortOrder) { mutableStateOf(sortOrder) }
    var showSavePreset by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }
    val normalizedDraft = {
        draft.copy(
            minLevel = minLevel.toDoubleOrNull(),
            maxLevel = maxLevel.toDoubleOrNull(),
            minBpm = minBpm.toIntOrNull(),
            maxBpm = maxBpm.toIntOrNull(),
        )
    }
    val applyPreset: (FilterPreset) -> Unit = { preset ->
        draft = preset.filters.copy(search = filters.search)
        minLevel = preset.filters.minLevel?.toString().orEmpty()
        maxLevel = preset.filters.maxLevel?.toString().orEmpty()
        minBpm = preset.filters.minBpm?.toString().orEmpty()
        maxBpm = preset.filters.maxBpm?.toString().orEmpty()
        draftSort = preset.sort
        draftSortOrder = preset.sortOrder
    }

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
                        draftSort = ChartSort.LEVEL
                        draftSortOrder = SortOrder.DESCENDING
                    }) { Text("Reset") }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "presets") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Presets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            FilterPresetMenu(
                                presets = presets,
                                onSelect = applyPreset,
                                onDelete = onDeletePreset,
                            )
                            if (showSavePreset) {
                                OutlinedTextField(
                                    value = presetName,
                                    onValueChange = { presetName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Preset name") },
                                    singleLine = true,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(onClick = {
                                        showSavePreset = false
                                        presetName = ""
                                    }) { Text("Cancel") }
                                    Button(
                                        enabled = presetName.isNotBlank(),
                                        onClick = {
                                            onSavePreset(
                                                presetName,
                                                normalizedDraft(),
                                                draftSort,
                                                draftSortOrder,
                                            )
                                            showSavePreset = false
                                            presetName = ""
                                        },
                                    ) { Text("Save") }
                                }
                            } else {
                                TextButton(onClick = { showSavePreset = true }) {
                                    Text("Save current as preset")
                                }
                            }
                        }
                    }
                    item(key = "preset-divider") { HorizontalDivider() }
                    item(key = "sort") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LabeledDropdown(
                                label = "Sort",
                                value = draftSort,
                                options = ChartSort.entries,
                                optionLabel = ChartSort::label,
                                onSelect = { draftSort = it },
                            )
                            LabeledDropdown(
                                label = "Sort order",
                                value = draftSortOrder,
                                options = SortOrder.entries,
                                optionLabel = SortOrder::label,
                                onSelect = { draftSortOrder = it },
                            )
                        }
                    }
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
                    item(key = "show-utage") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Show UTAGE charts")
                                Text(
                                    "Hide banquet charts from catalog results.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = draft.showUtage,
                                onCheckedChange = { draft = draft.copy(showUtage = it) },
                            )
                        }
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
                        onApply(normalizedDraft(), draftSort, draftSortOrder)
                    }) { Text("Show results") }
                }
            }
        }
    }
}

@Composable
private fun FilterPresetMenu(
    presets: List<FilterPreset>,
    onSelect: (FilterPreset) -> Unit,
    onDelete: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
        ) {
            Text(
                "Apply preset",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            presets.forEach { preset ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(preset.name)
                            Text(
                                "${preset.sort.label} · ${preset.sortOrder.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = {
                        onSelect(preset)
                        expanded = false
                    },
                    trailingIcon = if (preset.isBuiltIn) {
                        null
                    } else {
                        {
                            IconButton(onClick = { onDelete(preset.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete ${preset.name}",
                                )
                            }
                        }
                    },
                )
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
    playCountSnapshots: List<PlayCountSnapshot>,
    onImportData: (AccountRegion) -> Unit,
    onClear: () -> Unit,
    onDismissStatus: () -> Unit,
    onDismiss: () -> Unit,
) {
    var region by remember(profile) { mutableStateOf(profile?.region ?: AccountRegion.INTERNATIONAL) }
    var showProfileCloseUp by remember(profile) { mutableStateOf(false) }
    var showPlayCountHistory by remember(profile) { mutableStateOf(false) }
    val context = LocalContext.current
    val isImporting = importStatus is ImportStatus.Running
    val loginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onImportData(region)
    }

    Dialog(
        onDismissRequest = {
            onDismissStatus()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "maimai DX NET",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                    )
                    TextButton(onClick = {
                        onDismissStatus()
                        onDismiss()
                    }) { Text("Close") }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
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
                    DxNetPlayCounts(profile)
                    if (playCountSnapshots.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${playCountSnapshots.size} daily play-count snapshot" +
                                    if (playCountSnapshots.size == 1) "" else "s",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = { showPlayCountHistory = true }) {
                                Text("View history")
                            }
                        }
                    }
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
                                    "Sign in to import your profile, scores, play history, and circle data.",
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
                            "Sign-in opens the official DX NET site full screen. MaiDex automatically imports " +
                                "your profile, scores, play history, and circle data after sign-in succeeds.",
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
                        onClick = { loginLauncher.launch(DxNetLoginActivity.intent(context, region)) },
                        enabled = !isImporting,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Sign in", maxLines = 1)
                    }
                    Button(
                        onClick = { onImportData(region) },
                        enabled = !isImporting,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Import data", maxLines = 1)
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
    if (showPlayCountHistory) {
        PlayCountHistoryDialog(
            snapshots = playCountSnapshots,
            onDismiss = { showPlayCountHistory = false },
        )
    }
}

private val DxCircleSky = Color(0xFF55BCE9)
private val DxCircleBlue = Color(0xFF197CC5)
private val DxCircleBlueDark = Color(0xFF075A9E)
private val DxCirclePink = Color(0xFFF34BA7)
private val DxCirclePinkLight = Color(0xFFFFD6EB)
private val DxCircleInk = Color(0xFF263238)

@Composable
private fun CircleDialog(
    history: List<CircleData>,
    snapshots: List<CircleDailySnapshot>,
    trackingSettings: TrackingSettings,
    importStatus: ImportStatus,
    onSync: () -> Unit,
    onDismiss: () -> Unit,
) {
    val current = history.firstOrNull()
    val otherMembers = remember(snapshots) {
        snapshots.asSequence()
            .flatMap { it.circle.members.asSequence() }
            .filterNot { it.isCurrentUser }
            .distinctBy { it.key }
            .sortedByDescending { member ->
                snapshots.lastOrNull()?.circle?.members
                    ?.firstOrNull { it.key == member.key }
                    ?.points ?: member.points
            }
            .toList()
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(24.dp),
            color = DxCircleSky,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DxCircleBlueDark)
                        .padding(start = 16.dp, top = 10.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            current?.name ?: "Circle",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                        )
                        Text(
                            if (current == null) {
                                "Sign in to import circle data"
                            } else {
                                current.month
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f),
                        )
                    }
                    TextButton(onClick = onDismiss) { Text("Close", color = Color.White) }
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
                    item(key = "tracking") {
                        CircleTrackingCard(
                            settings = trackingSettings,
                            isSyncing = importStatus is ImportStatus.Running,
                            onSync = onSync,
                        )
                    }
                    if (current == null) {
                        item(key = "empty") {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, DxCircleBlueDark),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    "No circle information has been captured yet. Sign in to DX NET or use " +
                                        "Sync now. Monthly history starts with the first successful circle sync.",
                                    modifier = Modifier.padding(14.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        item(key = "overview") {
                            CircleOverviewCard(current)
                        }
                        val structuredPages = current.pages.filter { page ->
                            page.type != CirclePageType.OTHER &&
                                page.type != CirclePageType.PROFILE
                        }
                        if (structuredPages.isNotEmpty()) {
                            item(key = "details-heading") {
                                CircleWebTitle("Official circle pages")
                            }
                            items(
                                structuredPages,
                                key = { page -> "page-${page.type}-${page.title}" },
                            ) { page ->
                                CirclePageCard(page, current)
                            }
                        }
                        if (snapshots.isNotEmpty()) {
                            item(key = "history-heading") {
                                CircleSectionHeading(
                                    "Daily point tracking",
                                    "${snapshots.size} daily snapshot" +
                                        if (snapshots.size == 1) "" else "s",
                                )
                            }
                            item(key = "circle-trend") {
                                PointTrendCard(
                                    title = "Circle total",
                                    points = snapshots.map { it.day to it.circle.totalPoints },
                                )
                            }
                            val currentUserPoints = snapshots.mapNotNull { snapshot ->
                                snapshot.circle.members
                                    .firstOrNull { it.isCurrentUser }
                                    ?.let { snapshot.day to it.points }
                            }
                            if (currentUserPoints.isNotEmpty()) {
                                item(key = "current-user-trend") {
                                    PointTrendCard(
                                        title = snapshots.last().circle.members
                                            .firstOrNull { it.isCurrentUser }
                                            ?.name
                                            ?.let { "$it (you)" }
                                            ?: "Your contribution",
                                        points = currentUserPoints,
                                    )
                                }
                            }
                            items(otherMembers, key = { member -> "trend-${member.key}" }) { member ->
                                val memberPoints = snapshots.mapNotNull { snapshot ->
                                    snapshot.circle.members
                                        .firstOrNull { it.key == member.key }
                                        ?.let { snapshot.day to it.points }
                                }
                                PointTrendCard(member.name, memberPoints)
                            }
                        } else {
                            item(key = "tracking-empty") {
                                Text(
                                    "Point trends will appear after the first successful hourly sync.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        item(key = "months-heading") {
                            CircleSectionHeading(
                                "Monthly history",
                                "History starts when MaiDex first syncs each month",
                            )
                        }
                        items(history, key = { circle -> "month-${circle.month}-${circle.key}" }) { circle ->
                            CircleMonthCard(circle, initiallyExpanded = circle === current)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleWebTitle(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DxCircleBlue,
        shape = RoundedCornerShape(50),
        border = BorderStroke(2.dp, Color.White),
        shadowElevation = 2.dp,
    ) {
        Text(
            title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CircleOfficialTitle(type: CirclePageType, title: String) {
    val imageUrl = DxNetAssets.circleTitleImageUrl(type)
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            contentScale = ContentScale.Fit,
        )
    } else {
        CircleWebTitle(title)
    }
}

@Composable
private fun CircleMetricPill(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(50),
        border = BorderStroke(2.dp, DxCirclePink),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, color = DxCirclePink, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = DxCircleInk)
        }
    }
}

@Composable
private fun CircleTrackingCard(
    settings: TrackingSettings,
    isSyncing: Boolean,
    onSync: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DxCircleBlueDark),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircleWebTitle("Circle data sync")
            val status = when {
                isSyncing -> "Syncing now…"
                settings.lastError.isNotBlank() ->
                    "Last sync failed: ${settings.lastError}"
                settings.lastSuccessAt > 0L ->
                    "Last synced ${formatTrackedTime(settings.lastSuccessAt)}"
                else -> "Not synced yet"
            }
            Text(
                status,
                style = MaterialTheme.typography.bodyMedium,
                color = if (settings.lastError.isBlank() || isSyncing) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            Button(
                onClick = onSync,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DxCirclePink,
                    contentColor = Color.White,
                    disabledContainerColor = DxCirclePinkLight,
                    disabledContentColor = DxCircleInk.copy(alpha = 0.55f),
                ),
            ) {
                Text(
                    if (isSyncing) "Syncing…" else "Sync now",
                    fontWeight = FontWeight.Black,
                )
            }
            Text(
                "Automatic sync remains scheduled hourly. Android may defer a run because of battery " +
                    "or network restrictions.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CircleOverviewCard(circle: CircleData) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DxCircleBlueDark),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            CircleOfficialTitle(CirclePageType.PROFILE, "Circle profile")
            CircleProfileVisual(circle)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleMetricPill(
                    value = "${formatCount(circle.totalPoints)} PT",
                    label = "Circle total points",
                    modifier = Modifier.weight(1f),
                )
                CircleMetricPill(
                    value = circle.regionalRank?.let { "Rank ${formatCount(it)}" } ?: "—",
                    label = circle.rankingLabel.ifBlank { "Current ranking" },
                    modifier = Modifier.weight(1f),
                )
            }
            circle.code.takeIf(String::isNotBlank)?.let { CircleInfoRow("Circle code", it) }
            circle.leader.takeIf(String::isNotBlank)?.let { CircleInfoRow("Leader", it) }
            circle.circleClass.takeIf(String::isNotBlank)?.let { CircleInfoRow("Circle class", it) }
            circle.memberCount?.let { CircleInfoRow("Members", it.toString()) }
            circle.daysUntilReset?.let { CircleInfoRow("Days until reset", it.toString()) }
            circle.nextRewardPoints?.let { CircleInfoRow("Points until next reward", formatCount(it)) }
            circle.updatedAt.takeIf(String::isNotBlank)?.let { CircleInfoRow("DX NET updated", it) }
            if (circle.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    circle.tags.forEach { tag ->
                        Surface(
                            color = DxCirclePinkLight,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, DxCirclePink),
                        ) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleProfileVisual(circle: CircleData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DxCirclePinkLight),
    ) {
        circle.backgroundUrl.takeIf(String::isNotBlank)?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Circle background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        circle.characterUrl.takeIf(String::isNotBlank)?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Circle character",
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .padding(4.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp),
            color = Color.White.copy(alpha = 0.94f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(2.dp, DxCirclePink),
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, DxCirclePink),
                ) {
                    if (circle.profileImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = circle.profileImageUrl,
                            contentDescription = "${circle.name} profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        circle.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        circle.comment.ifBlank { "No circle note" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleMonthCard(circle: CircleData, initiallyExpanded: Boolean) {
    var expanded by remember(circle.month, circle.key) { mutableStateOf(initiallyExpanded) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DxCircleBlueDark),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(circle.month, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${formatCount(circle.totalPoints)} PT" +
                            (circle.regionalRank?.let { " · regional rank #${formatCount(it)}" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    if (expanded) "Collapse" else "Expand",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Rewards", fontWeight = FontWeight.Bold)
                    val earnedRewards = circle.rewards.filter { it.earned }
                    if (earnedRewards.isEmpty()) {
                        Text(
                            "No received reward details were available in this sync.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        earnedRewards.forEach { reward ->
                            CircleInfoRow("${formatCount(reward.pointsRequired)} PT", reward.name)
                        }
                    }
                    Text("Member contributions", fontWeight = FontWeight.Bold)
                    if (circle.members.isEmpty()) {
                        Text(
                            "DX NET did not expose member point details on the imported pages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        circle.members.sortedByDescending { it.points }.forEachIndexed { index, member ->
                            CircleInfoRow(
                                "${index + 1}. ${member.name}${if (member.isCurrentUser) " (you)" else ""}",
                                "${formatCount(member.points)} PT",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CirclePageCard(page: CirclePageInfo, circle: CircleData) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        CircleOfficialTitle(page.type, page.title)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, DxCircleBlueDark),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (page.type) {
                    CirclePageType.PROFILE -> CircleProfileVisual(circle)
                    CirclePageType.POINT_REWARD -> CircleRewardPage(circle)
                    CirclePageType.MEMBER -> CircleMemberPage(circle)
                    else -> CircleStructuredPage(page)
                }
            }
        }
    }
}

@Composable
private fun CircleRewardPage(circle: CircleData) {
    if (circle.rewards.isEmpty()) {
        CirclePageEmptyState("No point rewards were listed by DX NET.")
        return
    }
    circle.rewards.sortedBy { reward -> reward.pointsRequired }.forEach { reward ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, DxCircleInk),
        ) {
            Row(
                modifier = Modifier.padding(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (reward.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = reward.imageUrl,
                        contentDescription = reward.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.width(9.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(reward.name, fontWeight = FontWeight.Bold)
                    Text(
                        "${formatCount(reward.pointsRequired)} PT",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (reward.earned) {
                    Surface(
                        color = Color(0xFFFFC72C),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            "GET",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleMemberPage(circle: CircleData) {
    if (circle.members.isEmpty()) {
        CirclePageEmptyState("DX NET did not list any circle members.")
        return
    }
    circle.members.sortedByDescending { member -> member.points }.forEachIndexed { index, member ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (member.isCurrentUser) DxCirclePinkLight else Color(0xFFE2F5FC),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, DxCircleBlueDark),
        ) {
            Row(
                modifier = Modifier.padding(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "#${index + 1}",
                    modifier = Modifier.width(34.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                )
                if (member.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = member.avatarUrl,
                        contentDescription = member.name,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(9.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        member.name + if (member.isCurrentUser) " (you)" else "",
                        fontWeight = FontWeight.Bold,
                    )
                    if (member.role.isNotBlank()) {
                        Text(
                            member.role,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Surface(
                    color = DxCirclePink,
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        "${formatCount(member.points)} PT",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleStructuredPage(page: CirclePageInfo) {
    val heroImage = page.imageUrls.firstOrNull { image ->
        page.items.none { item -> item.imageUrl == image }
    }
    heroImage?.let { image ->
        AsyncImage(
            model = image,
            contentDescription = page.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Fit,
        )
    }
    if (page.items.isEmpty()) {
        val message = page.text.takeIf(String::isNotBlank) ?: when (page.type) {
            CirclePageType.SEARCH ->
                "Enter a circle code or browse recruiting circles on DX NET."
            CirclePageType.INVITE_ACCEPT ->
                "Circles that have invited you will be displayed here."
            CirclePageType.FESTA,
            CirclePageType.FESTA_RANKING,
            -> "Outside Circle Festa period. Please wait for the next event."
            CirclePageType.CHALLENGE_RANKING ->
                "No Circle Challenge ranking is available yet."
            CirclePageType.RANKING -> "No circle ranking entries are available."
            CirclePageType.LEAVE -> "Leave the circle?"
            else -> "No information is available for this page."
        }
        CirclePageEmptyState(message)
        return
    }
    page.items.take(100).forEachIndexed { index, item ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, DxCircleInk),
        ) {
            Row(
                modifier = Modifier.padding(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (
                    page.type == CirclePageType.RANKING ||
                    page.type == CirclePageType.CHALLENGE_RANKING
                ) {
                    Text(
                        "#${index + 1}",
                        modifier = Modifier.width(34.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.label,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.width(9.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(item.label, fontWeight = FontWeight.Bold)
                    if (item.value.isNotBlank()) {
                        Text(
                            item.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CirclePageEmptyState(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(2.dp, DxCircleBlue),
    ) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CircleSectionHeading(title: String, subtitle: String) {
    Column(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CircleWebTitle(title)
        Text(
            subtitle,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = DxCircleInk,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CircleInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PointTrendCard(title: String, points: List<Pair<String, Int>>) {
    if (points.isEmpty()) return
    val values = points.map { it.second }
    val first = values.first()
    val latest = values.last()
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DxCircleBlueDark),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(
                    formatCount(latest),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = DxCirclePink,
                )
                if (latest != first) {
                    Text(
                        "  ${if (latest > first) "+" else ""}${formatCount(latest - first)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(top = 8.dp),
            ) {
                val minimum = values.minOrNull()?.toFloat() ?: 0f
                val maximum = values.maxOrNull()?.toFloat() ?: minimum
                val range = (maximum - minimum).takeIf { it > 0f } ?: 1f
                val step = if (values.size > 1) size.width / values.lastIndex else 0f
                fun point(index: Int): androidx.compose.ui.geometry.Offset {
                    val x = if (values.size == 1) size.width / 2f else index * step
                    val y = size.height - ((values[index] - minimum) / range * size.height)
                    return androidx.compose.ui.geometry.Offset(x, y)
                }
                values.indices.drop(1).forEach { index ->
                    drawLine(
                        color = Color(0xFF087BA8),
                        start = point(index - 1),
                        end = point(index),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                values.indices.forEach { index ->
                    drawCircle(
                        color = Color(0xFF087BA8),
                        radius = 2.5.dp.toPx(),
                        center = point(index),
                    )
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Text(
                    points.first().first,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    points.last().first,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlayCountHistoryDialog(
    snapshots: List<PlayCountSnapshot>,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Play-count history",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "${snapshots.size} daily snapshot" + if (snapshots.size == 1) "" else "s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                if (snapshots.isEmpty()) {
                    Text("No play-count snapshots have been captured yet.")
                } else {
                    PointTrendCard(
                        title = "Current version play count",
                        points = snapshots.map { it.day to it.currentVersionPlayCount },
                    )
                    PointTrendCard(
                        title = "maimaiDX total play count",
                        points = snapshots.map { it.day to it.totalPlayCount },
                    )
                }
            }
        }
    }
}

private fun formatCount(value: Int): String = String.format(Locale.US, "%,d", value)

private fun formatTrackedTime(timestamp: Long): String = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm", Locale.US)
    .withZone(ZoneId.systemDefault())
    .format(Instant.ofEpochMilli(timestamp))

internal class MedalTableExpansionState {
    private val values = mutableStateMapOf<String, Boolean>()

    operator fun get(title: String): Boolean = values[title] ?: true

    operator fun set(title: String, expanded: Boolean) {
        values[title] = expanded
    }
}

@Composable
internal fun ScoreStatsDialog(
    charts: List<SongChart>,
    scores: Map<String, UserScore>,
    onDismiss: () -> Unit,
    onChart: (SongChart) -> Unit,
) {
    val summary = remember(charts, scores) { ScoreAnalytics.summarize(charts, scores) }
    val expandedTables = remember { MedalTableExpansionState() }
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
                            "${summary.playedCharts} matched scores",
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
                    summary.levelTables.forEachIndexed { index, table ->
                        item(key = "standard-levels-$index") {
                            MedalTableCard(
                                table = table,
                                expanded = expandedTables[table.title],
                                onExpandedChange = { expandedTables[table.title] = it },
                            )
                        }
                    }
                    item(key = "utage-levels") {
                        MedalTableCard(
                            table = summary.utage,
                            expanded = expandedTables[summary.utage.title],
                            onExpandedChange = { expandedTables[summary.utage.title] = it },
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
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    val fitsWidth = table.levels.size <= 6
    val scrollState = rememberScrollState()
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(vertical = 6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    table.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (expanded) "Collapse" else "Expand",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (expanded) {
                val tableModifier = if (fitsWidth) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.horizontalScroll(scrollState)
                }
                Column(
                    modifier = tableModifier.padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                    Row(
                        modifier = if (fitsWidth) Modifier.fillMaxWidth() else Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(Modifier.width(48.dp))
                        table.levels.forEach { level ->
                            Text(
                                level,
                                modifier = if (fitsWidth) Modifier.weight(1f) else Modifier.width(36.dp),
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
                                .then(if (fitsWidth) Modifier.fillMaxWidth() else Modifier)
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
                                    modifier = if (fitsWidth) Modifier.weight(1f) else Modifier.width(36.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                }
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
    var rotated by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val naturalWidth = if (rotated) {
                (maxHeight * 0.86f).coerceAtMost(720.dp)
            } else {
                (maxWidth * 0.96f).coerceAtMost(720.dp)
            }
            val naturalHeight = naturalWidth / PROFILE_CARD_ASPECT_RATIO
            DxNetProfileCard(
                profile = profile,
                modifier = Modifier
                    .requiredWidth(naturalWidth)
                    .requiredHeight(naturalHeight)
                    .graphicsLayer(rotationZ = if (rotated) 90f else 0f),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(50),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { rotated = !rotated }) {
                        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text(if (rotated) "Return card" else "Rotate · larger")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

private const val PROFILE_CARD_ASPECT_RATIO = 450f / 132f

@Composable
private fun DxNetProfileCard(
    profile: PlayerProfile,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(PROFILE_CARD_ASPECT_RATIO),
    ) {
        val scale = (maxWidth.value / 450f).coerceIn(0.68f, 1.8f)
        val nameFontSize = 16.sp * scale
        val starFontSize = 14.sp * scale
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(6.dp * scale),
            color = Color(0xFFD9F3FC),
            border = BorderStroke(1.dp, Color(0xFF8ED0EA)),
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp * scale),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(112.dp * scale),
                    shape = RoundedCornerShape(4.dp * scale),
                    color = Color(0xFFE5F6FD),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp * scale),
                            tint = Color(0xFF178AB6),
                        )
                        if (profile.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = profile.avatarUrl,
                                contentDescription = "${profile.name}'s DX NET icon",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp * scale))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    DxNetTitleBadge(
                        title = profile.title.ifBlank { "DX NET profile" },
                        imageUrl = profile.titleBackgroundUrl,
                        scale = scale,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp * scale),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(3.dp * scale),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFB8C3C9)),
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp * scale),
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
                        Spacer(Modifier.width(7.dp * scale))
                        DxRatingBadge(
                            rating = profile.officialRating,
                            imageUrl = profile.ratingBaseUrl,
                            scale = scale,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp * scale),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        profile.courseRankUrl.takeIf(String::isNotBlank)?.let {
                            DxNetRankImage(
                                url = it,
                                description = "Course rank",
                                modifier = Modifier.size(
                                    width = 90.dp * scale,
                                    height = 35.dp * scale,
                                ),
                            )
                        }
                        profile.classRankUrl.takeIf(String::isNotBlank)?.let {
                            Spacer(Modifier.width(10.dp * scale))
                            DxNetRankImage(
                                url = it,
                                description = "Class rank",
                                modifier = Modifier.size(
                                    width = 63.dp * scale,
                                    height = 35.dp * scale,
                                ),
                            )
                        }
                        Spacer(Modifier.width(10.dp * scale))
                        profile.starCount?.let { stars ->
                            AsyncImage(
                                model = profile.starIconUrl,
                                contentDescription = "DX star",
                                modifier = Modifier.size(25.dp * scale),
                                contentScale = ContentScale.Fit,
                            )
                            Spacer(Modifier.width(3.dp * scale))
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

@Composable
private fun DxNetPlayCounts(profile: PlayerProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.End,
    ) {
        profile.currentVersionPlayCount?.let { count ->
            Text(
                "play count of current version : ${formatCount(count)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        profile.totalPlayCount?.let { count ->
            Text(
                "maimaiDX total play count : ${formatCount(count)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DxNetTitleBadge(title: String, imageUrl: String, scale: Float) {
    val fontSize = 13.sp * scale
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp * scale),
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
                drawStyle = Stroke(width = 2f * scale),
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
private fun DxRatingBadge(rating: Int, imageUrl: String, scale: Float) {
    val fontSize = 16.sp * scale
    Box(
        modifier = Modifier
            .width(92.dp * scale)
            .height(28.dp * scale),
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
            modifier = Modifier.padding(end = 8.dp * scale),
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            lineHeight = fontSize,
            letterSpacing = 0.sp,
            textAlign = TextAlign.End,
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
    Spacer(Modifier.height(8.dp))
    DxNetScorePanel(chart = chart, score = score)

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
    losses: io.github.brainage04.maidex.rating.JudgmentRowLoss?,
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
    loss: io.github.brainage04.maidex.rating.LossRange?,
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

internal fun songIdentityTitle(
    original: String,
    romanized: String,
    meaning: String,
): String {
    val title = original.trim().ifBlank { romanized.trim() }
    if (!containsNonLatinText(title)) return title
    val reading = romanized.trim().takeUnless { it.equals(title, ignoreCase = true) }.orEmpty()
    val translation = meaning.trim()
    if (reading.isBlank() && translation.isBlank()) return title
    return buildString {
        append(title)
        append(" (")
        if (reading.isNotBlank()) {
            append(reading)
        }
        if (translation.isNotBlank()) {
            if (reading.isNotBlank()) append(" → ")
            append('"')
            append(translation)
            append('"')
        }
        append(')')
    }
}

internal fun chihoIsCompleted(
    title: String,
    romanized: String,
    completedNames: Set<String>,
): Boolean {
    val expected = setOf(title, romanized)
        .map(::chihoProgressKey)
        .filter(String::isNotBlank)
        .toSet()
    return completedNames
        .asSequence()
        .map(::chihoProgressKey)
        .any(expected::contains)
}

private fun chihoProgressKey(value: String): String =
    normalizeSearch(value)
        .removeSuffix("ちほー")
        .removeSuffix("area")

private fun unlockProgressSongKey(value: String): String =
    normalizeSearch(value)
        .removeSuffix("dx")
        .removeSuffix("std")

private fun classBattleScoreKey(
    title: String,
    level: String,
): String = "${unlockProgressSongKey(title)}|${normalizeSearch(level)}"

internal fun classBattleClearedCount(
    milestones: List<ClassBattleMilestone>,
    friendClass: String,
): Int {
    val normalizedClass = friendClass
        .uppercase(Locale.ROOT)
        .filter(Char::isLetterOrDigit)
    if (normalizedClass == "LEGEND") return milestones.size
    val exactIndex = milestones.indexOfFirst { it.className == normalizedClass }
    if (exactIndex >= 0) return exactIndex
    val tierPattern = when (normalizedClass) {
        "A" -> Regex("""A[1-5]""")
        "S" -> Regex("""S[1-5]""")
        "SS" -> Regex("""SS[1-5]""")
        "SSS" -> Regex("""SSS[1-5]""")
        else -> return 0
    }
    return milestones.indexOfFirst { tierPattern.matches(it.className) }.coerceAtLeast(0)
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



private fun scorePanelColor(grade: Grade): Color = when (grade) {
    Grade.SSS_PLUS,
    Grade.SSS,
    -> Color(0xFFFFF1C7)
    Grade.SS_PLUS,
    Grade.SS,
    -> Color(0xFFDDEBFF)
    Grade.S_PLUS,
    Grade.S,
    -> Color(0xFFFFE1E8)
    else -> Color.White.copy(alpha = 0.9f)
}
