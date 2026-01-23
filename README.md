# Eval - Chess Game Analysis App

A powerful Android application for fetching and analyzing chess games from Lichess.org and Chess.com using the Stockfish 17.1 chess engine and AI-powered position analysis from 6 leading AI services.

## Overview

Eval provides comprehensive game analysis through an intelligent three-stage system that automatically identifies critical positions and mistakes. Whether you're reviewing your own games or studying master games from tournaments and broadcasts, Eval gives you deep insights into every position.

**Statistics:** 44 Kotlin files, ~25,000 lines of code

## Features

### Game Sources

#### Online Sources
- **Lichess.org** - Fetch games by username, browse tournaments, watch broadcasts, view TV channels, follow streamers
- **Chess.com** - Fetch games by username, solve daily puzzle, view leaderboards
- **Live Games** - Follow games in real-time with automatic move updates

#### Local Sources
- **PGN Files** - Import PGN files (including ZIP archives) with multi-game event grouping
- **Opening Study** - Start from any ECO opening (A00-E99)
- **FEN Position** - Analyze any position by entering FEN notation (with history)
- **Game History** - Access previously analyzed games

### Three-Stage Analysis System

#### 1. Preview Stage
- **Quick Scan**: Rapidly evaluates all positions (50ms per move)
- **Forward Direction**: Analyzes from move 1 to the end
- **Visual Progress**: Watch the evaluation graph build in real-time
- **Automatic**: Completes fully before proceeding

#### 2. Analyse Stage
- **Deep Analysis**: Thorough evaluation (1 second per move)
- **Reverse Direction**: Analyzes from the end back to move 1
- **Interruptible**: Tap to jump to the most critical position
- **Dual Graphs**: Deep analysis line overlaid on preview scores

#### 3. Manual Stage
- **Interactive Exploration**: Navigate freely through the game
- **Real-Time Analysis**: Depth-based analysis (32 ply default)
- **Multiple Variations**: View up to 32 principal variations
- **Line Exploration**: Click any move to explore variations
- **AI Analysis**: Get insights from 6 AI services
- **Opening Explorer**: View position statistics

### AI Position Analysis

Get intelligent analysis from 6 leading AI services:

| Service | Models |
|---------|--------|
| **ChatGPT** (OpenAI) | GPT-4o, GPT-4o-mini, and more |
| **Claude** (Anthropic) | Claude 4 Sonnet, Claude 4 Opus |
| **Gemini** (Google) | Gemini 2.0 Flash, Gemini Pro |
| **Grok** (xAI) | Grok-3 and variants |
| **DeepSeek** | DeepSeek Chat |
| **Mistral** | Mistral Small and variants |

#### AI Features
- **Custom Prompts**: Customize analysis prompts with @FEN@ placeholder
- **Dynamic Models**: Automatically fetches available models from each service
- **Rich HTML Reports**: View in Chrome with interactive chessboard, graphs, and move list
- **Email Export**: Send reports as email attachments
- **AI Reports**: Analyze multiple positions with multiple services

### Interactive Chess Board

- **High-Quality Pieces**: Beautiful piece images with customizable colors
- **Move Highlighting**: Yellow squares show the last move
- **Evaluation Bar**: Vertical bar showing position evaluation
- **Three Arrow Modes**:
  - **None**: Clean board
  - **Main Line**: Numbered sequence of best moves (blue for White, green for Black)
  - **Multi Lines**: One arrow per engine line with evaluation score
- **Board Flipping**: Auto-flips when you played Black
- **Graph Navigation**: Tap or drag on evaluation graph to jump to any position
- **Move Sounds**: Optional audio feedback

### Result Bar

- Shows current move with piece symbol and coordinates
- Displays evaluation score + delta from previous move
- Color-coded delta indicates if move was good (green), bad (red), or neutral (blue)
- Example: `+2.1 / -0.8` means position is +2.1 for active player, and this move lost 0.8

### Player Bars

Four display modes:
- **None**: No player information
- **Top**: Single combined bar at top
- **Bottom**: Single combined bar at bottom
- **Both**: Separate bars above and below board (default)

Shows player names, ratings, clock times, and game result indicators.

### Export Features

- **PGN Export**: Full PGN with headers and evaluation comments
- **GIF Export**: Animated replay of the game
- **AI Reports**: HTML report with board, graphs, and AI analysis

### Customization

Extensive settings for:
- **Board Layout**: Square colors, piece colors, coordinates, last move highlight
- **Evaluation Bar**: Position, colors, range
- **Graphs**: Colors for positive/negative scores, ranges
- **Arrows**: Mode, count, colors for each side
- **Stockfish**: Per-stage settings (time, threads, hash, NNUE, MultiPV)
- **AI Services**: API keys, models, custom prompts
- **Interface**: Visibility of each UI element per analysis stage
- **General**: Fullscreen mode, pagination, move sounds

## Requirements

### System Requirements
- Android 8.0 (API 26) or higher
- ~50 MB storage space

### Required External App
**Important**: Eval requires the "Stockfish 17.1 Chess Engine" app from Google Play Store.

Package: `com.stockfish141`

The app displays installation instructions if Stockfish is not detected.

### Optional: AI API Keys
To use AI position analysis, obtain API keys from:
- OpenAI: https://platform.openai.com/api-keys
- Anthropic: https://console.anthropic.com/
- Google: https://makersuite.google.com/app/apikey
- xAI: https://console.x.ai/
- DeepSeek: https://platform.deepseek.com/
- Mistral: https://console.mistral.ai/

## Installation

### From APK
1. Install "Stockfish 17.1 Chess Engine" from Google Play Store
2. Download the Eval APK
3. Enable "Install from unknown sources" if prompted
4. Install and launch Eval

### Building from Source
```bash
# Clone the repository
git clone https://github.com/your-repo/Eval.git
cd Eval

# Build debug APK (requires Java 17)
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.eval/.MainActivity
```

## Usage Guide

### Getting Started
1. Launch the app (ensure Stockfish is installed)
2. Tap Lichess or Chess.com to enter a username
3. Set the number of games to retrieve
4. Tap "Retrieve" to fetch games
5. Select a game to analyze

### During Analysis
1. **Preview Stage**: Watch the evaluation graph build (~5-10 seconds)
2. **Analyse Stage**: Deep analysis runs - tap banner to skip ahead
3. **Manual Stage**: Explore freely, use AI analysis, examine positions

### Navigation Controls
- **⏮** : Go to start
- **◀** : Previous move
- **▶** : Next move
- **⏭** : Go to end
- **↻** : Flip board

### Top Bar Controls
- **↻** : Reload latest game
- **≡** : Return to retrieve screen
- **↗** : Cycle arrow mode
- **⚙** : Settings
- **?** : Help

### Using AI Analysis
1. Configure API keys in Settings > AI Analysis
2. In Manual stage, tap AI logos next to the board
3. View analysis in popup dialog
4. Use "View in Chrome" for rich HTML report
5. Use "Send by email" to share

### Exploring Variations
1. In Manual stage, view the analysis panel
2. Click any move in a variation to explore
3. Use "Back to game" to return
4. Click main moves list to return to actual game

## Technical Details

### Architecture
- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with StateFlow
- **Networking**: Retrofit with OkHttp
- **Chess Engine**: Stockfish 17.1 via UCI protocol

### Project Structure
```
com.eval/
├── MainActivity.kt          # Entry point
├── chess/                   # Board state, PGN parsing
├── data/                    # APIs, repositories, models
├── stockfish/               # UCI protocol wrapper
├── export/                  # PGN, GIF export
├── audio/                   # Move sounds
└── ui/                      # Compose screens, ViewModel
```

### Key Design Decisions

1. **External Stockfish**: Uses system-installed engine, reducing APK size
2. **Three-Stage Analysis**: Immediate feedback with thorough analysis
3. **Score Perspective**: All scores from active player's perspective
4. **Helper Classes**: ViewModel split for maintainability (~7 helper classes)
5. **Rich HTML Reports**: Interactive chessboard.js visualization

## Privacy

- All data stored locally on device
- API keys stored in local SharedPreferences only
- Network requests only to:
  - Lichess.org/Chess.com for game retrieval
  - AI services for position analysis (only FEN sent)
- No tracking or analytics

## License

This project is provided as-is for personal use in analyzing chess games.

## Acknowledgments

- **Lichess.org** - Excellent free API
- **Chess.com** - Public game API
- **Stockfish Team** - World's strongest open-source chess engine
- **Jetpack Compose** - Modern Android UI
- **OpenAI, Anthropic, Google, xAI, DeepSeek, Mistral** - AI analysis
- **chessboard.js** - HTML board visualization

---

*Eval - Understand your games, improve your play.*
