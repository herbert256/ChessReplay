package com.eval.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Navigation routes for the app.
 */
object NavRoutes {
    const val GAME = "game"
    const val SETTINGS = "settings"
    const val HELP = "help"
    const val RETRIEVE = "retrieve"
    const val PLAYER_INFO = "player_info"
    const val AI_REPORTS = "ai_reports"
}

/**
 * Main navigation host for the app.
 */
@Composable
fun EvalNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: GameViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.GAME,
        modifier = modifier
    ) {
        composable(NavRoutes.GAME) {
            GameScreenContent(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToHelp = { navController.navigate(NavRoutes.HELP) },
                onNavigateToRetrieve = { navController.navigate(NavRoutes.RETRIEVE) },
                onNavigateToPlayerInfo = { navController.navigate(NavRoutes.PLAYER_INFO) },
                onNavigateToAiReports = { navController.navigate(NavRoutes.AI_REPORTS) }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreenNav(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.HELP) {
            HelpScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.RETRIEVE) {
            RetrieveScreenNav(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGame = { navController.navigate(NavRoutes.GAME) }
            )
        }

        composable(NavRoutes.PLAYER_INFO) {
            PlayerInfoScreenNav(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.AI_REPORTS) {
            AiReportsScreenNav(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Wrapper for SettingsScreen that gets state from ViewModel.
 */
@Composable
fun SettingsScreenNav(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        stockfishSettings = uiState.stockfishSettings,
        boardLayoutSettings = uiState.boardLayoutSettings,
        graphSettings = uiState.graphSettings,
        interfaceVisibility = uiState.interfaceVisibility,
        generalSettings = uiState.generalSettings,
        aiSettings = uiState.aiSettings,
        availableChatGptModels = uiState.availableChatGptModels,
        isLoadingChatGptModels = uiState.isLoadingChatGptModels,
        availableGeminiModels = uiState.availableGeminiModels,
        isLoadingGeminiModels = uiState.isLoadingGeminiModels,
        availableGrokModels = uiState.availableGrokModels,
        isLoadingGrokModels = uiState.isLoadingGrokModels,
        availableGroqModels = uiState.availableGroqModels,
        isLoadingGroqModels = uiState.isLoadingGroqModels,
        availableDeepSeekModels = uiState.availableDeepSeekModels,
        isLoadingDeepSeekModels = uiState.isLoadingDeepSeekModels,
        availableMistralModels = uiState.availableMistralModels,
        isLoadingMistralModels = uiState.isLoadingMistralModels,
        availablePerplexityModels = uiState.availablePerplexityModels,
        isLoadingPerplexityModels = uiState.isLoadingPerplexityModels,
        availableTogetherModels = uiState.availableTogetherModels,
        isLoadingTogetherModels = uiState.isLoadingTogetherModels,
        availableOpenRouterModels = uiState.availableOpenRouterModels,
        isLoadingOpenRouterModels = uiState.isLoadingOpenRouterModels,
        onBack = onNavigateBack,
        onSaveStockfish = { viewModel.updateStockfishSettings(it) },
        onSaveBoardLayout = { viewModel.updateBoardLayoutSettings(it) },
        onSaveGraph = { viewModel.updateGraphSettings(it) },
        onSaveInterfaceVisibility = { viewModel.updateInterfaceVisibilitySettings(it) },
        onSaveGeneral = { viewModel.updateGeneralSettings(it) },
        onSaveAi = { viewModel.updateAiSettings(it) },
        onFetchChatGptModels = { viewModel.fetchChatGptModels(it) },
        onFetchGeminiModels = { viewModel.fetchGeminiModels(it) },
        onFetchGrokModels = { viewModel.fetchGrokModels(it) },
        onFetchGroqModels = { viewModel.fetchGroqModels(it) },
        onFetchDeepSeekModels = { viewModel.fetchDeepSeekModels(it) },
        onFetchMistralModels = { viewModel.fetchMistralModels(it) },
        onFetchPerplexityModels = { viewModel.fetchPerplexityModels(it) },
        onFetchTogetherModels = { viewModel.fetchTogetherModels(it) },
        onFetchOpenRouterModels = { viewModel.fetchOpenRouterModels(it) },
        onTestAiModel = { service, apiKey, model -> viewModel.testAiModel(service, apiKey, model) }
    )
}

/**
 * Wrapper for RetrieveScreen that gets state from ViewModel.
 */
@Composable
fun RetrieveScreenNav(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGame: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    RetrieveScreen(
        viewModel = viewModel,
        uiState = uiState,
        onBack = onNavigateBack,
        onNavigateToGame = onNavigateToGame
    )
}
