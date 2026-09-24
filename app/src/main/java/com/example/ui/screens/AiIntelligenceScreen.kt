package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.DocumentSummary
import com.example.data.ai.GeminiPdfService
import com.example.ui.components.PdfNovaButton
import com.example.ui.components.PdfNovaOutlinedButton
import com.example.ui.components.PdfNovaTopBar
import com.example.ui.theme.NovaPrimaryLight
import kotlinx.coroutines.launch

@Composable
fun AiIntelligenceScreen(
    documentName: String,
    summary: DocumentSummary,
    geminiService: GeminiPdfService,
    onBack: () -> Unit,
    onExportPdf: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var questionInput by remember { mutableStateOf("") }
    val qnaHistory = remember { mutableStateListOf<Pair<String, String>>() }
    var isAnsweringQuestion by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PdfNovaTopBar(
                title = "AI Intelligence",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            val fullText = buildString {
                                appendLine("Summary of $documentName:")
                                appendLine()
                                appendLine("OVERVIEW:")
                                appendLine(summary.overview)
                                appendLine()
                                appendLine("KEY POINTS:")
                                summary.keyPoints.forEach { appendLine("• $it") }
                                appendLine()
                                appendLine("IMPORTANT DETAILS:")
                                summary.importantDetails.forEach { appendLine("• $it") }
                            }
                            clipboardManager.setText(AnnotatedString(fullText))
                            Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("copy_ai_summary_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Summary")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("ai_intelligence_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero AI Header
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF0EA5E9))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF0EA5E9)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("PDF Intelligence Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(documentName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Estimated read: ${summary.estimatedReadTimeMinutes} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Overview Card
            item {
                AiSectionCard(title = "Executive Overview", icon = Icons.Default.Summarize) {
                    Text(
                        text = summary.overview,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Key Points Card
            item {
                AiSectionCard(title = "Key Points", icon = Icons.Default.CheckCircleOutline) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        summary.keyPoints.forEach { point ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NovaPrimaryLight)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Important Details Card
            item {
                AiSectionCard(title = "Important Details", icon = Icons.Default.Info) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        summary.importantDetails.forEach { detail ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = detail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Ask AI Q&A interactive section
            item {
                AiSectionCard(title = "Ask Questions about this Document", icon = Icons.Default.QuestionAnswer) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = questionInput,
                                onValueChange = { questionInput = it },
                                placeholder = { Text("Ask anything about this document...") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ask_ai_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val q = questionInput.trim()
                                    if (q.isNotEmpty()) {
                                        questionInput = ""
                                        isAnsweringQuestion = true
                                        coroutineScope.launch {
                                            val answer = geminiService.askDocumentQuestion(documentName, q)
                                            qnaHistory.add(q to answer)
                                            isAnsweringQuestion = false
                                        }
                                    }
                                },
                                enabled = !isAnsweringQuestion && questionInput.isNotBlank(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("submit_ai_question_btn")
                            ) {
                                if (isAnsweringQuestion) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        if (qnaHistory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            qnaHistory.forEach { (q, a) ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Q: $q", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("A: $a", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Export Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PdfNovaOutlinedButton(
                        text = "Export Summary PDF",
                        onClick = {
                            val content = "Executive Summary\n\n${summary.overview}\n\nKey Points:\n" + summary.keyPoints.joinToString("\n• ", prefix = "• ")
                            onExportPdf(content)
                        },
                        icon = Icons.Default.Download,
                        modifier = Modifier.weight(1f),
                        testTag = "export_ai_pdf_btn"
                    )
                }
            }
        }
    }
}

@Composable
private fun AiSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
