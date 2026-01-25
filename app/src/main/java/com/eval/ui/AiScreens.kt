package com.eval.ui

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Type of AI report being generated.
 */
enum class AiReportScreenType { GAME, PLAYER }

/**
 * Navigation wrapper for AI Reports screen.
 */
@Composable
fun AiReportsScreenNav(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Determine report type: player > game
    val reportType = when {
        uiState.playerAiReportsPlayerName.isNotEmpty() -> AiReportScreenType.PLAYER
        else -> AiReportScreenType.GAME
    }

    AiReportsScreen(
        uiState = uiState,
        savedAgentIds = viewModel.loadAiReportAgents(),
        reportType = reportType,
        onGenerate = { selectedAgentIds ->
            viewModel.saveAiReportAgents(selectedAgentIds)
            when (reportType) {
                AiReportScreenType.PLAYER -> viewModel.startPlayerAiReportsWithAgents(selectedAgentIds)
                AiReportScreenType.GAME -> viewModel.generateAiReportsWithAgents(selectedAgentIds)
            }
        },
        onShare = {
            when (reportType) {
                AiReportScreenType.PLAYER -> sharePlayerAiReports(context, uiState)
                AiReportScreenType.GAME -> shareAiReports(context, uiState)
            }
        },
        onOpenInBrowser = {
            when (reportType) {
                AiReportScreenType.PLAYER -> openPlayerAiReportsInChrome(context, uiState)
                AiReportScreenType.GAME -> openAiReportsInChrome(context, uiState)
            }
        },
        onDismiss = {
            when (reportType) {
                AiReportScreenType.PLAYER -> viewModel.dismissPlayerAiReportsDialog()
                AiReportScreenType.GAME -> viewModel.dismissAiReportsDialog()
            }
            onNavigateBack()
        }
    )
}

/**
 * Full-screen AI Reports generation and results screen.
 * Shows agent selection first, then progress and results.
 * Supports game reports and player reports.
 */
@Composable
fun AiReportsScreen(
    uiState: GameUiState,
    savedAgentIds: Set<String>,
    reportType: AiReportScreenType = AiReportScreenType.GAME,
    onGenerate: (Set<String>) -> Unit,
    onShare: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onDismiss: () -> Unit
) {
    // Use appropriate state based on report type
    val reportsTotal = when (reportType) {
        AiReportScreenType.PLAYER -> uiState.playerAiReportsTotal
        AiReportScreenType.GAME -> uiState.aiReportsTotal
    }
    val reportsProgress = when (reportType) {
        AiReportScreenType.PLAYER -> uiState.playerAiReportsProgress
        AiReportScreenType.GAME -> uiState.aiReportsProgress
    }
    val reportsAgentResults = when (reportType) {
        AiReportScreenType.PLAYER -> uiState.playerAiReportsAgentResults
        AiReportScreenType.GAME -> uiState.aiReportsAgentResults
    }
    val reportsSelectedAgents = when (reportType) {
        AiReportScreenType.PLAYER -> uiState.playerAiReportsSelectedAgents
        AiReportScreenType.GAME -> uiState.aiReportsSelectedAgents
    }

    val isGenerating = reportsTotal > 0
    val isComplete = reportsProgress >= reportsTotal && reportsTotal > 0

    // Viewer state
    var showViewer by remember { mutableStateOf(false) }
    var selectedAgentForViewer by remember { mutableStateOf<String?>(null) }

    // Show viewer screen when activated
    if (showViewer) {
        AiReportsViewerScreen(
            agentResults = reportsAgentResults,
            aiSettings = uiState.aiSettings,
            initialSelectedAgentId = selectedAgentForViewer,
            onDismiss = { showViewer = false }
        )
        return
    }

    // Agent selection state
    val configuredAgents = uiState.aiSettings.getConfiguredAgents()
    var selectedAgentIds by remember {
        mutableStateOf(
            if (savedAgentIds.isNotEmpty()) {
                savedAgentIds.filter { id -> configuredAgents.any { it.id == id } }.toSet()
            } else {
                configuredAgents.map { it.id }.toSet()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        EvalTitleBar(
            title = when {
                isComplete -> when (reportType) {
                    AiReportScreenType.PLAYER -> "Player Reports Ready"
                    AiReportScreenType.GAME -> "AI Reports Ready"
                }
                isGenerating -> when (reportType) {
                    AiReportScreenType.PLAYER -> "Generating Player Reports"
                    AiReportScreenType.GAME -> "Generating AI Reports"
                }
                else -> when (reportType) {
                    AiReportScreenType.PLAYER -> "Player: ${uiState.playerAiReportsPlayerName}"
                    AiReportScreenType.GAME -> "AI Reports"
                }
            },
            onBackClick = onDismiss,
            onEvalClick = onDismiss
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isGenerating) {
            // Select all / Select none buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { selectedAgentIds = configuredAgents.map { it.id }.toSet() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Select all")
                }
                OutlinedButton(
                    onClick = { selectedAgentIds = emptySet() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Select none")
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (configuredAgents.isEmpty()) {
                        Text(
                            text = "No AI agents configured. Please configure agents in Settings > AI Setup.",
                            color = Color(0xFFAAAAAA)
                        )
                    } else {
                        configuredAgents.sortedBy { it.name.lowercase() }.forEach { agent ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAgentIds = if (agent.id in selectedAgentIds) {
                                            selectedAgentIds - agent.id
                                        } else {
                                            selectedAgentIds + agent.id
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = agent.id in selectedAgentIds,
                                    onCheckedChange = { checked ->
                                        selectedAgentIds = if (checked) {
                                            selectedAgentIds + agent.id
                                        } else {
                                            selectedAgentIds - agent.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = agent.name,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${agent.provider.displayName} - ${agent.model}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFAAAAAA)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generate button
            Button(
                onClick = { onGenerate(selectedAgentIds) },
                enabled = selectedAgentIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6)
                )
            ) {
                Text("Generate Reports")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        } else {
            // Progress/Results UI
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Show all selected agents with their status
                    reportsSelectedAgents.mapNotNull { agentId ->
                        uiState.aiSettings.getAgentById(agentId)
                    }.sortedBy { it.name.lowercase() }.forEach { agent ->
                        val result = reportsAgentResults[agent.id]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = agent.name,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            when {
                                result == null -> {
                                    // Still pending - show small spinner
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.Gray
                                    )
                                }
                                result.isSuccess -> {
                                    Text(
                                        text = "✓",
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "✗",
                                        color = Color(0xFFF44336),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons at the bottom
            if (isComplete) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showViewer = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("View")
                    }
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Share")
                    }
                    Button(
                        onClick = onOpenInBrowser,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        )
                    ) {
                        Text("Browser")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Close/Cancel button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isComplete) "Close" else "Cancel")
            }
        }
    }
}

/**
 * Full-screen viewer for AI agent responses.
 * Shows buttons for each agent at the top, displays HTML-converted markdown content.
 */
@Composable
fun AiReportsViewerScreen(
    agentResults: Map<String, com.eval.data.AiAnalysisResponse>,
    aiSettings: AiSettings,
    initialSelectedAgentId: String? = null,
    onDismiss: () -> Unit
) {
    // Get agents with successful results
    val agentsWithResults = agentResults.entries
        .filter { it.value.isSuccess }
        .mapNotNull { (agentId, result) ->
            val agent = aiSettings.getAgentById(agentId)
            if (agent != null) Triple(agentId, agent, result) else null
        }
        .sortedBy { it.second.name.lowercase() }

    // Selected agent state
    var selectedAgentId by remember {
        mutableStateOf(initialSelectedAgentId ?: agentsWithResults.firstOrNull()?.first)
    }

    // Get the selected agent's result
    val selectedResult = selectedAgentId?.let { agentResults[it] }
    val selectedAgent = selectedAgentId?.let { aiSettings.getAgentById(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header - show provider and model of selected agent
        val titleText = if (selectedAgent != null) {
            "${selectedAgent.provider.displayName} - ${selectedAgent.model}"
        } else {
            "View Reports"
        }
        EvalTitleBar(
            title = titleText,
            onBackClick = onDismiss,
            onEvalClick = onDismiss
        )

        // Agent selection buttons - wrapping flow layout
        if (agentsWithResults.isNotEmpty()) {
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                agentsWithResults.forEach { (agentId, agent, _) ->
                    val isSelected = agentId == selectedAgentId
                    Button(
                        onClick = { selectedAgentId = agentId },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF3A3A4A)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = agent.name,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Content area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            if (agentsWithResults.isEmpty()) {
                // No results
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No successful reports to display",
                        color = Color(0xFFAAAAAA),
                        fontSize = 16.sp
                    )
                }
            } else if (selectedResult?.analysis != null) {
                // Show the HTML content in a WebView-like display
                val htmlContent = remember(selectedResult.analysis) {
                    convertMarkdownToSimpleHtml(selectedResult.analysis)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // HTML content rendered as styled text
                    HtmlContentDisplay(htmlContent = htmlContent)

                    // Citations section (if available)
                    selectedResult.citations?.takeIf { it.isNotEmpty() }?.let { citations ->
                        Spacer(modifier = Modifier.height(16.dp))
                        CitationsSection(citations = citations)
                    }

                    // Search results section (if available)
                    selectedResult.searchResults?.takeIf { it.isNotEmpty() }?.let { searchResults ->
                        Spacer(modifier = Modifier.height(16.dp))
                        SearchResultsSection(searchResults = searchResults)
                    }
                }
            } else {
                // No analysis for selected agent
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No analysis available",
                        color = Color(0xFFAAAAAA),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Converts markdown text to simple HTML, removing multiple blank lines.
 */
private fun convertMarkdownToSimpleHtml(markdown: String): String {
    // First normalize line endings and remove multiple blank lines
    var html = markdown
        .replace("\r\n", "\n")
        .replace(Regex("\n{3,}"), "\n\n")  // Replace 3+ newlines with 2

    // Basic markdown to HTML conversion
    html = html
        // Escape HTML entities first
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        // Headers
        .replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<h3>$1</h3>")
        .replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<h2>$1</h2>")
        .replace(Regex("^# (.+)$", RegexOption.MULTILINE), "<h1>$1</h1>")
        // Bold
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
        // Italic
        .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
        // Bullet points
        .replace(Regex("^- (.+)$", RegexOption.MULTILINE), "<li>$1</li>")
        .replace(Regex("^\\* (.+)$", RegexOption.MULTILINE), "<li>$1</li>")
        // Numbered lists
        .replace(Regex("^\\d+\\. (.+)$", RegexOption.MULTILINE), "<li>$1</li>")
        // Line breaks - convert double newlines to paragraph breaks
        .replace("\n\n", "</p><p>")
        .replace("\n", "<br>")

    // Wrap consecutive <li> items in <ul>
    html = html.replace(Regex("(<li>.*?</li>)+")) { match ->
        "<ul>${match.value}</ul>"
    }

    // Wrap in paragraph if not empty
    if (html.isNotBlank()) {
        html = "<p>$html</p>"
    }

    return html
}

/**
 * Displays HTML content as styled Compose text.
 */
@Composable
private fun HtmlContentDisplay(htmlContent: String) {
    // Parse and display HTML content
    val annotatedString = remember(htmlContent) {
        parseHtmlToAnnotatedString(htmlContent)
    }

    Text(
        text = annotatedString,
        color = Color.White,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Parses HTML to AnnotatedString for display.
 */
private fun parseHtmlToAnnotatedString(html: String): androidx.compose.ui.text.AnnotatedString {
    // First clean up HTML entities and structural tags
    val cleanHtml = html
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("<p>", "")
        .replace("</p>", "\n\n")
        .replace("<br>", "\n")
        .replace("<ul>", "\n")
        .replace("</ul>", "\n")
        .replace("<li>", "  \u2022 ")
        .replace("</li>", "\n")
        .replace(Regex("\n{3,}"), "\n\n")  // Remove excess blank lines
        .trim()

    return androidx.compose.ui.text.buildAnnotatedString {
        // Process tags
        val tagPattern = Regex("<(/?)(h[123]|strong|em)>")
        var lastEnd = 0

        val matches = tagPattern.findAll(cleanHtml).toList()
        val styleStack = mutableListOf<Pair<String, Int>>()

        for (match in matches) {
            // Add text before this tag
            if (match.range.first > lastEnd) {
                append(cleanHtml.substring(lastEnd, match.range.first))
            }

            val isClosing = match.groupValues[1] == "/"
            val tagName = match.groupValues[2]

            if (!isClosing) {
                styleStack.add(tagName to length)
            } else {
                // Find matching opening tag
                val openTagIndex = styleStack.indexOfLast { it.first == tagName }
                if (openTagIndex >= 0) {
                    val (_, startPos) = styleStack.removeAt(openTagIndex)
                    val style = when (tagName) {
                        "h1" -> androidx.compose.ui.text.SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                        "h2" -> androidx.compose.ui.text.SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF8BB8FF)
                        )
                        "h3" -> androidx.compose.ui.text.SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF9FCFFF)
                        )
                        "strong" -> androidx.compose.ui.text.SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                        "em" -> androidx.compose.ui.text.SpanStyle(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color(0xFFCCCCCC)
                        )
                        else -> null
                    }
                    if (style != null) {
                        addStyle(style, startPos, length)
                    }
                }
            }

            lastEnd = match.range.last + 1
        }

        // Add remaining text
        if (lastEnd < cleanHtml.length) {
            append(cleanHtml.substring(lastEnd))
        }
    }
}

/**
 * Displays a list of citations (URLs) returned by the AI service.
 */
@Composable
private fun CitationsSection(citations: List<String>) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252525), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Sources",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF8B5CF6),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        citations.forEachIndexed { index, url ->
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignore if URL can't be opened
                        }
                    }
            ) {
                Text(
                    text = "${index + 1}. ",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
                Text(
                    text = url,
                    color = Color(0xFF64B5F6),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
        }
    }
}

/**
 * Displays search results returned by the AI service.
 */
@Composable
private fun SearchResultsSection(searchResults: List<com.eval.data.SearchResult>) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252525), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Search Results",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFFFF9800),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        searchResults.forEachIndexed { index, result ->
            if (result.url != null) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(result.url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Ignore if URL can't be opened
                            }
                        }
                ) {
                    // Title/Name with number
                    Row {
                        Text(
                            text = "${index + 1}. ",
                            color = Color(0xFFAAAAAA),
                            fontSize = 14.sp
                        )
                        Text(
                            text = result.name ?: result.url,
                            color = Color(0xFF64B5F6),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    }
                    // URL if different from name
                    if (result.name != null && result.name != result.url) {
                        Text(
                            text = result.url,
                            color = Color(0xFF888888),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                        )
                    }
                    // Snippet if available
                    if (!result.snippet.isNullOrBlank()) {
                        Text(
                            text = result.snippet,
                            color = Color(0xFFBBBBBB),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
