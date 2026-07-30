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
private fun ChartCard(chart: SongChart, score: UserScore?, onClick: () -> Unit) {
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
                    chart.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!chart.titleRomanized.equals(chart.title, ignoreCase = true)) {
                    Text(
                        chart.titleRomanized,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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
                }
            }
        }
    }
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
                        Text("maimai DX NET", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Official site sign-in; credentials never enter MaiDex",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = {
                        onDismissStatus()
                        onDismiss()
                    }) { Text("Close") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountRegion.entries.forEach { option ->
                        FilterChip(
                            selected = region == option,
                            enabled = !isImporting,
                            onClick = { region = option },
                            label = { Text(option.label) },
                        )
                    }
                }
                profile?.let {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(it.name, fontWeight = FontWeight.Bold)
                                Text("Official rating ${it.officialRating} · ${it.region.label}")
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
                    Text(
                        "DX NET sign-in opens full screen so SNS login pages receive a full browser viewport. " +
                            "MaiDex closes it automatically after DX NET confirms the login.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                        Text("Open sign-in")
                    }
                    Button(
                        onClick = { onImport(region) },
                        enabled = !isImporting,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Import scores")
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
private fun ChartDetailDialog(
    chart: SongChart,
    score: UserScore?,
    playDetail: PlayDetail?,
    state: CatalogUiState,
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
                Text(chart.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                if (!chart.titleRomanized.equals(chart.title, ignoreCase = true)) {
                    Text(chart.titleRomanized, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
    DetailRow("DX score", "${score.dxScore} / ${score.maxDxScore}")
    DetailRow("Chart rating", RatingCalculator.chartRating(chart, score)?.toString() ?: "Unknown constant")
    DetailRow(
        "Total rating",
        "${state.calculatedRating} calculated" +
            (state.profile?.officialRating?.let { "  ·  $it official" } ?: ""),
    )

    val milestones = remember(chart.chartKey, score, state.scores) {
        RatingCalculator.milestones(
            chart = chart,
            score = score,
            charts = state.allCharts,
            scores = state.scores,
            newVersions = state.newVersions,
        )
    }
    if (milestones.isNotEmpty() && chart.constant != null) {
        Spacer(Modifier.height(12.dp))
        Text("Next rating milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "AP and AP+ receive the current CiRCLE-era +1 rating bonus. Achievement is capped at 100.5%.",
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
                    Text(String.format(Locale.US, "%.4f%%", milestone.achievement))
                }
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        "Chart Rt ${milestone.chartRating ?: "—"}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        if (milestone.totalChange >= 0) {
                            "+${milestone.totalChange} total rating"
                        } else {
                            "${milestone.totalChange} total rating"
                        },
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

@Composable
private fun JudgmentDetail(chart: SongChart, detail: PlayDetail) {
    var showLossTable by remember { mutableStateOf(false) }
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
            TableCell("Note", 62, true)
            TableCell("CP", 48, true)
            TableCell("P", 48, true)
            TableCell("Great", 54, true)
            TableCell("Good", 50, true)
            TableCell("Miss", 50, true)
        }
        JudgeRow("Tap", detail.judgments.tap)
        JudgeRow("Hold", detail.judgments.hold)
        JudgeRow("Slide", detail.judgments.slide)
        JudgeRow("Touch", detail.judgments.touch)
        JudgeRow("Break", detail.judgments.breakNotes)
    }
    val loss = AchievementLossCalculator.actualLossRange(chart.noteCounts, detail.judgments)
    val lossText = if (kotlin.math.abs(loss.maximum - loss.minimum) < 0.0000001) {
        String.format(Locale.US, "%.6f percentage points", loss.minimum)
    } else {
        String.format(Locale.US, "%.6f–%.6f percentage points", loss.minimum, loss.maximum)
    }
    DetailRow("Judgement loss", lossText)
    Text(
        "A range is shown when DX NET groups near/far Break Perfects or high/mid/low Break Greats together.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    TextButton(onClick = { showLossTable = !showLossTable }) {
        Text(if (showLossTable) "Hide per-judgement losses" else "Show per-judgement losses")
    }
    if (showLossTable) {
        Text(
            "Loss from one judgement on this chart. Non-Break Perfect loses 0 achievement but 1 DX score.",
            style = MaterialTheme.typography.bodySmall,
        )
        AchievementLossCalculator.perJudgment(chart.noteCounts).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
            ) {
                Text(row.noteType, modifier = Modifier.width(52.dp), style = MaterialTheme.typography.labelMedium)
                Text(row.judgment, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                Text(
                    String.format(Locale.US, "−%.6f pp", row.achievementLoss),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.width(8.dp))
                Text("DX −${row.dxScoreLoss}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun JudgeRow(name: String, counts: JudgeCounts) {
    Row(Modifier.padding(vertical = 3.dp)) {
        TableCell(name, 62, true)
        TableCell(counts.criticalPerfect.toString(), 48)
        TableCell(counts.perfect.toString(), 48)
        TableCell(counts.great.toString(), 54)
        TableCell(counts.good.toString(), 50)
        TableCell(counts.miss.toString(), 50)
    }
}

@Composable
private fun TableCell(value: String, width: Int, bold: Boolean = false) {
    Text(
        value,
        modifier = Modifier.width(width.dp),
        style = MaterialTheme.typography.bodySmall,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        values.forEach { (name, count) ->
            Surface(
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
