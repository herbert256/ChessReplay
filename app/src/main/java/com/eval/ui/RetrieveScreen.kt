package com.eval.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eval.data.ChessServer
import com.eval.data.LeaderboardPlayer

/**
 * Sub-screen navigation for retrieve screen.
 */
private enum class RetrieveSubScreen {
    MAIN,
    LICHESS,
    CHESS_COM,
    TOP_RANKINGS_LICHESS,
    TOP_RANKINGS_CHESS_COM
}

/**
 * Retrieve screen for fetching chess games from Lichess or Chess.com.
 */
@Composable
fun RetrieveScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onBack: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(RetrieveSubScreen.MAIN) }
    // Track which screen we came from when showing player info
    var previousScreen by remember { mutableStateOf(RetrieveSubScreen.MAIN) }

    // Show player info screen if requested (from top rankings)
    if (uiState.showPlayerInfoScreen) {
        PlayerInfoScreen(
            playerInfo = uiState.playerInfo,
            isLoading = uiState.playerInfoLoading,
            error = uiState.playerInfoError,
            games = uiState.playerGames,
            gamesLoading = uiState.playerGamesLoading,
            currentPage = uiState.playerGamesPage,
            pageSize = uiState.playerGamesPageSize,
            hasMoreGames = uiState.playerGamesHasMore,
            onNextPage = { viewModel.nextPlayerGamesPage() },
            onPreviousPage = { viewModel.previousPlayerGamesPage() },
            onGameSelected = { game -> viewModel.selectGameFromPlayerInfo(game) },
            onDismiss = {
                viewModel.dismissPlayerInfo()
                // Go back to the top rankings screen we came from
                currentScreen = previousScreen
            }
        )
        return
    }

    // Handle back navigation
    BackHandler {
        when (currentScreen) {
            RetrieveSubScreen.MAIN -> onBack()
            RetrieveSubScreen.LICHESS, RetrieveSubScreen.CHESS_COM -> currentScreen = RetrieveSubScreen.MAIN
            RetrieveSubScreen.TOP_RANKINGS_LICHESS -> currentScreen = RetrieveSubScreen.LICHESS
            RetrieveSubScreen.TOP_RANKINGS_CHESS_COM -> currentScreen = RetrieveSubScreen.CHESS_COM
        }
    }

    when (currentScreen) {
        RetrieveSubScreen.MAIN -> RetrieveMainScreen(
            uiState = uiState,
            viewModel = viewModel,
            onBack = onBack,
            onLichessClick = { currentScreen = RetrieveSubScreen.LICHESS },
            onChessComClick = { currentScreen = RetrieveSubScreen.CHESS_COM }
        )
        RetrieveSubScreen.LICHESS -> LichessRetrieveScreen(
            viewModel = viewModel,
            uiState = uiState,
            onBack = { currentScreen = RetrieveSubScreen.MAIN },
            onTopRankingsClick = {
                viewModel.showTopRankings(ChessServer.LICHESS)
                previousScreen = RetrieveSubScreen.TOP_RANKINGS_LICHESS
                currentScreen = RetrieveSubScreen.TOP_RANKINGS_LICHESS
            }
        )
        RetrieveSubScreen.CHESS_COM -> ChessComRetrieveScreen(
            viewModel = viewModel,
            uiState = uiState,
            onBack = { currentScreen = RetrieveSubScreen.MAIN },
            onTopRankingsClick = {
                viewModel.showTopRankings(ChessServer.CHESS_COM)
                previousScreen = RetrieveSubScreen.TOP_RANKINGS_CHESS_COM
                currentScreen = RetrieveSubScreen.TOP_RANKINGS_CHESS_COM
            }
        )
        RetrieveSubScreen.TOP_RANKINGS_LICHESS -> TopRankingsScreen(
            viewModel = viewModel,
            uiState = uiState,
            server = ChessServer.LICHESS,
            onBack = { currentScreen = RetrieveSubScreen.LICHESS },
            onPlayerClick = { player ->
                previousScreen = RetrieveSubScreen.TOP_RANKINGS_LICHESS
                viewModel.selectTopRankingPlayer(player.username, ChessServer.LICHESS)
            }
        )
        RetrieveSubScreen.TOP_RANKINGS_CHESS_COM -> TopRankingsScreen(
            viewModel = viewModel,
            uiState = uiState,
            server = ChessServer.CHESS_COM,
            onBack = { currentScreen = RetrieveSubScreen.CHESS_COM },
            onPlayerClick = { player ->
                previousScreen = RetrieveSubScreen.TOP_RANKINGS_CHESS_COM
                viewModel.selectTopRankingPlayer(player.username, ChessServer.CHESS_COM)
            }
        )
    }
}

/**
 * Main retrieve screen with options to select Lichess or Chess.com.
 */
@Composable
private fun RetrieveMainScreen(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onLichessClick: () -> Unit,
    onChessComClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("< Back", color = Color.White)
            }
            Text(
                text = "Select a game",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Button to select from previous retrieves
            if (uiState.hasPreviousRetrieves) {
                Button(
                    onClick = { viewModel.showPreviousRetrieves() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3A5A7C)
                    )
                ) {
                    Text("Select from a previous retrieve")
                }
            }

            // Button to select from previous analysed games
            if (uiState.hasAnalysedGames) {
                Button(
                    onClick = { viewModel.showAnalysedGames() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3A5A7C)
                    )
                ) {
                    Text("Select from previous analysed games")
                }
            }

            if (uiState.hasPreviousRetrieves || uiState.hasAnalysedGames) {
                HorizontalDivider(color = Color(0xFF404040), modifier = Modifier.padding(vertical = 8.dp))
            }

            // Error message
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = Color.White,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Lichess card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLichessClick() },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF629924) // Lichess green
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "lichess.org",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = ">",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            // Chess.com card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChessComClick() },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF769656) // Chess.com green
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "chess.com",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = ">",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF6B9BFF))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching games...",
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lichess retrieve screen.
 */
@Composable
private fun LichessRetrieveScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onBack: () -> Unit,
    onTopRankingsClick: () -> Unit
) {
    var username by remember { mutableStateOf(viewModel.savedLichessUsername) }
    val focusManager = LocalFocusManager.current

    // Handle back navigation
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("< Back", color = Color.White)
            }
            Text(
                text = "lichess.org",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF629924),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = Color.White,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                placeholder = { Text("Enter Lichess username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF555555),
                    focusedBorderColor = Color(0xFF629924),
                    unfocusedLabelColor = Color(0xFFAAAAAA),
                    focusedLabelColor = Color(0xFF629924)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Retrieve button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (username.isNotBlank()) {
                        viewModel.fetchGames(ChessServer.LICHESS, username)
                    }
                },
                enabled = !uiState.isLoading && username.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF629924)
                )
            ) {
                Text(
                    text = "Retrieve games",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top rankings button
            OutlinedButton(
                onClick = onTopRankingsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF629924)
                )
            ) {
                Text(
                    text = "Select from top rankings",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF629924))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching games from Lichess...",
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chess.com retrieve screen.
 */
@Composable
private fun ChessComRetrieveScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onBack: () -> Unit,
    onTopRankingsClick: () -> Unit
) {
    var username by remember { mutableStateOf(viewModel.savedChessComUsername) }
    val focusManager = LocalFocusManager.current

    // Handle back navigation
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("< Back", color = Color.White)
            }
            Text(
                text = "chess.com",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF769656),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = Color.White,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                placeholder = { Text("Enter Chess.com username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF555555),
                    focusedBorderColor = Color(0xFF769656),
                    unfocusedLabelColor = Color(0xFFAAAAAA),
                    focusedLabelColor = Color(0xFF769656)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Retrieve button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (username.isNotBlank()) {
                        viewModel.fetchGames(ChessServer.CHESS_COM, username)
                    }
                },
                enabled = !uiState.isLoading && username.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF769656)
                )
            ) {
                Text(
                    text = "Retrieve games",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top rankings button
            OutlinedButton(
                onClick = onTopRankingsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF769656)
                )
            ) {
                Text(
                    text = "Select from top rankings",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF769656))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching games from Chess.com...",
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top rankings screen for a given chess server.
 */
@Composable
private fun TopRankingsScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    server: ChessServer,
    onBack: () -> Unit,
    onPlayerClick: (LeaderboardPlayer) -> Unit
) {
    val serverName = if (server == ChessServer.LICHESS) "lichess.org" else "chess.com"
    val serverColor = if (server == ChessServer.LICHESS) Color(0xFF629924) else Color(0xFF769656)

    // Handle back navigation
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("< Back", color = Color.White)
            }
            Text(
                text = "Top Rankings",
                style = MaterialTheme.typography.titleLarge,
                color = serverColor,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = serverName,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFAAAAAA),
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
        )

        // Content
        when {
            uiState.topRankingsLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = serverColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading top rankings...",
                            color = Color(0xFFAAAAAA)
                        )
                    }
                }
            }
            uiState.topRankingsError != null -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.topRankingsError ?: "",
                        color = Color.White,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Show each format
                    uiState.topRankings.forEach { (format, players) ->
                        item {
                            FormatSection(
                                format = format,
                                players = players,
                                serverColor = serverColor,
                                onPlayerClick = onPlayerClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section for a single format (Bullet, Blitz, etc.) showing top 10 players.
 */
@Composable
private fun FormatSection(
    format: String,
    players: List<LeaderboardPlayer>,
    serverColor: Color,
    onPlayerClick: (LeaderboardPlayer) -> Unit
) {
    Column {
        // Format header
        Text(
            text = format,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = serverColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Players table
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2A2A)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "#",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.width(30.dp)
                    )
                    Text(
                        text = "Player",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Rating",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )
                }

                // Player rows
                players.forEachIndexed { index, player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayerClick(player) }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 14.sp,
                            color = Color(0xFFAAAAAA),
                            modifier = Modifier.width(30.dp)
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            player.title?.let { title ->
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE6A800)
                                )
                            }
                            Text(
                                text = player.username,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = player.rating?.toString() ?: "-",
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
