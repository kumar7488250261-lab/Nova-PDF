package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PdfToolsRegistry
import com.example.ui.components.*

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNavigateToTool: (String) -> Unit,
    favoriteToolIds: List<String>,
    onToggleFavorite: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResults = remember(query) { PdfToolsRegistry.searchTools(query) }

    val popularTags = listOf("compress", "sign", "photo", "security", "merge", "split", "word", "ai", "watermark", "protect")

    Scaffold(
        topBar = {
            PdfNovaTopBar(
                title = "Search PDF Tools",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("search_screen")
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                PdfNovaSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "Type e.g. compress, sign, photo, security...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick keyword suggestion chips
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Popular Searches",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(popularTags) { tag ->
                        SuggestionChip(
                            onClick = { query = tag },
                            label = { Text(tag) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("tag_chip_$tag")
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (searchResults.isEmpty()) {
                PdfNovaEmptyState(
                    title = "No PDF tools found",
                    message = "We couldn't find any tool matching '$query'. Try another keyword or browse all 33 tools.",
                    buttonText = "Clear Search",
                    onButtonClick = { query = "" },
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = if (query.isEmpty()) "All Tools (${searchResults.size})" else "Results for \"$query\" (${searchResults.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(searchResults, key = { it.id }) { tool ->
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
