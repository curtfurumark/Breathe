package se.curtrune.breathe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import se.curtrune.breathe.viewmodel.BreathPattern
import se.curtrune.breathe.viewmodel.BreathingState
import se.curtrune.breathe.viewmodel.BreathingViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class BreathingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BreathingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = BreathingViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdle() = runTest {
        val state = viewModel.uiState.value
        assertEquals(BreathingState.IDLE, state.sessionState)
        assertEquals(300, state.remainingTimeSeconds)
        assertEquals(300, state.totalDurationSeconds)
    }

    @Test
    fun startSession_setsStateToInhaling() = runTest {
        viewModel.startSession(durationSeconds = 60, pattern = BreathPattern(2, 2, 2))
        testScheduler.runCurrent()
        
        val state = viewModel.uiState.value
        assertEquals(BreathingState.INHALING, state.sessionState)
        assertEquals(60, state.remainingTimeSeconds)
    }

    @Test
    fun pauseAndResumeSession_worksCorrectly() = runTest {
        viewModel.startSession(durationSeconds = 60, pattern = BreathPattern(2, 2, 2))
        testScheduler.runCurrent()

        viewModel.pauseSession()
        assertEquals(BreathingState.PAUSED, viewModel.uiState.value.sessionState)

        viewModel.resumeSession()
        assertEquals(BreathingState.INHALING, viewModel.uiState.value.sessionState)
    }

    @Test
    fun restartSession_resetsSession() = runTest {
        viewModel.startSession(durationSeconds = 60, pattern = BreathPattern(2, 2, 2))
        testScheduler.runCurrent()

        viewModel.restartSession()
        testScheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals(BreathingState.INHALING, state.sessionState)
        assertEquals(60, state.remainingTimeSeconds)
    }

    @Test
    fun sessionProgressesThroughPhases() = runTest {
        val pattern = BreathPattern(inhaleSeconds = 1, holdSeconds = 1, exhaleSeconds = 1)
        viewModel.startSession(durationSeconds = 10, pattern = pattern)
        
        testScheduler.runCurrent()
        assertEquals(BreathingState.INHALING, viewModel.uiState.value.sessionState)

        testScheduler.advanceTimeBy(1000L)
        testScheduler.runCurrent()
        assertEquals(BreathingState.HOLDING, viewModel.uiState.value.sessionState)

        testScheduler.advanceTimeBy(1000L)
        testScheduler.runCurrent()
        assertEquals(BreathingState.EXHALING, viewModel.uiState.value.sessionState)
    }

    @Test
    fun sessionCompletionTriggersCompletedState() = runTest {
        val pattern = BreathPattern(inhaleSeconds = 1, holdSeconds = 0, exhaleSeconds = 1, postExhaleHoldSeconds = 0)
        viewModel.startSession(durationSeconds = 2, pattern = pattern)
        
        testScheduler.runCurrent()
        assertEquals(BreathingState.INHALING, viewModel.uiState.value.sessionState)

        testScheduler.advanceTimeBy(3000L)
        testScheduler.runCurrent()
        assertEquals(BreathingState.COMPLETED, viewModel.uiState.value.sessionState)
    }
}
