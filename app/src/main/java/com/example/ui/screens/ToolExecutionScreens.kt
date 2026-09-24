package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfTool
import com.example.data.repository.CompressionLevel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File
import java.util.Locale

data class SelectedFileItem(
    val name: String,
    val sizeBytes: Long,
    val file: File? = null,
    val uri: Uri? = null
)

@Composable
fun ToolDetailScreen(
    tool: PdfTool,
    onBack: () -> Unit,
    onStartProcessing: (tool: PdfTool, files: List<SelectedFileItem>, options: Map<String, Any>) -> Unit,
    onOpenSpecialWorkflow: (toolId: String, files: List<SelectedFileItem>) -> Unit,
    onGenerateSampleFile: () -> File
) {
    val selectedFiles = remember { mutableStateListOf<SelectedFileItem>() }

    // Tool specific option states
    var compressionLevel by remember { mutableStateOf(CompressionLevel.RECOMMENDED) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordConfirmInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var watermarkOpacity by remember { mutableFloatStateOf(0.35f) }
    var targetLanguage by remember { mutableStateOf("Spanish") }
    var summaryStyle by remember { mutableStateOf("Executive Summary") }
    var rotateDegrees by remember { mutableFloatStateOf(90f) }
    var showAiPrivacyWarning by remember { mutableStateOf(false) }

    // Android file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Document.pdf"
                selectedFiles.add(SelectedFileItem(name = name, sizeBytes = 1024 * 180, uri = uri))
            }
        }
    }

    // Direct Camera / Scan launcher if Scan to PDF
    if (tool.id == "scan_to_pdf") {
        LaunchedEffect(Unit) {
            if (selectedFiles.isEmpty()) {
                onOpenSpecialWorkflow(tool.id, selectedFiles)
            }
        }
    }

    // AI Privacy Consent Dialog
    if (showAiPrivacyWarning) {
        AlertDialog(
            onDismissRequest = { showAiPrivacyWarning = false },
            icon = { Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("AI Processing Notice") },
            text = {
                Text(
                    "Your document will be sent for AI processing via Gemini API. We do not store or use your documents to train models. Continue?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAiPrivacyWarning = false
                        val opts = mapOf(
                            "summaryStyle" to summaryStyle,
                            "targetLanguage" to targetLanguage
                        )
                        onStartProcessing(tool, selectedFiles.toList(), opts)
                    },
                    modifier = Modifier.testTag("ai_consent_continue")
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiPrivacyWarning = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            PdfNovaTopBar(
                title = tool.name,
                onBack = onBack,
                actions = {
                    PdfNovaPrivacyBadge(isCloudAi = tool.isAi)
                }
            )
        },
        bottomBar = {
            if (selectedFiles.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${selectedFiles.size} file(s) selected",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Ready to process",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaSuccess
                            )
                        }

                        PdfNovaButton(
                            text = when (tool.id) {
                                "sign_pdf" -> "Open Sign Studio"
                                "watermark" -> "Apply Watermark"
                                "protect_pdf" -> "Protect PDF"
                                "ai_summarizer" -> "Generate Summary"
                                "split_pdf", "remove_pages", "extract_pages", "organize_pdf", "crop_pdf" -> "Open Page Organizer"
                                else -> "Process PDF"
                            },
                            onClick = {
                                if (tool.id in listOf("split_pdf", "remove_pages", "extract_pages", "organize_pdf", "crop_pdf", "sign_pdf", "scan_to_pdf")) {
                                    onOpenSpecialWorkflow(tool.id, selectedFiles)
                                } else if (tool.isAi) {
                                    showAiPrivacyWarning = true
                                } else {
                                    val opts = mutableMapOf<String, Any>()
                                    opts["compression"] = compressionLevel
                                    opts["password"] = passwordInput
                                    opts["watermarkText"] = watermarkText
                                    opts["watermarkOpacity"] = watermarkOpacity
                                    opts["rotateDegrees"] = rotateDegrees
                                    onStartProcessing(tool, selectedFiles.toList(), opts)
                                }
                            },
                            testTag = "execute_tool_action_btn"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("tool_detail_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(tool.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(tool.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // File Upload / Selection Area
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .testTag("upload_dropzone"),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (tool.id == "scan_to_pdf") Icons.Default.CameraAlt else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (tool.requiresMultiFile) "Select multiple PDF files" else "Select PDF document",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Supports PDF, images, scanned pages & office docs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PdfNovaButton(
                                text = "Choose Files",
                                onClick = {
                                    filePickerLauncher.launch(tool.acceptMimeTypes.toTypedArray())
                                },
                                icon = Icons.Default.Folder,
                                testTag = "choose_files_btn"
                            )

                            // Instant Sample PDF helper button so emulator / tester can immediately test with 1 tap!
                            PdfNovaOutlinedButton(
                                text = "Use Sample PDF",
                                onClick = {
                                    val sample = onGenerateSampleFile()
                                    selectedFiles.add(
                                        SelectedFileItem(
                                            name = sample.name,
                                            sizeBytes = sample.length(),
                                            file = sample
                                        )
                                    )
                                },
                                icon = Icons.Default.Description,
                                testTag = "use_sample_pdf_btn"
                            )
                        }
                    }
                }
            }

            // Selected Files List
            if (selectedFiles.isNotEmpty()) {
                item {
                    PdfNovaSectionHeader(
                        title = "Selected Files (${selectedFiles.size})",
                        actionText = if (tool.requiresMultiFile || selectedFiles.size < 5) "+ Add More" else null,
                        onActionClick = { filePickerLauncher.launch(tool.acceptMimeTypes.toTypedArray()) }
                    )
                }

                items(selectedFiles) { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NovaErrorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = NovaError, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                val kb = item.sizeBytes / 1024.0
                                Text("${String.format(Locale.US, "%.0f", kb)} KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = { selectedFiles.remove(item) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("remove_selected_file_${item.name}")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Tool Specific Configuration Section
            item {
                when (tool.id) {
                    "compress_pdf" -> {
                        SettingsCard(title = "Compression Level") {
                            CompressionLevel.values().forEach { level ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { compressionLevel = level }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = compressionLevel == level, onClick = { compressionLevel = level })
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(level.label, fontWeight = FontWeight.SemiBold)
                                        Text(level.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    "protect_pdf" -> {
                        SettingsCard(title = "Set Document Password") {
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("protect_password_input")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = passwordConfirmInput,
                                onValueChange = { passwordConfirmInput = it },
                                label = { Text("Confirm Password") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("protect_password_confirm_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = NovaSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Secured with local 256-bit AES encryption standard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    "unlock_pdf" -> {
                        SettingsCard(title = "Enter Document Password") {
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("PDF Password") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unlock_password_input")
                            )
                        }
                    }

                    "add_watermark" -> {
                        SettingsCard(title = "Watermark Customization") {
                            OutlinedTextField(
                                value = watermarkText,
                                onValueChange = { watermarkText = it },
                                label = { Text("Watermark Text") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("watermark_text_input")
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Opacity: ${(watermarkOpacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = watermarkOpacity,
                                onValueChange = { watermarkOpacity = it },
                                valueRange = 0.1f..0.9f,
                                modifier = Modifier.testTag("watermark_opacity_slider")
                            )
                        }
                    }

                    "rotate_pdf" -> {
                        SettingsCard(title = "Rotation Angle") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(90f, 180f, 270f).forEach { deg ->
                                    FilterChip(
                                        selected = rotateDegrees == deg,
                                        onClick = { rotateDegrees = deg },
                                        label = { Text("${deg.toInt()}° Clockwise") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    "translate_pdf" -> {
                        SettingsCard(title = "Target Language") {
                            val languages = listOf("Spanish", "Hindi", "French", "German", "Japanese", "Arabic")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                languages.take(3).forEach { lang ->
                                    FilterChip(
                                        selected = targetLanguage == lang,
                                        onClick = { targetLanguage = lang },
                                        label = { Text(lang) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    "ai_summarizer" -> {
                        SettingsCard(title = "Summary Style") {
                            listOf("Executive Summary", "Key Bullet Points", "Detailed Synthesis").forEach { style ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { summaryStyle = style }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = summaryStyle == style, onClick = { summaryStyle = style })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(style, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun ProcessingScreen(
    toolName: String,
    onCancel: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0.1f) }
    var statusText by remember { mutableStateOf("Preparing pages...") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    LaunchedEffect(Unit) {
        progress = 0.25f
        statusText = "Preparing pages..."
        kotlinx.coroutines.delay(600)
        progress = 0.65f
        statusText = "Processing document with PDF engine..."
        kotlinx.coroutines.delay(700)
        progress = 0.95f
        statusText = "Finalizing output and applying verification..."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("processing_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(rotation)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Processing your PDF...",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(36.dp))

            PdfNovaOutlinedButton(
                text = "Cancel",
                onClick = onCancel,
                testTag = "cancel_processing_btn"
            )
        }
    }
}

@Composable
fun SuccessScreen(
    outputFile: File,
    toolName: String,
    onOpen: (File) -> Unit,
    onShare: (File) -> Unit,
    onSaveAs: (File) -> Unit,
    onDone: () -> Unit,
    onProcessAnother: () -> Unit
) {
    val kb = outputFile.length() / 1024.0
    val sizeStr = if (kb > 1024) String.format(Locale.US, "%.1f MB", kb / 1024.0) else String.format(Locale.US, "%.0f KB", kb)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("success_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(NovaSuccessContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NovaSuccess,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Done!",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your document has been processed successfully.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // File summary card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NovaErrorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = NovaError, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(outputFile.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Size: $sizeStr • Tool: $toolName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PdfNovaButton(
                    text = "Open",
                    onClick = { onOpen(outputFile) },
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f),
                    testTag = "success_open_btn"
                )
                PdfNovaOutlinedButton(
                    text = "Share",
                    onClick = { onShare(outputFile) },
                    icon = Icons.Default.Share,
                    modifier = Modifier.weight(1f),
                    testTag = "success_share_btn"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            PdfNovaOutlinedButton(
                text = "Save As...",
                onClick = { onSaveAs(outputFile) },
                icon = Icons.Default.Download,
                modifier = Modifier.fillMaxWidth(),
                testTag = "success_save_as_btn"
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onProcessAnother,
                modifier = Modifier.testTag("process_another_file_btn")
            ) {
                Text("Process another file", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }

            TextButton(
                onClick = onDone,
                modifier = Modifier.testTag("success_done_btn")
            ) {
                Text("Return to Home", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
