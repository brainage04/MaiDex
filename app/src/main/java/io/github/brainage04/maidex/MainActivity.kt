package io.github.brainage04.maidex

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.brainage04.maidex.ui.MaiDexApp
import io.github.brainage04.maidex.ui.theme.MaiDexTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = themeMode.isDark(isSystemInDarkTheme())
            // The in-app theme can differ from the system one, so the status and navigation bar
            // icons follow the app's theme rather than Android's.
            DisposableEffect(darkTheme) {
                val style = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
            MaiDexTheme(darkTheme = darkTheme) {
                MaiDexApp(viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Opening (or returning to) the app triggers the opt-in automatic DX NET update; the
        // worker itself throttles, so this is cheap when it is disabled or ran recently.
        viewModel.requestAutoUpdateOnAppOpen()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTrackingData()
    }
}
