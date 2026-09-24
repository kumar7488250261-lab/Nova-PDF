package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.RecentDocument
import com.example.ui.components.*

@Composable
fun RecentFilesScreen(
    recentDocuments: List<RecentDocument>,
    onOpenDocument: (RecentDocument) -> Unit,
    onShareDocument: (RecentDocument) -> Unit,
    onDeleteDocument: (Long) -> Unit,
    onClearAll: () -> Unit,
    onChoosePdf: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    val filtered = remember(recentDocuments, searchQuery) {
        if (searchQuery.isBlank()) recentDocuments
        else recentDocuments.filter {
            it.fileName.contains(searchQuery, ignoreCase = true) ||
            it.toolUsed.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("Clear Recent Files?") },
            text = { Text("This will remove all recent document shortcuts from PDFNova history. Your original files will not be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showConfirmClearDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_recents")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            PdfNovaTopBar(
                title = "Recent Files",
                actions = {
                    if (recentDocuments.isNotEmpty()) {
                        IconButton(
                            onClick = { showConfirmClearDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("clear_all_recent_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear all recent",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("recent_files_screen")
        ) {
            if (recentDocuments.isNotEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    PdfNovaSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search recent documents...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (filtered.isEmpty()) {
                PdfNovaEmptyState(
                    title = if (recentDocuments.isEmpty()) "No recent files" else "No matching documents",
                    message = if (recentDocuments.isEmpty()) "Documents you process or view with PDFNova will appear here for instant access." else "No files matched '$searchQuery'.",
                    buttonText = "Choose a PDF",
                    onButtonClick = onChoosePdf,
                    icon = Icons.Default.FolderOpen,
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filtered.size} documents",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PdfNovaPrivacyBadge()
                        }
                    }
                    items(filtered, key = { it.id }) { doc ->
                        PdfNovaFileCard(
                            document = doc,
                            onOpen = { onOpenDocument(doc) },
                            onShare = { onShareDocument(doc) },
                            onDelete = { onDeleteDocument(doc.id) }
                        )
                    }
                }
            }
        }
    }
}
