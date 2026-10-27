package se.curtrune.breathe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import se.curtrune.breathe.ui.screens.BreathingScreen
import se.curtrune.breathe.ui.screens.SummaryScreen
import se.curtrune.breathe.ui.theme.BreatheTheme
import se.curtrune.breathe.viewmodel.BreathingState
import se.curtrune.breathe.viewmodel.BreathingViewModel

sealed interface BreatheRoute {
    data object Breathing : BreatheRoute
    data object Summary : BreatheRoute
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BreathingViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val backStack = remember { mutableStateListOf<Any>(BreatheRoute.Breathing) }

            // State-driven navigation: when session state becomes COMPLETED, navigate to Summary
            LaunchedEffect(uiState.sessionState) {
                if (uiState.sessionState == BreathingState.COMPLETED) {
                    if (backStack.lastOrNull() != BreatheRoute.Summary) {
                        backStack.add(BreatheRoute.Summary)
                    }
                }
            }

            BreatheTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            } else {
                                finish()
                            }
                        },
                        entryProvider = { key ->
                            when (key) {
                                is BreatheRoute.Breathing -> NavEntry(key) {
                                    BreathingScreen(viewModel = viewModel)
                                }
                                is BreatheRoute.Summary -> NavEntry(key) {
                                    SummaryScreen(
                                        uiState = uiState,
                                        onDoAnotherSession = {
                                            viewModel.restartSession()
                                            backStack.clear()
                                            backStack.add(BreatheRoute.Breathing)
                                        },
                                        onExit = {
                                            finish()
                                        }
                                    )
                                }
                                else -> error("Unknown route: $key")
                            }
                        }
                    )
                }
            }
        }
    }
}
