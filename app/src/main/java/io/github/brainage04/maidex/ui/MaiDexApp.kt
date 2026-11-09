package io.github.brainage04.maidex.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.RadioButton
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.semantics.Role
import io.github.brainage04.maidex.data.ThemeMode
import io.github.brainage04.maidex.ui.theme.MaiDexTheme
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.brainage04.maidex.BuildConfig
import io.github.brainage04.maidex.DxNetLoginActivity
import io.github.brainage04.maidex.CatalogUiState
import io.github.brainage04.maidex.ImportStatus
import io.github.brainage04.maidex.MainViewModel
import io.github.brainage04.maidex.AutoLoginUiState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
    val autoLogin by viewModel.autoLogin.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.autoUpdateMessages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            launch { snackbarHostState.showSnackbar(message, withDismissAction = true) }
        }
    }
    ReportDrawnWhen { !state.isLoading }
    val backStack = rememberSaveable(saver = AppBackStack.Saver) { AppBackStack() }
    val screenStates = rememberSaveableStateHolder()
    // A closed screen forgets its state, so reopening it starts fresh as the old dialogs did.
    val pop: () -> Unit = {
        backStack.top?.let { screenStates.removeState(it.key) }
        backStack.pop()
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openFromDrawer: (AppScreen) -> Unit = { screen ->
        scope.launch {
            drawerState.close()
            backStack.push(screen)
        }
    }
    val openGuideEntry: (String) -> Unit = { entryId ->
        UnlockMetadata.guideEntry(entryId)?.let { entry ->
            backStack.push(AppScreen.UnlockGuide(entry.section, entryId))
        }
    }
    val openChart: (SongChart) -> Unit = { chart -> backStack.push(AppScreen.ChartDetail(chart.chartKey)) }

    Box(Modifier.fillMaxSize()) {
        when (val screen = backStack.top) {
            null -> screenStates.SaveableStateProvider("catalog") {
                // Back closes an open drawer instead of leaving the app.
                BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawerContent(
                            profile = state.profile,
                            onClose = { scope.launch { drawerState.close() } },
                            onOpen = openFromDrawer,
                        )
                    },
                ) {
                    CatalogRoot(
                        state = state,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onRetry = viewModel::loadCatalog,
                        onSearch = viewModel::setSearch,
                        onSort = viewModel::setSort,
                        onToggleSortOrder = viewModel::toggleSortOrder,
                        onOpenFilters = { backStack.push(AppScreen.Filters) },
                        onClearFilters = viewModel::clearFilters,
                        onChart = openChart,
                        onUnlockGuide = openGuideEntry,
                    )
                }
            }

            else -> screenStates.SaveableStateProvider(screen.key) {
                when (screen) {
                    AppScreen.Filters -> FilterScreen(
                        filters = state.filters,
                        sort = state.sort,
                        sortOrder = state.sortOrder,
                        options = state.options,
                        presets = filterPresets,
                        hasScores = state.scores.isNotEmpty(),
                        onBack = pop,
                        onApply = { appliedFilters, appliedSort, appliedSortOrder ->
                            viewModel.applyFilters(appliedFilters, appliedSort, appliedSortOrder)
                            pop()
                        },
                        onSavePreset = viewModel::saveFilterPreset,
                        onDeletePreset = viewModel::deleteFilterPreset,
                    )

                    is AppScreen.ChartDetail -> {
                        val chart = remember(state.allCharts, screen.chartKey) {
                            state.allCharts.firstOrNull { it.chartKey == screen.chartKey }
                        }
                        when {
                            chart != null -> ChartDetailScreen(
                                chart = chart,
                                score = state.scores[chart.chartKey],
                                playDetail = state.playDetails[chart.chartKey],
                                state = state,
                                onBack = pop,
                                onOpenGuide = openGuideEntry,
                            )
                            state.isLoading -> LoadingScreen(onBack = pop)
                            else -> LaunchedEffect(screen) { pop() }
                        }
                    }

                    AppScreen.Account -> AccountScreen(
                        profile = state.profile,
                        importStatus = state.importStatus,
                        playCountSnapshots = state.playCountSnapshots,
                        autoLogin = autoLogin,
                        onImportData = viewModel::importData,
                        onClear = viewModel::clearAccount,
                        onSaveAutoLoginCredentials = viewModel::saveAutoLoginCredentials,
                        onAutoLoginEnabledChange = viewModel::setAutoLoginEnabled,
                        onForgetAutoLoginCredentials = viewModel::forgetAutoLoginCredentials,
                        onOpenProfileCard = { backStack.push(AppScreen.ProfileCard) },
                        onOpenPlayCountHistory = { backStack.push(AppScreen.PlayCountHistory) },
                        onBack = {
                            viewModel.dismissImportStatus()
                            pop()
                        },
                    )

                    AppScreen.ProfileCard -> {
                        val profile = state.profile
                        if (profile != null) {
                            ProfileCardScreen(profile = profile, onBack = pop)
                        } else {
                            LaunchedEffect(screen) { pop() }
                        }
                    }

                    AppScreen.PlayCountHistory -> PlayCountHistoryScreen(
                        snapshots = state.playCountSnapshots,
                        onBack = pop,
                    )

                    AppScreen.Circle -> CircleScreen(
                        history = state.circleHistory,
                        snapshots = state.circleSnapshots,
                        trackingSettings = state.trackingSettings,
                        importStatus = state.importStatus,
                        onSync = {
                            viewModel.importData(state.profile?.region ?: AccountRegion.INTERNATIONAL)
                        },
                        onOpenAccount = { backStack.push(AppScreen.Account) },
                        onBack = pop,
                    )

                    AppScreen.ScoreStats -> ScoreStatsScreen(
                        charts = state.allCharts,
                        scores = state.scores,
                        onBack = pop,
                        onChart = openChart,
                        onOpenAccount = { backStack.push(AppScreen.Account) },
                    )

                    is AppScreen.UnlockGuide -> UnlockGuideScreen(
                        section = screen.section,
                        initialEntryId = screen.entryId,
                        charts = state.allCharts,
                        scores = state.scores,
                        profile = state.profile,
                        onBack = pop,
                        onChart = openChart,
                    )

                    AppScreen.DanGuide -> {
                        val danChartLookup = remember(state.allCharts) {
                            DanCourseMetadata.chartLookup(state.allCharts)
                        }
                        DanGuideScreen(
                            chartLookup = danChartLookup,
                            initialRegion = state.profile?.region ?: AccountRegion.INTERNATIONAL,
                            onBack = pop,
                            onChart = openChart,
                        )
                    }

                    AppScreen.About -> AboutScreen(info = state.info, onBack = pop)

                    AppScreen.Settings -> SettingsScreen(
                        themeMode = themeMode,
                        onThemeModeChange = viewModel::setThemeMode,
                        onBack = pop,
                    )
                }
            }
        }
        SnackbarHost(
            snackbarHostState,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogRoot(
    state: CatalogUiState,
    onOpenDrawer: () -> Unit,
    onRetry: () -> Unit,
    onSearch: (String) -> Unit,
    onSort: (ChartSort) -> Unit,
    onToggleSortOrder: () -> Unit,
    onOpenFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onChart: (SongChart) -> Unit,
    onUnlockGuide: (String) -> Unit,
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
                    IconButton(onClick = onOpenDrawer) {
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
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Default.ErrorOutline,
                    title = "The song catalog could not be opened",
                    body = "MaiDex could not read its offline song catalog. Your account, guides and " +
                        "settings are still in the menu.",
                ) {
                    Text(
                        // SQLite messages carry the whole failing query; the first clause is enough.
                        state.error.orEmpty().substringBefore(", while compiling"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Button(onClick = onRetry) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Try again")
                    }
                    TextButton(onClick = onOpenDrawer) { Text("Open menu") }
                }
            }

            else -> CatalogContent(
                state = state,
                modifier = Modifier.padding(padding),
                onSearch = onSearch,
                onSort = onSort,
                onToggleSortOrder = onToggleSortOrder,
                onOpenFilters = onOpenFilters,
                onClearFilters = onClearFilters,
                onChart = onChart,
                onUnlockGuide = onUnlockGuide,
            )
        }
    }
}

@Composable
private fun LoadingScreen(onBack: () -> Unit) {
    SecondaryScreen(title = "Loading…", onBack = onBack) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun AppDrawerContent(
    profile: PlayerProfile?,
    onClose: () -> Unit,
    onOpen: (AppScreen) -> Unit,
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
            onClick = { onOpen(AppScreen.Account) },
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
            onClick = { onOpen(AppScreen.Circle) },
            icon = { Icon(Icons.Default.Groups, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("Score stats") },
            selected = false,
            onClick = { onOpen(AppScreen.ScoreStats) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            label = { Text("Dan courses") },
            selected = false,
            onClick = { onOpen(AppScreen.DanGuide) },
            icon = { Icon(Icons.Default.MilitaryTech, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("Chiho guides") },
            selected = false,
            onClick = { onOpen(AppScreen.UnlockGuide(UnlockGuideSection.CHIHOS)) },
            icon = { Icon(Icons.Default.Map, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("Class Battle guides") },
            selected = false,
            onClick = { onOpen(AppScreen.UnlockGuide(UnlockGuideSection.CLASS_BATTLES)) },
            icon = { Icon(Icons.Default.Groups, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Spacer(Modifier.weight(1f))
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = { onOpen(AppScreen.Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("About MaiDex") },
            selected = false,
            onClick = { onOpen(AppScreen.About) },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun AboutScreen(
    info: CatalogInfo?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    SecondaryScreen(
        title = "About MaiDex",
        subtitle = "Version ${BuildConfig.VERSION_NAME}",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://arcade-songs.zetaraku.dev/maimai/"),
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Data source")
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    SecondaryScreen(title = "Settings", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(vertical = 8.dp),
        ) {
            Text(
                "Theme",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mode == themeMode,
                                onClick = { onThemeModeChange(mode) },
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == themeMode, onClick = null)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                            if (mode == ThemeMode.SYSTEM) {
                                Text(
                                    "Follow Android's dark theme setting",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            placeholder = {
                Text(
                    "Title, artist, designer…",
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            shape = RoundedCornerShape(6.dp),
        )
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 40.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "No matching charts",
                    body = when {
                        state.filters.search.isNotBlank() && state.filters.activeCount > 0 ->
                            "Nothing matches “${state.filters.search}” with the current filters."
                        state.filters.search.isNotBlank() -> "Nothing matches “${state.filters.search}”."
                        else -> "No chart passes the current filters."
                    },
                ) {
                    if (state.filters.search.isNotBlank()) {
                        TextButton(onClick = { onSearch("") }) { Text("Clear search") }
                    }
                    if (state.filters.activeCount > 0) {
                        TextButton(onClick = onClearFilters) { Text("Clear filters") }
                    }
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
        colors = CardDefaults.cardColors(containerColor = MaiDexTheme.colors.chartCard),
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
                    color = MaiDexTheme.colors.chartMeta,
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
                        listOfNotNull(
                            chart.level?.let { "Lv$it" },
                            chart.constant?.let { "C${String.format(Locale.US, "%.1f", it)}" },
                        ).joinToString(" · "),
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
                    color = MaiDexTheme.colors.panel,
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, MaiDexTheme.colors.panelBorder),
                ) {
                    Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                        Text(
                            "No imported DX NET score",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaiDexTheme.colors.inkSoft,
                        )
                        chart.noteDesigner?.takeIf(String::isNotBlank)?.let { designer ->
                            Text(
                                "NOTES DESIGNER · " +
                                    titleWithRomanization(designer, chart.noteDesignerRomanized),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaiDexTheme.colors.inkMuted,
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
        border = BorderStroke(1.dp, MaiDexTheme.colors.scoreBorder),
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
                    color = MaiDexTheme.colors.ink,
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
            HorizontalDivider(color = MaiDexTheme.colors.ink.copy(alpha = 0.16f))
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
                        color = MaiDexTheme.colors.positive,
                    )
                    Text(
                        titleWithRomanization(
                            chart.noteDesigner.orEmpty(),
                            chart.noteDesignerRomanized,
                        ).ifBlank { "—" },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaiDexTheme.colors.inkSoft,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    RatingCalculator.chartRating(chart, score)?.let {
                        Text(
                            "Rt $it",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = MaiDexTheme.colors.ink,
                        )
                    }
                    chart.bpm?.let {
                        Text(
                            "BPM $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaiDexTheme.colors.inkMuted,
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
                color = MaiDexTheme.colors.inkSoft,
            )
            DxStarImage(score.dxStarCount)
        }
        percentage?.let {
            Text(
                "${String.format(Locale.US, "%.2f", it)}% of maximum DX score",
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelSmall,
                color = MaiDexTheme.colors.inkMuted,
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



@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DanGuideScreen(
    chartLookup: Map<String, SongChart>,
    initialRegion: AccountRegion,
    onBack: () -> Unit,
    onChart: (SongChart) -> Unit,
) {
    val context = LocalContext.current
    var region by rememberSaveable(initialRegion) { mutableStateOf(initialRegion) }
    var version by rememberSaveable { mutableStateOf(DanCourseMetadata.version) }
    var group by rememberSaveable { mutableStateOf(DanCourseGroup.TRUE) }
    var query by rememberSaveable { mutableStateOf("") }
    val versionCourses = DanCourseMetadata.coursesForVersion(version)
    val groups = DanCourseGroup.entries.filter { candidate -> versionCourses.any { it.group == candidate } }
    LaunchedEffect(version) {
        if (group !in groups) group = DanCourseGroup.TRUE
    }
    // Search looks through every Dan type of the selected version only (owner decision).
    val searching = query.isNotBlank()
    val courses = if (searching) {
        filterDanCourses(versionCourses, query, region, chartLookup)
    } else {
        versionCourses.filter { it.group == group }
    }

    SecondaryScreen(
        title = "Dan courses",
        subtitle = "$version · ${region.label}",
        onBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(DanCourseMetadata.sourceUrl(region, version))),
                    )
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open Dan course source")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "controls") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GuideSearchField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Course or song in $version",
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledDropdown(
                            label = "Version",
                            value = version,
                            options = DanCourseMetadata.versions,
                            optionLabel = { it },
                            onSelect = { version = it },
                            modifier = Modifier.weight(1f),
                        )
                        LabeledDropdown(
                            label = "Region",
                            value = region,
                            options = AccountRegion.entries,
                            optionLabel = AccountRegion::label,
                            onSelect = { region = it },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (searching) {
                        Text(
                            "${courses.size} of ${versionCourses.size} $version courses match, all Dan types",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            groups.forEach { option ->
                                FilterChip(
                                    selected = option == group,
                                    onClick = { group = option },
                                    label = { Text(danGroupLabel(option)) },
                                )
                            }
                        }
                    }
                }
            }
            if (courses.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "No matching courses",
                        body = "No $version course name or track matches “$query”. Search covers the selected version only.",
                    )
                }
            }
            items(courses, key = { "$version:${it.id}" }) { course ->
                DanCourseCard(
                    course = course,
                    region = region,
                    chartLookup = chartLookup,
                    onChart = onChart,
                    showGroup = searching,
                )
            }
        }
    }
}

private fun danGroupLabel(group: DanCourseGroup): String = when (group) {
    DanCourseGroup.NORMAL -> "Dan 1–10"
    DanCourseGroup.TRUE -> "Shin Dan"
    DanCourseGroup.URA -> "Ura Kaiden"
}

@Composable
private fun <T> LabeledDropdown(
    label: String,
    value: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 4.dp),
            ) {
                Text(
                    optionLabel(value),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
    showGroup: Boolean,
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
                    if (showGroup) {
                        Text(
                            danGroupLabel(course.group).uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
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

/** "DX · MASTER 14+ (14.7)"; level and constant are left out when the catalog does not know them. */
internal fun danTrackLabel(track: DanTrack, chart: SongChart?): String = buildString {
    append(typeLabel(track.type))
    append(" · ")
    append(difficultyLabel(track.difficulty))
    chart?.level?.let {
        append(' ')
        append(it)
    }
    chart?.constant?.let {
        append(" (")
        append(String.format(Locale.US, "%.1f", it))
        append(')')
    }
}

@Composable
private fun DanTrackRow(
    number: Int,
    track: DanTrack,
    chart: SongChart?,
    onChart: (SongChart) -> Unit,
) {
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
            SongJacket(imageUrl = chart?.imageUrl, size = 52.dp)
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
                Spacer(Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = difficultyColor(track.difficulty).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(7.dp),
                    ) {
                        Text(
                            danTrackLabel(track, chart),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = difficultyTextColor(track.difficulty),
                        )
                    }
                    if (chart == null) {
                        Text(
                            "Not in the offline catalog",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (chart != null) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Cover art, or a music-note tile when there is no image (a song missing from the catalog). */
@Composable
private fun SongJacket(imageUrl: String?, size: androidx.compose.ui.unit.Dp) {
    if (imageUrl.isNullOrBlank()) {
        Surface(
            modifier = Modifier.size(size),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(7.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(7.dp)),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun UnlockGuideScreen(
    section: UnlockGuideSection,
    initialEntryId: String?,
    charts: List<SongChart>,
    scores: Map<String, UserScore>,
    profile: PlayerProfile?,
    onBack: () -> Unit,
    onChart: (SongChart) -> Unit,
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
    val chartsBySong = remember(charts) {
        buildMap<String, MutableList<SongChart>> {
            charts.forEach { chart ->
                setOf(chart.sourceSongId, chart.title).forEach { getOrPut(it) { mutableListOf() }.add(chart) }
            }
        }
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
    var query by rememberSaveable { mutableStateOf("") }
    val visibleEntries = filterUnlockGuideEntries(orderedEntries, query, romanizedTitles)
    val currentClassVersion = ClassBattleMetadata.byVersion.keys.firstOrNull()
    val isChiho = section == UnlockGuideSection.CHIHOS
    val chartFor: (String, String?) -> SongChart? = { title, level ->
        guideSongChart(title, chartsBySong[title].orEmpty(), scores, level)
    }

    SecondaryScreen(
        title = if (isChiho) "Chiho guides" else "Class Battle guides",
        subtitle = if (isChiho) "${entries.size} areas" else "${entries.size} versions",
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "search") {
                GuideSearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = if (isChiho) "Area or unlock song" else "Version, Gift Song or boss song",
                )
            }
            item(key = "intro") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaiDexTheme.colors.infoContainer,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaiDexTheme.colors.info)
                        Text(
                            UnlockMetadata.sectionIntro(section) + if (profile == null) {
                                "\n\nImport your DX NET data from the account page to see your progress here."
                            } else {
                                ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            if (visibleEntries.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "No matches",
                        body = if (isChiho) {
                            "No area or unlock song matches “$query”."
                        } else {
                            "No version, Gift Song or boss song matches “$query”."
                        },
                    )
                }
            }
            items(visibleEntries, key = UnlockGuideEntry::id) { entry ->
                UnlockGuideEntryCard(
                    entry = entry,
                    isLinkedEntry = entry.id == initialEntryId,
                    isCurrentClassVersion = entry.title == currentClassVersion,
                    query = query,
                    romanizedTitles = romanizedTitles,
                    bestScoresBySong = bestScoresBySong,
                    bestScoresBySongAndLevel = bestScoresBySongAndLevel,
                    profile = profile,
                    chartFor = chartFor,
                    onChart = onChart,
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

/** Progress line of a Chiho card. Map completion comes from the imported DX NET profile. */
internal fun chihoStatusText(
    playedSongs: Int,
    totalSongs: Int,
    mapComplete: Boolean,
): String = if (mapComplete) {
    "Map complete on DX NET"
} else {
    "$playedSongs/$totalSongs unlock songs played"
}

@Composable
private fun UnlockGuideEntryCard(
    entry: UnlockGuideEntry,
    isLinkedEntry: Boolean,
    isCurrentClassVersion: Boolean,
    query: String,
    romanizedTitles: Map<String, String>,
    bestScoresBySong: Map<String, UserScore>,
    bestScoresBySongAndLevel: Map<String, UserScore>,
    profile: PlayerProfile?,
    chartFor: (String, String?) -> SongChart?,
    onChart: (SongChart) -> Unit,
    onOpenSource: () -> Unit,
) {
    val isChiho = entry.section == UnlockGuideSection.CHIHOS
    val milestones = ClassBattleMetadata.byVersion[entry.title].orEmpty()
    var showMilestones by rememberSaveable(entry.id) { mutableStateOf(isLinkedEntry) }
    // A search that matches a boss song opens the milestone list so the match is visible.
    val normalizedQuery = normalizeSearch(query)
    val milestoneMatch = normalizedQuery.isNotEmpty() && milestones.any { milestone ->
        normalizeSearch(milestone.songTitle).contains(normalizedQuery) ||
            normalizeSearch(romanizedTitles[milestone.songTitle].orEmpty()).contains(normalizedQuery)
    }
    LaunchedEffect(milestoneMatch) {
        if (milestoneMatch) showMilestones = true
    }
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
    val allClassesCleared = isCurrentClassVersion && clearedClasses == milestones.size && milestones.isNotEmpty()

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
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
                }
                IconButton(onClick = onOpenSource) {
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = if (isChiho) "Open source" else "Open boss source",
                    )
                }
            }
            // Progress needs imported data; without it every card would repeat the same hint, so the
            // section intro says it once instead.
            if (profile != null) {
                Spacer(Modifier.height(6.dp))
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
                                imageVector = if (completedChiho || allClassesCleared) {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.Default.RadioButtonUnchecked
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (completedChiho || allClassesCleared) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                            )
                            Text(
                                text = when {
                                    isChiho -> chihoStatusText(
                                        playedSongs = playedUnlockSongs,
                                        totalSongs = entry.songs.size,
                                        mapComplete = completedChiho,
                                    )
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
            }
            Spacer(Modifier.height(4.dp))
            entry.songs.forEach { song ->
                val played = bestScoresBySong[unlockProgressSongKey(song.title)] != null
                GuideSongRow(
                    title = titleWithRomanization(song.title, romanizedTitles[song.title].orEmpty()),
                    detail = listOfNotNull(
                        song.requirement.takeIf(String::isNotBlank),
                        "Played".takeIf { played },
                    ).joinToString(" · "),
                    played = played,
                    chart = chartFor(song.title, null),
                    onChart = onChart,
                )
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
                                // Without an imported class there is no current one to highlight.
                                current = isCurrentClassVersion &&
                                    !profile?.friendClass.isNullOrBlank() &&
                                    index == clearedClasses &&
                                    !profile?.friendClass.equals("LEGEND", ignoreCase = true),
                                chart = chartFor(milestone.songTitle, milestone.level),
                                onChart = onChart,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** An unlock song: jacket, title and requirement; opens the song's chart when the catalog has it. */
@Composable
private fun GuideSongRow(
    title: String,
    detail: String,
    played: Boolean,
    chart: SongChart?,
    onChart: (SongChart) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = chart != null) { chart?.let(onChart) }
            .padding(vertical = 5.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongJacket(imageUrl = chart?.imageUrl, size = 44.dp)
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (detail.isNotBlank()) {
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (played) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Played",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        if (chart != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ClassBattleMilestoneRow(
    milestone: ClassBattleMilestone,
    score: UserScore?,
    cleared: Boolean,
    current: Boolean,
    chart: SongChart?,
    onChart: (SongChart) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .clickable(enabled = chart != null) { chart?.let(onChart) },
        color = when {
            cleared -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            current -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f)
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        shape = RoundedCornerShape(9.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongJacket(imageUrl = chart?.imageUrl, size = 40.dp)
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (cleared) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Cleared",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
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
private fun FilterScreen(
    filters: ChartFilters,
    sort: ChartSort,
    sortOrder: SortOrder,
    options: FilterOptions,
    presets: List<FilterPreset>,
    hasScores: Boolean,
    onBack: () -> Unit,
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

    SecondaryScreen(
        title = "Filters",
        onBack = onBack,
        actions = {
            TextButton(onClick = {
                draft = ChartFilters(search = filters.search)
                minLevel = ""
                maxLevel = ""
                minBpm = ""
                maxBpm = ""
                draftSort = ChartSort.LEVEL
                draftSortOrder = SortOrder.DESCENDING
            }) { Text("Reset") }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onBack) { Text("Cancel") }
                    Button(onClick = {
                        onApply(normalizedDraft(), draftSort, draftSortOrder)
                    }) { Text("Show results") }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
private fun AccountScreen(
    profile: PlayerProfile?,
    importStatus: ImportStatus,
    playCountSnapshots: List<PlayCountSnapshot>,
    autoLogin: AutoLoginUiState,
    onImportData: (AccountRegion) -> Unit,
    onClear: () -> Unit,
    onSaveAutoLoginCredentials: (String, String) -> Unit,
    onAutoLoginEnabledChange: (Boolean) -> Unit,
    onForgetAutoLoginCredentials: () -> Unit,
    onOpenProfileCard: () -> Unit,
    onOpenPlayCountHistory: () -> Unit,
    onBack: () -> Unit,
) {
    var region by rememberSaveable(profile?.region) {
        mutableStateOf(profile?.region ?: AccountRegion.INTERNATIONAL)
    }
    val context = LocalContext.current
    val isImporting = importStatus is ImportStatus.Running
    val loginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onImportData(region)
    }

    SecondaryScreen(title = "maimai DX NET", subtitle = profile?.name, onBack = onBack) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                "Region",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )

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
                        .clickable(onClick = onOpenProfileCard),
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
                        TextButton(onClick = onOpenPlayCountHistory) {
                            Text("View history")
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 8.dp),
                    color = MaiDexTheme.colors.infoContainer,
                    border = BorderStroke(1.dp, MaiDexTheme.colors.infoBorder),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaiDexTheme.colors.info,
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
            AutoSignInCard(
                state = autoLogin,
                busy = isImporting,
                onSaveCredentials = onSaveAutoLoginCredentials,
                onEnabledChange = onAutoLoginEnabledChange,
                onForget = onForgetAutoLoginCredentials,
            )
            if (profile != null || autoLogin.username != null) {
                TextButton(
                    onClick = onClear,
                    enabled = !isImporting,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        if (autoLogin.username != null) {
                            "Remove imported account data and saved SEGA ID login"
                        } else {
                            "Remove imported account data"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleScreen(
    history: List<CircleData>,
    snapshots: List<CircleDailySnapshot>,
    trackingSettings: TrackingSettings,
    importStatus: ImportStatus,
    onSync: () -> Unit,
    onOpenAccount: () -> Unit,
    onBack: () -> Unit,
) {
    val current = history.firstOrNull()
    val memberTrends = remember(snapshots) { circleMemberTrends(snapshots) }
    SecondaryScreen(
        title = current?.name ?: "Circle",
        subtitle = current?.let { circle ->
            listOfNotNull(circle.month, circle.circleClass.takeIf(String::isNotBlank)?.let { "Class $it" })
                .joinToString(" · ")
        } ?: "No circle data yet",
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "tracking") {
                CircleTrackingCard(
                    settings = trackingSettings,
                    importStatus = importStatus,
                    onSync = onSync,
                )
            }
            if (current == null) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Default.Groups,
                        title = "No circle data yet",
                        body = "Sign in to DX NET or use Sync now. Monthly history starts with the first " +
                            "successful circle sync.",
                    ) {
                        OutlinedButton(onClick = onOpenAccount) { Text("Open DX NET account") }
                    }
                }
            } else {
                item(key = "summary") { CircleSummaryCard(current) }
                item(key = "history-heading") {
                    SectionHeading(
                        "Daily point tracking",
                        if (snapshots.isEmpty()) {
                            "Trends appear after the first successful hourly sync"
                        } else {
                            "${snapshots.size} daily snapshot" + if (snapshots.size == 1) "" else "s"
                        },
                    )
                }
                if (snapshots.isNotEmpty()) {
                    item(key = "circle-trend") {
                        PointTrendCard(
                            title = "Circle total",
                            points = snapshots.map { it.day to it.circle.totalPoints },
                        )
                    }
                    if (memberTrends.isNotEmpty()) {
                        item(key = "member-trend") { MemberTrendCard(memberTrends) }
                    }
                }
                item(key = "months-heading") {
                    SectionHeading(
                        "Monthly history",
                        "History starts when MaiDex first syncs each month",
                    )
                }
                items(history, key = { circle -> "month-${circle.month}-${circle.key}" }) { circle ->
                    CircleMonthCard(circle, initiallyExpanded = circle === current)
                }
                val officialPages = current.pages.filter { it.html.isNotBlank() }
                item(key = "official-heading") {
                    SectionHeading(
                        "Official DX NET pages",
                        if (officialPages.isEmpty()) {
                            "Read-only copies of DX NET's own Circle pages"
                        } else {
                            "${officialPages.size} page" + (if (officialPages.size == 1) "" else "s") +
                                " from the last sync, as DX NET shows them"
                        },
                    )
                }
                if (officialPages.isEmpty()) {
                    item(key = "snapshot-required") {
                        EmptyState(
                            icon = Icons.Default.Language,
                            title = "No official pages captured",
                            body = "Sync now to capture the official Circle layouts.",
                        )
                    }
                }
                items(
                    officialPages,
                    key = { page -> "page-${page.type}-${page.title}" },
                ) { page ->
                    OfficialPageCard(title = page.title, html = page.html)
                }
            }
        }
    }
}

@Composable
private fun CircleSummaryCard(circle: CircleData) {
    val metrics = listOfNotNull(
        formatCount(circle.totalPoints) to "Points",
        circle.regionalRank?.let { "#${formatCount(it)}" to "Regional rank" },
        circle.memberCount?.let { it.toString() to "Members" },
        circle.daysUntilReset?.let { it.toString() to "Days to reset" },
    )
    // Two per row: four tiles in one row wrap large point totals mid-number.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { (value, label) ->
                    InfoMetric(value = value, label = label, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** An official DX NET page, collapsed by default so the app's own cards stay together above it. */
@Composable
private fun OfficialPageCard(title: String, html: String) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Text(
                title.ifBlank { "DX NET page" },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
            )
        }
        if (expanded) {
            OfficialCirclePage(html = html, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun CircleTrackingCard(
    settings: TrackingSettings,
    importStatus: ImportStatus,
    onSync: () -> Unit,
) {
    val isSyncing = importStatus is ImportStatus.Running
    val lastError = (importStatus as? ImportStatus.Failure)?.message ?: settings.lastError
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Circle data sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val status = when {
                        isSyncing -> "Syncing now…"
                        lastError.isNotBlank() ->
                            "Last sync failed: $lastError"
                        settings.lastSuccessAt > 0L ->
                            "Last synced ${formatTrackedTime(settings.lastSuccessAt)}"
                        else -> "Not synced yet"
                    }
                    Text(
                        status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (lastError.isBlank() || isSyncing) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
                Button(onClick = onSync, enabled = !isSyncing) {
                    if (isSyncing) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (isSyncing) "Syncing…" else "Sync now")
                }
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
private fun CircleMonthCard(circle: CircleData, initiallyExpanded: Boolean) {
    var expanded by rememberSaveable(circle.month, circle.key) { mutableStateOf(initiallyExpanded) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
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
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
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
private fun SectionHeading(title: String, subtitle: String) {
    Column(
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(12.dp)) {
            TrendHeader(title, points)
            TrendChart(points)
        }
    }
}

/** One trend card for all members: chips pick the member, the signed-in player first. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberTrendCard(trends: List<MemberTrend>) {
    var selectedKey by rememberSaveable { mutableStateOf(trends.first().key) }
    val selected = trends.firstOrNull { it.key == selectedKey } ?: trends.first()
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(12.dp)) {
            TrendHeader(
                title = if (selected.isCurrentUser) "${selected.name} (you)" else selected.name,
                points = selected.points,
            )
            TrendChart(selected.points)
            FlowRow(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                trends.forEach { trend ->
                    FilterChip(
                        selected = trend.key == selected.key,
                        onClick = { selectedKey = trend.key },
                        label = { Text(if (trend.isCurrentUser) "${trend.name} (you)" else trend.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendHeader(title: String, points: List<Pair<String, Int>>) {
    val first = points.first().second
    val latest = points.last().second
    Row(verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(
                "${points.size} day" + (if (points.size == 1) "" else "s") + " · since ${points.first().first}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatCount(latest),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
            if (latest != first) {
                Text(
                    "${if (latest > first) "+" else ""}${formatCount(latest - first)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (latest > first) MaiDexTheme.colors.positive else MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

/** Line chart with a light grid, the highest and lowest value on the left and the first and last day below. */
@Composable
private fun TrendChart(points: List<Pair<String, Int>>) {
    val values = points.map { it.second }
    val minimum = values.min()
    val maximum = values.max()
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .height(96.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
        ) {
            Text(formatCount(maximum), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (maximum != minimum) {
                Text(formatCount(minimum), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 6.dp),
        ) {
            val range = (maximum - minimum).toFloat().takeIf { it > 0f } ?: 1f
            val step = if (values.size > 1) size.width / values.lastIndex else 0f
            fun point(index: Int): androidx.compose.ui.geometry.Offset {
                val x = if (values.size == 1) size.width / 2f else index * step
                val y = if (maximum == minimum) {
                    size.height / 2f
                } else {
                    size.height - ((values[index] - minimum) / range * size.height)
                }
                return androidx.compose.ui.geometry.Offset(x, y)
            }
            listOf(0f, 0.5f, 1f).forEach { fraction ->
                val y = size.height * fraction
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            if (values.size > 1) {
                val area = androidx.compose.ui.graphics.Path().apply {
                    moveTo(point(0).x, size.height)
                    values.indices.forEach { lineTo(point(it).x, point(it).y) }
                    lineTo(point(values.lastIndex).x, size.height)
                    close()
                }
                drawPath(area, lineColor.copy(alpha = 0.12f))
            }
            values.indices.drop(1).forEach { index ->
                drawLine(
                    color = lineColor,
                    start = point(index - 1),
                    end = point(index),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            values.indices.forEach { index ->
                drawCircle(color = lineColor, radius = 2.5.dp.toPx(), center = point(index))
            }
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

@Composable
private fun PlayCountHistoryScreen(
    snapshots: List<PlayCountSnapshot>,
    onBack: () -> Unit,
) {
    SecondaryScreen(
        title = "Play-count history",
        subtitle = "${snapshots.size} daily snapshot" + if (snapshots.size == 1) "" else "s",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (snapshots.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.BarChart,
                    title = "No play counts yet",
                    body = "MaiDex records your play counts once a day when it syncs with DX NET.",
                )
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
internal fun ScoreStatsScreen(
    charts: List<SongChart>,
    scores: Map<String, UserScore>,
    onBack: () -> Unit,
    onChart: (SongChart) -> Unit,
    onOpenAccount: () -> Unit,
) {
    val summary = remember(charts, scores) { ScoreAnalytics.summarize(charts, scores) }
    val expandedTables = remember { MedalTableExpansionState() }
    SecondaryScreen(
        title = "Score stats",
        subtitle = if (summary.playedCharts > 0) "${summary.playedCharts} matched scores" else null,
        onBack = onBack,
    ) { padding ->
        if (summary.playedCharts == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Default.BarChart,
                    title = "No scores imported yet",
                    body = "Import your DX NET scores to see personal bests and medal counts for every level.",
                ) {
                    Button(onClick = onOpenAccount) { Text("Open DX NET account") }
                }
            }
            return@SecondaryScreen
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
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

@Composable
private fun medalRowColor(label: String): Color = when (label) {
    "FC", "FC+" -> MaiDexTheme.colors.medalComboRow
    "AP", "AP+" -> MaiDexTheme.colors.medalPerfectRow
    "FS", "FS+" -> MaiDexTheme.colors.medalSyncRow
    else -> MaiDexTheme.colors.medalOtherRow
}

@Composable
private fun medalLabelColor(label: String): Color = when (label) {
    "FC", "FC+" -> MaiDexTheme.colors.medalComboLabel
    "AP", "AP+" -> MaiDexTheme.colors.medalPerfectLabel
    "FS", "FS+" -> MaiDexTheme.colors.medalSyncLabel
    else -> MaiDexTheme.colors.medalOtherLabel
}

@Composable
private fun ProfileCardScreen(
    profile: PlayerProfile,
    onBack: () -> Unit,
) {
    var rotated by rememberSaveable { mutableStateOf(false) }
    SecondaryScreen(
        title = "Profile card",
        subtitle = profile.name,
        onBack = onBack,
        actions = {
            TextButton(onClick = { rotated = !rotated }) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text(if (rotated) "Return card" else "Rotate · larger")
            }
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            val naturalWidth = if (rotated) {
                (maxHeight * 0.92f).coerceAtMost(720.dp)
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
private fun ChartDetailScreen(
    chart: SongChart,
    score: UserScore?,
    playDetail: PlayDetail?,
    state: CatalogUiState,
    onOpenGuide: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    SecondaryScreen(
        title = chart.title,
        subtitle = "${chart.displayType} · ${chart.displayDifficulty}",
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    listOfNotNull(
                        chart.level?.let { "Lv $it" },
                        chart.constant?.let { "constant ${String.format(Locale.US, "%.1f", it)}" },
                    ).joinToString(" · ").ifEmpty { "Level unknown" },
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

/** [difficultyColor] for text on a light tint of itself; lightened on dark surfaces so it stays readable. */
@Composable
private fun difficultyTextColor(value: String): Color {
    val base = difficultyColor(value)
    return if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) lerp(base, Color.White, 0.45f) else base
}

private fun difficultyColor(value: String): Color = when (value) {
    "basic" -> Color(0xFF189B4E)
    "advanced" -> Color(0xFFD87300)
    "expert" -> Color(0xFFD72F48)
    "master" -> Color(0xFF7D35BD)
    "remaster" -> Color(0xFF9D50D0)
    else -> Color(0xFF52636F)
}



@Composable
private fun scorePanelColor(grade: Grade): Color = when (grade) {
    Grade.SSS_PLUS,
    Grade.SSS,
    -> MaiDexTheme.colors.scoreGold
    Grade.SS_PLUS,
    Grade.SS,
    -> MaiDexTheme.colors.scoreBlue
    Grade.S_PLUS,
    Grade.S,
    -> MaiDexTheme.colors.scoreRed
    else -> MaiDexTheme.colors.scoreNeutral
}
