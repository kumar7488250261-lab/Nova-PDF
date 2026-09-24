package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppPreferences
import com.example.data.repository.AppThemeMode
import com.example.data.repository.CompressionLevel
import com.example.ui.components.*

@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    onSetTheme: (AppThemeMode) -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetCompression: (CompressionLevel) -> Unit,
    onToggleLocalOnly: (Boolean) -> Unit,
    onClearHistory: () -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCompressionDialog by remember { mutableStateOf(false) }
    var showInfoDialogTitle by remember { mutableStateOf<String?>(null) }
    var showInfoDialogMessage by remember { mutableStateOf<String?>(null) }
    var showClearHistorySuccess by remember { mutableStateOf(false) }

    // Dialogs
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    AppThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable {
                                    onSetTheme(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.themeMode == mode,
                                onClick = {
                                    onSetTheme(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (mode) {
                                    AppThemeMode.SYSTEM -> "System Default"
                                    AppThemeMode.LIGHT -> "Light Theme"
                                    AppThemeMode.DARK -> "Dark Theme (Premium Charcoal)"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Choose Language") },
            text = {
                Column {
                    listOf("English", "Hindi").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable {
                                    onSetLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.language == lang,
                                onClick = {
                                    onSetLanguage(lang)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (lang == "Hindi") "हिंदी (Hindi)" else "English (US/UK)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showCompressionDialog) {
        AlertDialog(
            onDismissRequest = { showCompressionDialog = false },
            title = { Text("Default Compression") },
            text = {
                Column {
                    CompressionLevel.values().forEach { comp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable {
                                    onSetCompression(comp)
                                    showCompressionDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.defaultCompression == comp,
                                onClick = {
                                    onSetCompression(comp)
                                    showCompressionDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(comp.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(comp.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showInfoDialogTitle != null && showInfoDialogMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showInfoDialogTitle = null
                showInfoDialogMessage = null
            },
            title = { Text(showInfoDialogTitle!!) },
            text = { Text(showInfoDialogMessage!!) },
            confirmButton = {
                TextButton(onClick = {
                    showInfoDialogTitle = null
                    showInfoDialogMessage = null
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            PdfNovaTopBar(title = "Settings")
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = "Appearance") {
                    SettingsRow(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = when (preferences.themeMode) {
                            AppThemeMode.SYSTEM -> "System default"
                            AppThemeMode.LIGHT -> "Light theme"
                            AppThemeMode.DARK -> "Dark theme"
                        },
                        onClick = { showThemeDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SettingsRow(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = preferences.language,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }

            // Processing Section
            item {
                SettingsSection(title = "Processing") {
                    SettingsRow(
                        icon = Icons.Default.Folder,
                        title = "Default Output Location",
                        subtitle = "App Internal Sandbox • Downloads/PDFNova",
                        onClick = {
                            showInfoDialogTitle = "Storage Location"
                            showInfoDialogMessage = "PDFNova stores processed documents securely in application private storage and exports directly to Android Downloads or your chosen folder via system file picker."
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SettingsRow(
                        icon = Icons.Default.Compress,
                        title = "Default Compression",
                        subtitle = preferences.defaultCompression.label,
                        onClick = { showCompressionDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SettingsRow(
                        icon = Icons.Default.HighQuality,
                        title = "Default Image Quality",
                        subtitle = "150 DPI (Balanced web & print quality)",
                        onClick = {
                            showInfoDialogTitle = "Image Quality (DPI)"
                            showInfoDialogMessage = "Standardized to 150 DPI for optimal rendering clarity while avoiding oversized file attachments."
                        }
                    )
                }
            }

            // Privacy Section
            item {
                SettingsSection(title = "Privacy & Security") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Strict Local Processing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Process files strictly on this device whenever possible without transmitting file bytes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.localProcessingOnly,
                            onCheckedChange = onToggleLocalOnly,
                            modifier = Modifier.testTag("toggle_local_processing")
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SettingsRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "AI Privacy Consent",
                        subtitle = "Requires explicit user confirmation before sending document snippets",
                        onClick = {
                            showInfoDialogTitle = "AI Privacy Notice"
                            showInfoDialogMessage = "PDF Intelligence features (Summarizer, Translate, PDF to Markdown) will always prompt for your explicit consent prior to invoking Gemini API. Your documents are never retained for model training."
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SettingsRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Clear Recent History",
                        subtitle = "Remove all document references from the app",
                        onClick = {
                            onClearHistory()
                            showInfoDialogTitle = "History Cleared"
                            showInfoDialogMessage = "Your recent documents history has been cleared successfully."
                        }
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = "About PDFNova") {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "1.0.0 (Production Release)",
                        onClick = {}
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SettingsRow(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        subtitle = "Read our comprehensive privacy guarantee",
                        onClick = {
                            showInfoDialogTitle = "PDFNova Privacy Policy"
                            showInfoDialogMessage = "PDFNova is engineered with a strict privacy-first foundation. All core 33 utilities process documents locally using native Android graphics and PDF drivers. No file data is sold, stored externally, or tracked."
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SettingsRow(
                        icon = Icons.Default.Gavel,
                        title = "Terms of Service",
                        subtitle = "License and user terms",
                        onClick = {
                            showInfoDialogTitle = "Terms of Service"
                            showInfoDialogMessage = "PDFNova is provided for professional document manipulation. You retain full ownership and intellectual rights to all documents processed."
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SettingsRow(
                        icon = Icons.Default.Code,
                        title = "Open-Source Licenses",
                        subtitle = "Android Jetpack, Compose, Room, Coil, Coroutines",
                        onClick = {
                            showInfoDialogTitle = "Open Source Licenses"
                            showInfoDialogMessage = "Built with modern Android open-source technologies under Apache 2.0 and MIT licenses:\n• AndroidX Jetpack & Jetpack Compose\n• Kotlin Coroutines & Flow\n• Room Database & KSP\n• Material Design 3 Components"
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "Contact Support",
                        subtitle = "support@pdfnova.app",
                        onClick = {
                            showInfoDialogTitle = "Customer Support"
                            showInfoDialogMessage = "Need help or want to suggest a new tool? Email our product team at support@pdfnova.app."
                        }
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PDFNova • One App. Every PDF Tool.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Designed for Google Play Store",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp)
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
