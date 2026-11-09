package io.github.brainage04.maidex.data

import android.content.Context

/** App colour theme chosen in Settings; [SYSTEM] follows Android's dark theme setting. */
enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark"),
    ;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        val DEFAULT = SYSTEM

        fun fromStored(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: DEFAULT
    }
}

class AppSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("app-settings", Context.MODE_PRIVATE)

    fun themeMode(): ThemeMode = ThemeMode.fromStored(preferences.getString(KEY_THEME_MODE, null))

    fun setThemeMode(mode: ThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
