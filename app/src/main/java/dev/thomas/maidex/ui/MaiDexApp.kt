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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.thomas.maidex.CatalogUiState
import dev.thomas.maidex.MainViewModel
import dev.thomas.maidex.data.ChartFilters
import dev.thomas.maidex.data.ChartSort
import dev.thomas.maidex.data.FilterOptions
import dev.thomas.maidex.data.SongChart
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaiDexApp(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    var selectedChart by remember { mutableStateOf<SongChart?>(null) }
    var showAbout by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { showAbout = true }) {
                        Icon(Icons.Default.Info, contentDescription = "About catalog")
                    }
                    Box {
                        IconButton(onClick = { showFilters = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Open filters")
                        }
                        if (state.filters.activeCount > 0) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        state.filters.activeCount.toString(),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
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
                onClearFilters = viewModel::clearFilters,
                onChart = { selectedChart = it },
            )
        }
    }

    if (showFilters) {
        FilterDialog(
            filters = state.filters,
            options = state.options,
            onDismiss = { showFilters = false },
            onApply = {
                viewModel.applyFilters(it)
                showFilters = false
            },
        )
    }
    selectedChart?.let { chart ->
        ChartDetailDialog(chart = chart, onDismiss = { selectedChart = null })
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Offline catalog") },
            text = {
                Text(
                    "${state.info?.songCount ?: 0} songs and ${state.info?.chartCount ?: 0} charts. " +
                        "Metadata: arcade-songs, updated ${state.info?.updateTime?.take(10).orEmpty()}. " +
                        "Community English aliases improve romanised search. Cover art is cached after loading.",
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SortMenu(selected = state.sort, onSelect = onSort)
            if (state.filters.activeCount > 0) {
                AssistChip(
                    onClick = onClearFilters,
                    label = { Text("Clear ${state.filters.activeCount} filters") },
                    leadingIcon = { Icon(Icons.Default.Clear, null, Modifier.size(18.dp)) },
                )
            }
            Text(
                "${state.charts.size} charts",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
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
                    ChartCard(chart, onClick = { onChart(chart) })
                }
            }
        }
    }
}

@Composable
private fun SortMenu(selected: ChartSort, onSelect: (ChartSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { expanded = true },
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
private fun ChartCard(chart: SongChart, onClick: () -> Unit) {
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
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterDialog(
    filters: ChartFilters,
    options: FilterOptions,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Known constants only", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Hide charts without a published decimal constant",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = draft.knownConstantsOnly,
                            onCheckedChange = { draft = draft.copy(knownConstantsOnly = it) },
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
private fun MultiSelectSection(
    title: String,
    options: List<String>,
    selected: Set<String>,
    label: (String) -> String = { it },
    onChange: (Set<String>) -> Unit,
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
private fun ChartDetailDialog(chart: SongChart, onDismiss: () -> Unit) {
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
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
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
