package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.RecentDocument
import com.example.data.model.PdfTool
import com.example.data.model.PdfToolsRegistry
import com.example.data.model.ToolCategory
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToTool: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToRecent: () -> Unit,
    onOpenDocument: (RecentDocument) -> Unit,
    onShareDocument: (RecentDocument) -> Unit,
    onDeleteDocument: (Long) -> Unit,
    onChoosePdf: () -> Unit,
    recentDocuments: List<RecentDocument>,
    favoriteToolIds: List<String>,
    onToggleFavorite: (String) -> Unit
) {
    val quickActions = remember { PdfToolsRegistry.quickActionTools }
    val favoriteTools = remember(favoriteToolIds) {
        if (favoriteToolIds.isEmpty()) {
            listOf(
                PdfToolsRegistry.getToolById("merge_pdf")!!,
                PdfToolsRegistry.getToolById("compress_pdf")!!,
                PdfToolsRegistry.getToolById("sign_pdf")!!,
                PdfToolsRegistry.getToolById("ai_summarizer")!!
            )
        } else {
            favoriteToolIds.mapNotNull { PdfToolsRegistry.getToolById(it) }
        }
    }

    Scaffold(
        topBar = {
            PdfNovaTopBar(
                title = "PDFNova",
                actions = {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("home_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search PDF Tools",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("home_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("home_scroll_list"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Hero Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF4338CA),
                                    Color(0xFF4F46E5),
                                    Color(0xFF6366F1)
                                )
                            )
                        )
                        .testTag("home_hero_card")
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "33 ALL-IN-ONE TOOLS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• Privacy First",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Everything you need for PDF",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Create, edit, convert, organize and protect your documents with ease.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onChoosePdf,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF4338CA)
                            ),
                            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("hero_choose_pdf_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Choose a PDF",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Search Field Trigger
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToSearch() }
                ) {
                    PdfNovaSearchBar(
                        query = "",
                        onQueryChange = {},
                        placeholder = "Search PDF tools (e.g. compress, sign, photo)...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. Quick Actions Section
            item {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    PaddingWrapper {
                        PdfNovaSectionHeader(
                            title = "Quick Actions",
                            subtitle = "Most frequently used utilities"
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(quickActions, key = { it.id }) { tool ->
                            QuickActionChip(
                                tool = tool,
                                onClick = { onNavigateToTool(tool.id) }
                            )
                        }
                    }
                }
            }

            // 4. Favorite Tools Section
            item {
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    PaddingWrapper {
                        PdfNovaSectionHeader(
                            title = "Favorite Tools",
                            subtitle = if (favoriteToolIds.isEmpty()) "Suggested essential tools (Tap star to customize)" else "Your pinned tools",
                            actionText = "View All 33",
                            onActionClick = onNavigateToTools
                        )
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        favoriteTools.forEach { tool ->
                            PdfNovaToolCard(
                                tool = tool,
                                onClick = { onNavigateToTool(tool.id) },
                                isFavorite = favoriteToolIds.contains(tool.id),
                                onToggleFavorite = { onToggleFavorite(tool.id) }
                            )
                        }
                    }
                }
            }

            // 5. Explore Categories Preview
            item {
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    PaddingWrapper {
                        PdfNovaSectionHeader(
                            title = "Categories",
                            subtitle = "7 specialized categories",
                            actionText = "See All",
                            onActionClick = onNavigateToTools
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(ToolCategory.values(), key = { it.name }) { category ->
                            CategoryPill(
                                category = category,
                                onClick = onNavigateToTools
                            )
                        }
                    }
                }
            }

            // 6. Recent Files Section
            item {
                Column(modifier = Modifier.padding(top = 22.dp)) {
                    PaddingWrapper {
                        PdfNovaSectionHeader(
                            title = "Recent Files",
                            subtitle = "Processed on your device",
                            actionText = if (recentDocuments.isNotEmpty()) "See All" else null,
                            onActionClick = if (recentDocuments.isNotEmpty()) onNavigateToRecent else null
                        )
                    }

                    if (recentDocuments.isEmpty()) {
                        PdfNovaEmptyState(
                            title = "No recent files",
                            message = "Your recent PDF files will appear here as you process them.",
                            buttonText = "Choose a PDF",
                            onButtonClick = onChoosePdf,
                            icon = Icons.Default.FolderOpen,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            recentDocuments.take(4).forEach { doc ->
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
    }
}

@Composable
private fun PaddingWrapper(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        content()
    }
}

@Composable
private fun QuickActionChip(
    tool: PdfTool,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ),
        modifier = Modifier
            .width(160.dp)
            .height(90.dp)
            .testTag("quick_action_${tool.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryPill(
    category: ToolCategory,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag("category_pill_${category.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NovaPrimaryLight)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
