package io.github.brainage04.maidex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import io.github.brainage04.maidex.ui.MaiDexApp
import io.github.brainage04.maidex.ui.theme.MaiDexTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaiDexTheme {
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
