package dev.thomas.maidex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dev.thomas.maidex.ui.MaiDexApp
import dev.thomas.maidex.ui.theme.MaiDexTheme

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

    override fun onResume() {
        super.onResume()
        viewModel.refreshTrackingData()
    }
}
