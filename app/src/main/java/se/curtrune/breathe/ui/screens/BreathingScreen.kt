package se.curtrune.breathe.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import se.curtrune.breathe.ui.theme.BreatheTheme
import se.curtrune.breathe.viewmodel.BreathingState
import se.curtrune.breathe.viewmodel.BreathingUiState
import se.curtrune.breathe.viewmodel.BreathingViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    viewModel: BreathingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathe", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        BreathingContent(
            uiState = uiState,
            onStart = { viewModel.startSession() },
            onPause = { viewModel.pauseSession() },
            onResume = { viewModel.resumeSession() },
            onRestart = { viewModel.restartSession() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun BreathingContent(
    uiState: BreathingUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatTime = remember(uiState.remainingTimeSeconds) {
        val minutes = uiState.remainingTimeSeconds / 60
        val seconds = uiState.remainingTimeSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    val statusText = when (uiState.sessionState) {
        BreathingState.IDLE -> "Find a comfortable position & tap Start"
        BreathingState.INHALING -> "Inhale deeply..."
        BreathingState.HOLDING -> "Hold your breath..."
        BreathingState.EXHALING -> "Exhale slowly..."
        BreathingState.PAUSED -> "Session Paused"
        BreathingState.COMPLETED -> "Session Completed 🌟"
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val targetCircleColor = when (uiState.sessionState) {
        BreathingState.INHALING -> primaryColor
        BreathingState.HOLDING -> tertiaryColor
        BreathingState.EXHALING -> secondaryColor
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val animatedCircleColor by animateColorAsState(
        targetValue = targetCircleColor,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CircleColor"
    )

    // Animation progress ranges from 0f (contracted) to 1f (expanded)
    val scale = 0.75f + (uiState.animationProgress * 0.35f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Timer & Session Progress
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                val sessionProgress = if (uiState.totalDurationSeconds > 0) {
                    uiState.remainingTimeSeconds.toFloat() / uiState.totalDurationSeconds.toFloat()
                } else {
                    1f
                }
                CircularProgressIndicator(
                    progress = { sessionProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatTime,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.sessionState != BreathingState.IDLE && uiState.sessionState != BreathingState.COMPLETED) {
                Text(
                    text = "Cycle ${uiState.currentCycle + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Center Section: Visual Breathing Animation & Status Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Ambient outer glow circle
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(scale * 1.15f)
                        .blur(32.dp)
                        .background(animatedCircleColor.copy(alpha = 0.25f), CircleShape)
                )

                // Outer ring
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(scale)
                        .background(animatedCircleColor.copy(alpha = 0.15f), CircleShape)
                )

                // Inner core breathing circle
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    animatedCircleColor,
                                    animatedCircleColor.copy(alpha = 0.8f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (uiState.sessionState) {
                            BreathingState.INHALING -> "Inhale"
                            BreathingState.HOLDING -> "Hold"
                            BreathingState.EXHALING -> "Exhale"
                            BreathingState.PAUSED -> "Paused"
                            BreathingState.COMPLETED -> "Done"
                            BreathingState.IDLE -> "Breathe"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Bottom Section: Session State Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (uiState.sessionState) {
                BreathingState.IDLE, BreathingState.COMPLETED -> {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Start Session",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Session", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                BreathingState.INHALING, BreathingState.HOLDING, BreathingState.EXHALING -> {
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Restart Session",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restart", fontSize = 16.sp)
                    }

                    Button(
                        onClick = onPause,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Pause Session",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                BreathingState.PAUSED -> {
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Restart Session",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restart", fontSize = 16.sp)
                    }

                    Button(
                        onClick = onResume,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Resume Session",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resume", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BreathingScreenPreviewIdle() {
    BreatheTheme {
        BreathingContent(
            uiState = BreathingUiState(sessionState = BreathingState.IDLE),
            onStart = {},
            onPause = {},
            onResume = {},
            onRestart = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BreathingScreenPreviewInhaling() {
    BreatheTheme {
        BreathingContent(
            uiState = BreathingUiState(
                sessionState = BreathingState.INHALING,
                remainingTimeSeconds = 275,
                animationProgress = 0.7f,
                currentCycle = 1
            ),
            onStart = {},
            onPause = {},
            onResume = {},
            onRestart = {}
        )
    }
}
