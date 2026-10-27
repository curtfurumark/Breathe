package se.curtrune.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BreathingState {
    IDLE,
    INHALING,
    HOLDING,
    EXHALING,
    PAUSED,
    COMPLETED
}

data class BreathPattern(
    val inhaleSeconds: Int = 4,
    val holdSeconds: Int = 4,
    val exhaleSeconds: Int = 4,
    val postExhaleHoldSeconds: Int = 0
) {
    val totalCycleSeconds: Int
        get() = inhaleSeconds + holdSeconds + exhaleSeconds + postExhaleHoldSeconds
}

data class BreathingUiState(
    val sessionState: BreathingState = BreathingState.IDLE,
    val remainingTimeSeconds: Int = 300,
    val totalDurationSeconds: Int = 300,
    val phaseProgress: Float = 0f, // 0f to 1f within current phase
    val animationProgress: Float = 0f, // 0f (empty/min) to 1f (full/max)
    val currentCycle: Int = 0,
    val pattern: BreathPattern = BreathPattern()
)

class BreathingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BreathingUiState())
    val uiState: StateFlow<BreathingUiState> = _uiState.asStateFlow()

    private var breathingJob: Job? = null
    private var stateBeforePause: BreathingState = BreathingState.IDLE
    private var elapsedSessionMillis: Long = 0L
    private var totalSessionMillis: Long = 300_000L

    fun startSession(durationSeconds: Int = 300, pattern: BreathPattern = BreathPattern()) {
        breathingJob?.cancel()
        elapsedSessionMillis = 0L
        totalSessionMillis = durationSeconds * 1000L

        _uiState.update {
            it.copy(
                sessionState = BreathingState.INHALING,
                remainingTimeSeconds = durationSeconds,
                totalDurationSeconds = durationSeconds,
                phaseProgress = 0f,
                animationProgress = 0f,
                currentCycle = 0,
                pattern = pattern
            )
        }

        startBreathingLoop()
    }

    fun pauseSession() {
        val currentState = _uiState.value.sessionState
        if (currentState in listOf(BreathingState.INHALING, BreathingState.HOLDING, BreathingState.EXHALING)) {
            stateBeforePause = currentState
            breathingJob?.cancel()
            _uiState.update { it.copy(sessionState = BreathingState.PAUSED) }
        }
    }

    fun resumeSession() {
        if (_uiState.value.sessionState == BreathingState.PAUSED) {
            _uiState.update { it.copy(sessionState = stateBeforePause) }
            startBreathingLoop()
        }
    }

    fun restartSession() {
        breathingJob?.cancel()
        val currentPattern = _uiState.value.pattern
        val totalDuration = _uiState.value.totalDurationSeconds
        startSession(totalDuration, currentPattern)
    }

    private fun startBreathingLoop() {
        breathingJob?.cancel()
        breathingJob = viewModelScope.launch {
            val tickIntervalMillis = 50L // 20fps for smooth animation progress
            val pattern = _uiState.value.pattern
            val cycleDurationMillis = pattern.totalCycleSeconds * 1000L
            val inhaleMillis = pattern.inhaleSeconds * 1000L
            val holdMillis = pattern.holdSeconds * 1000L
            val exhaleMillis = pattern.exhaleSeconds * 1000L
            val postExhaleHoldMillis = pattern.postExhaleHoldSeconds * 1000L

            while (elapsedSessionMillis < totalSessionMillis) {
                delay(tickIntervalMillis)
                elapsedSessionMillis += tickIntervalMillis

                val remainingMillis = (totalSessionMillis - elapsedSessionMillis).coerceAtLeast(0L)
                val remainingSeconds = (remainingMillis + 999L) / 1000L

                if (remainingMillis <= 0L) {
                    _uiState.update {
                        it.copy(
                            sessionState = BreathingState.COMPLETED,
                            remainingTimeSeconds = 0,
                            phaseProgress = 0f,
                            animationProgress = 0f
                        )
                    }
                    break
                }

                val cycleTimeMillis = elapsedSessionMillis % cycleDurationMillis
                val currentCycleCount = (elapsedSessionMillis / cycleDurationMillis).toInt()

                val currentState: BreathingState
                val phaseProgress: Float
                val animationProgress: Float

                when {
                    cycleTimeMillis < inhaleMillis -> {
                        currentState = BreathingState.INHALING
                        phaseProgress = cycleTimeMillis.toFloat() / inhaleMillis.toFloat()
                        animationProgress = phaseProgress.coerceIn(0f, 1f)
                    }
                    cycleTimeMillis < inhaleMillis + holdMillis -> {
                        currentState = BreathingState.HOLDING
                        phaseProgress = (cycleTimeMillis - inhaleMillis).toFloat() / holdMillis.toFloat()
                        animationProgress = 1f
                    }
                    cycleTimeMillis < inhaleMillis + holdMillis + exhaleMillis -> {
                        currentState = BreathingState.EXHALING
                        val exhaleElapsed = cycleTimeMillis - inhaleMillis - holdMillis
                        phaseProgress = exhaleElapsed.toFloat() / exhaleMillis.toFloat()
                        animationProgress = (1f - phaseProgress).coerceIn(0f, 1f)
                    }
                    else -> {
                        currentState = BreathingState.HOLDING // post-exhale hold
                        val postHoldElapsed = cycleTimeMillis - inhaleMillis - holdMillis - exhaleMillis
                        phaseProgress = postHoldElapsed.toFloat() / postExhaleHoldMillis.toFloat()
                        animationProgress = 0f
                    }
                }

                _uiState.update {
                    it.copy(
                        sessionState = currentState,
                        remainingTimeSeconds = remainingSeconds.toInt(),
                        phaseProgress = phaseProgress,
                        animationProgress = animationProgress,
                        currentCycle = currentCycleCount
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        breathingJob?.cancel()
    }
}
