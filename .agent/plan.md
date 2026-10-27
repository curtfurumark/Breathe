# Project Plan

An animated app for a five minute assisted breathing exercise

## Project Brief

# Project Brief: Breathe (5-Minute Assisted Breathing App)

## Features
1. **Guided Breathing Animation**: Smooth, synchronized visual animation (expanding and contracting shapes) guiding users through inhale and exhale phases for the 5-minute session.
2. **Session Timer & Progress Tracking**: A real-time 5-minute countdown timer with a circular progress indicator displaying remaining session time.
3. **Session State Controls**: Intuitive start, pause, resume, and restart controls allowing users to manage their breathing exercise seamlessly.
4. **Completion & Summary Screen**: A calm completion screen displayed when the 5-minute session finishes, offering options to repeat the exercise or exit.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose & Jetpack Compose Material 3
- **Core Asynchronous Handling**: Kotlin Coroutines and Flows (for smooth UI animations, timers, and state management)
- **Architecture**: MVVM (Model-View-ViewModel) with unidirectional data flow (UDF)
- **Navigation**: Jetpack Navigation 3 (state-driven navigation)
- **Adaptive Strategy**: Compose Material Adaptive library for multi-window/device responsiveness

## Implementation Steps
**Total Duration:** 8m 27s

### Task_1_CoreBreathingEngineAndViewModel: Implement the core breathing state machine, ViewModel, and countdown timer logic for the 5-minute session using Kotlin Coroutines and Flows.
- **Status:** COMPLETED
- **Updates:** Successfully implemented BreathingViewModel, BreathingState, BreathingUiState, countdown timer logic, and unit tests. Verified build and tests pass successfully.
- **Acceptance Criteria:**
  - Session ViewModel handles start, pause, resume, restart states correctly
  - 5-minute countdown timer and inhale/exhale timing cycles operate smoothly using Flows
- **Duration:** 4m 11s

### Task_2_BreathingScreenAndAnimationUI: Build the Jetpack Compose UI for the guided breathing animation (expanding/contracting shapes), circular progress indicator, and session state controls.
- **Status:** COMPLETED
- **Updates:** Successfully implemented BreathingScreen with smooth animated expanding/contracting visualizer, circular countdown timer, status text guidance, session control buttons, and wired into MainActivity. Verified successful build and test execution.
- **Acceptance Criteria:**
  - Smooth synchronized visual breathing animation rendered with Jetpack Compose
  - Circular progress indicator shows remaining session time accurately
  - Controls allow seamless start, pause, resume, and restart
- **Duration:** 52s

### Task_3_SummaryScreenAndNavigation: Implement the session completion & summary screen and setup state-driven navigation between the breathing exercise screen and completion screen.
- **Status:** COMPLETED
- **Updates:** Successfully implemented SummaryScreen and state-driven navigation using Jetpack Navigation, wired completion transition from BreathingScreen to SummaryScreen, added unit tests, and verified successful build and test execution.
- **Acceptance Criteria:**
  - Completion screen displays upon session finish with options to repeat or exit
  - Navigation flows correctly between breathing screen and summary screen
- **Duration:** 57s

### Task_4_AppPolishRunAndVerify: Add app icon and visual assets, polish theme/colors, and run & verify application stability.
- **Status:** COMPLETED
- **Updates:** Successfully completed theme polish, verified all unit tests pass, and critic_agent confirmed app stability, smooth breathing animation, session controls, completion screen navigation, and Material 3 design quality.
- **Acceptance Criteria:**
  - App icon and visual assets added
  - project builds successfully
  - make sure all existing tests pass
  - app does not crash
  - critic_agent verifies application stability and alignment with requirements
- **Duration:** 2m 27s

