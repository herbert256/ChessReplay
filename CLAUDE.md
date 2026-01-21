# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Deploy Commands

```bash
# Build debug APK (requires Java 17)
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebug

# Build release APK (requires keystore in local.properties)
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleRelease

# Clean build
./gradlew clean

# Deploy to emulator (also copy to cloud folder)
adb install -r app/build/outputs/apk/debug/app-debug.apk && \
adb shell am start -n com.chessreplay/.MainActivity && \
cp app/build/outputs/apk/debug/app-debug.apk /Users/herbert/cloud/
```

## Project Overview

Chess Replay is an Android app for fetching and analyzing chess games from Lichess.org using the Stockfish 17.1 chess engine. The app retrieves games via the Lichess API, parses PGN notation, and provides multi-stage computer analysis with an interactive board display.

**Key Dependencies:**
- External app required: "Stockfish 17.1 Chess Engine" (com.stockfish141) from Google Play Store
- Android SDK: minSdk 26, targetSdk 34, compileSdk 34
- Kotlin with Jetpack Compose for UI
- Retrofit for networking (Lichess API)

## Architecture

### Package Structure (20 Kotlin files, ~7,260 lines)

```
com.chessreplay/
├── MainActivity.kt (33 lines) - Entry point, sets up Compose theme
├── chess/
│   ├── ChessBoard.kt (533 lines) - Board state, move validation, FEN generation
│   └── PgnParser.kt (70 lines) - PGN parsing with clock time extraction
├── data/
│   ├── LichessApi.kt (48 lines) - Retrofit interface for Lichess NDJSON API
│   ├── LichessModels.kt (40 lines) - Data classes: LichessGame, Players, Clock
│   └── LichessRepository.kt (56 lines) - Repository with sealed Result<T> type
├── stockfish/
│   └── StockfishEngine.kt (504 lines) - UCI protocol wrapper, process management
└── ui/
    ├── GameViewModel.kt (1,781 lines) - Central state management, analysis orchestration
    ├── GameScreen.kt (484 lines) - Main screen, Lichess username input, Stockfish check
    ├── GameContent.kt (738 lines) - Game display: board, players, moves, result bar
    ├── ChessBoardView.kt (518 lines) - Canvas-based interactive chess board with arrows
    ├── AnalysisComponents.kt (455 lines) - Evaluation graph, analysis panel
    ├── MovesDisplay.kt (195 lines) - Move list with scores and piece symbols
    ├── GameSelectionDialog.kt (190 lines) - Dialog for selecting from multiple games
    ├── SettingsScreen.kt (214 lines) - Settings navigation hub
    ├── StockfishSettingsScreen.kt (496 lines) - Engine settings for all 3 stages
    ├── ArrowSettingsScreen.kt (336 lines) - Arrow display configuration (3 cards)
    ├── BoardLayoutSettingsScreen.kt (284 lines) - Board colors, pieces, coordinates
    ├── ColorPickerDialog.kt (254 lines) - HSV color picker for colors
    └── theme/Theme.kt (32 lines) - Material3 dark theme
```

### Key Data Classes

```kotlin
// Analysis stages
enum class AnalysisStage { PREVIEW, ANALYSE, MANUAL }

// Arrow modes
enum class ArrowMode { NONE, MAIN_LINE, MULTI_LINES }

// Settings for each stage
data class PreviewStageSettings(secondsForMove, threads, hashMb, useNnue)
data class AnalyseStageSettings(secondsForMove, threads, hashMb, useNnue)
data class ManualStageSettings(depth, threads, hashMb, multiPv, useNnue,
    arrowMode, numArrows, showArrowNumbers, whiteArrowColor, blackArrowColor, multiLinesArrowColor)

// Board appearance
data class BoardLayoutSettings(showCoordinates, showLastMove,
    whiteSquareColor, blackSquareColor, whitePieceColor, blackPieceColor)

// UI state (30+ fields)
data class GameUiState(stockfishInstalled, isLoading, game, currentBoard,
    currentMoveIndex, analysisResult, currentStage, previewScores, analyseScores,
    isExploringLine, stockfishSettings, boardLayoutSettings, ...)
```

### Key Design Patterns

1. **MVVM with Jetpack Compose**: `GameViewModel` exposes `StateFlow<GameUiState>`, UI recomposes reactively

2. **Three-Stage Analysis System**: Games progress through PREVIEW → ANALYSE → MANUAL stages automatically

3. **Arrow System with 3 Modes**:
   - NONE: No arrows displayed
   - MAIN_LINE: Multiple arrows from PV line (1-8 arrows, colored by side, numbered)
   - MULTI_LINES: One arrow per Stockfish line with evaluation score displayed

4. **Piece Color Tinting**: Uses white piece images with ColorFilter.Modulate for custom colors (black pieces use white images when custom-colored)

5. **SharedPreferences Persistence**: All settings saved, with version tracking for defaults reset on app updates

## Analysis Stages

### 1. Preview Stage
- **Purpose**: Quick initial scan of all positions
- **Timing**: 100ms per move (configurable: 10ms-500ms)
- **Direction**: Forward through game (move 0 → end)
- **Settings**: 1 thread, 16MB hash, NNUE disabled
- **UI**: Board hidden, only evaluation graph shown
- **Interruptible**: No

### 2. Analyse Stage
- **Purpose**: Deep analysis focusing on critical positions
- **Timing**: 1 second per move (configurable: 500ms-10s)
- **Direction**: Backward through game (end → move 0)
- **Settings**: 2 threads, 32MB hash, NNUE enabled
- **UI**: Full display, clickable "Tap here for manual stage" banner (yellow)
- **Interruptible**: Yes (click to enter Manual at biggest evaluation change)

### 3. Manual Stage
- **Purpose**: Interactive exploration with real-time analysis
- **Analysis**: Depth-based (default 24), MultiPV support (1-6 lines)
- **Settings**: 4 threads, 64MB hash, NNUE enabled
- **Features**:
  - Draggable board navigation
  - Three arrow modes (cycle with top-bar icon)
  - Line exploration (click PV moves to explore variations)
  - "Back to game" button when exploring
  - Evaluation graph with current position indicator

## UI Components

### Title Bar Icons (left to right when game loaded)
- **↻** : Reload last game from Lichess
- **≡** : Return to game selection
- **↗** : Arrow mode toggle (gray=none, white=main line, blue=multi lines)
- **Chess Replay** : Title (centered)
- **⚙** : Settings

### Settings Structure
```
Settings (main menu)
├── Board layout
│   ├── Show coordinates (Yes/No)
│   ├── Show last move (Yes/No)
│   ├── White squares color (color picker)
│   ├── Black squares color (color picker)
│   ├── White pieces color (color picker)
│   ├── Black pieces color (color picker)
│   └── Reset to defaults (button)
├── Arrow settings
│   ├── Card 1: Draw arrows (None / Main line / Multi lines)
│   ├── Card 2 "Main line": numArrows, showNumbers, white/black colors
│   └── Card 3 "Multi lines": arrow color
└── Stockfish
    ├── Preview Stage: seconds, threads, hash, NNUE
    ├── Analyse Stage: seconds, threads, hash, NNUE
    └── Manual Stage: depth, threads, hash, multiPV, NNUE
```

### Color Conventions
- **Score Colors**: Red = white better (+), Green = black better (-)
- **Main Line Arrows**: Blue (default) for white moves, Green (default) for black moves
- **Multi Lines Arrows**: Single configurable color (default blue)
- **Evaluation Graph**: Bright red (#FF5252) above axis, bright green (#00E676) below
- **Background Color**: Changes based on game result (green=win, red=loss, blue=draw)

## Stockfish Integration

### Engine Management (`StockfishEngine.kt`)
- **Requirement**: External "Stockfish 17.1 Chess Engine" app must be installed
- **Detection**: `isStockfishInstalled()` checks for `com.stockfish141` package
- **Binary Location**: Uses native library from system app (`lib_sf171.so`)
- **Process Control**: Managed via `ProcessBuilder`, UCI protocol communication
- **Restart Sequence**: `stop()` → `newGame()` → `delay(100ms)` → start analysis

### Analysis Output
```kotlin
data class AnalysisResult(
    val depth: Int,
    val nodes: Long,
    val lines: List<PvLine>  // Multiple principal variations
)

data class PvLine(
    val score: Float,      // Centipawns / 100
    val isMate: Boolean,
    val mateIn: Int,
    val pv: String,        // Space-separated UCI moves
    val multipv: Int       // Line number (1-6)
)
```

## Settings Persistence

SharedPreferences keys in `chess_replay_prefs`:

```
// Lichess
lichess_username, lichess_max_games

// Preview stage
preview_seconds, preview_threads, preview_hash, preview_nnue

// Analyse stage
analyse_seconds, analyse_threads, analyse_hash, analyse_nnue

// Manual stage
manual_depth, manual_threads, manual_hash, manual_multipv, manual_nnue
manual_arrow_mode, manual_numarrows, manual_shownumbers
manual_white_arrow_color, manual_black_arrow_color, manual_multilines_arrow_color

// Board layout
board_show_coordinates, board_show_last_move
board_white_square_color, board_black_square_color
board_white_piece_color, board_black_piece_color

// App versioning
first_game_retrieved_version
```

## Common Tasks

### Adding a New Setting
1. Add field to appropriate settings data class in `GameViewModel.kt`
2. Add SharedPreferences key constant in companion object
3. Update `loadStockfishSettings()` or `loadBoardLayoutSettings()`
4. Update corresponding save function
5. Add UI control in appropriate settings screen
6. Use setting value in relevant code

### Modifying Arrow Behavior
1. Check `ArrowMode` enum in `GameViewModel.kt`
2. Update `MoveArrow` data class in `ChessBoardView.kt` if needed
3. Modify arrow generation in `GameContent.kt` (around line 308)
4. Update arrow drawing in `ChessBoardView.kt` (around line 288)

### Changing Board Display
1. `ChessBoardView.kt` - Canvas drawing, gestures, arrows, piece tinting
2. `GameContent.kt` - Layout, player bars, result bar
3. `AnalysisComponents.kt` - Evaluation graph, analysis panel

### Triggering Stockfish Analysis
Use `restartAnalysisForExploringLine()` for proper restart sequence:
- Stops current analysis
- Sends newGame command
- Waits 100ms
- Starts fresh analysis
