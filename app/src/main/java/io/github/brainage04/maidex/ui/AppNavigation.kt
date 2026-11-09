package io.github.brainage04.maidex.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.brainage04.maidex.data.UnlockGuideSection

/** Every screen above the catalog. Each has a stable [key] so its UI state survives being covered. */
sealed interface AppScreen {
    val key: String

    data object Account : AppScreen { override val key = "account" }
    data object Circle : AppScreen { override val key = "circle" }
    data object ScoreStats : AppScreen { override val key = "score-stats" }
    data object DanGuide : AppScreen { override val key = "dan" }
    data object Filters : AppScreen { override val key = "filters" }
    data object PlayCountHistory : AppScreen { override val key = "play-counts" }
    data object ProfileCard : AppScreen { override val key = "profile-card" }
    data object About : AppScreen { override val key = "about" }
    data object Settings : AppScreen { override val key = "settings" }

    data class UnlockGuide(val section: UnlockGuideSection, val entryId: String? = null) : AppScreen {
        override val key = "guide:${section.name}:${entryId.orEmpty()}"
    }

    data class ChartDetail(val chartKey: String) : AppScreen {
        override val key = "chart:$chartKey"
    }

    companion object {
        private val objects = listOf(
            Account, Circle, ScoreStats, DanGuide, Filters, PlayCountHistory, ProfileCard, About, Settings,
        )

        /** Inverse of [key], for restoring the back stack after the activity is recreated. */
        fun fromKey(key: String): AppScreen? {
            objects.firstOrNull { it.key == key }?.let { return it }
            if (key.startsWith("chart:")) return ChartDetail(key.removePrefix("chart:"))
            if (key.startsWith("guide:")) {
                val parts = key.removePrefix("guide:").split(':', limit = 2)
                val section = UnlockGuideSection.entries.firstOrNull { it.name == parts[0] } ?: return null
                return UnlockGuide(section, parts.getOrNull(1)?.takeIf(String::isNotEmpty))
            }
            return null
        }
    }
}

/**
 * The screens open above the catalog, oldest first. Opening a screen that is already the top one
 * does nothing, so a double tap cannot stack the same screen twice.
 */
class AppBackStack(initial: List<AppScreen> = emptyList()) {
    private val screens: SnapshotStateList<AppScreen> = mutableStateListOf<AppScreen>().apply { addAll(initial) }

    val entries: List<AppScreen> get() = screens
    val top: AppScreen? get() = screens.lastOrNull()

    fun push(screen: AppScreen) {
        if (screens.lastOrNull() != screen) screens.add(screen)
    }

    fun pop(): Boolean {
        if (screens.isEmpty()) return false
        screens.removeAt(screens.lastIndex)
        return true
    }

    companion object {
        val Saver: Saver<AppBackStack, Any> = listSaver(
            save = { stack -> stack.screens.map(AppScreen::key) },
            restore = { keys -> AppBackStack(keys.mapNotNull(AppScreen::fromKey)) },
        )
    }
}

/**
 * Full-screen page for everything above the catalog: a top app bar with a back arrow, the title
 * and optional actions, and the system back gesture wired to [onBack].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                        subtitle?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = bottomBar,
        content = content,
    )
}

/** Centred message for a screen or section that has nothing to show yet, with an optional action. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (action != null) {
                Spacer(Modifier.height(4.dp))
                action()
            }
        }
    }
}

/** Search field shared by the guide screens. */
@Composable
fun GuideSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        },
        label = { Text("Search") },
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        shape = RoundedCornerShape(6.dp),
    )
}
