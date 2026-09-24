package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PdfTool
import com.example.data.model.PdfToolsRegistry
import com.example.data.model.ToolCategory
import com.example.ui.components.*

@Composable
fun ToolsScreen(
    onNavigateToTool: (String) -> Unit,
    favoriteToolIds: List<String>,
    onToggleFavorite: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }

    val allTools = remember { PdfToolsRegistry.allTools }
    val filteredTools = remember(searchQuery, selectedCategory) {
        allTools.filter { tool ->
            val matchesCategory = selectedCategory == null || tool.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                tool.name.contains(searchQuery, ignoreCase = true) ||
                tool.description.contains(searchQuery, ignoreCase = true) ||
                tool.keywords.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            PdfNovaTopBar(
                title = "All 33 PDF Tools"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("tools_screen")
        ) {
            // Search Input
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                PdfNovaSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search 33 tools by name or task...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${allTools.size})") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("filter_all")
                    )
                }
                items(ToolCategory.values(), key = { it.name }) { category ->
                    val isSelected = selectedCategory == category
                    val count = allTools.count { it.category == category }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = if (isSelected) null else category },
                        label = { Text("${category.title} ($count)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("filter_${category.name.lowercase()}")
                    )
                }
            }

            // Results count label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTools.size} tools available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PdfNovaPrivacyBadge()
            }

            if (filteredTools.isEmpty()) {
                PdfNovaEmptyState(
                    title = "No tools matched",
                    message = "Try searching with keywords like 'merge', 'protect', 'convert', or 'ocr'.",
                    buttonText = "Clear Search",
                    onButtonClick = {
                        searchQuery = ""
                        selectedCategory = null
                    },
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                // Adaptive 2-column grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTools, key = { it.id }) { tool ->
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
    }
}
