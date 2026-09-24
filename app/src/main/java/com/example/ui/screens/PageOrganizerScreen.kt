package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.engine.PdfPageInfo
import com.example.ui.components.PdfNovaButton
import com.example.ui.components.PdfNovaOutlinedButton
import com.example.ui.components.PdfNovaTopBar
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPrimaryLight

data class PageItem(
    val originalIndex: Int,
    val bitmap: Bitmap?,
    var rotation: Float = 0f
)

@Composable
fun PageOrganizerScreen(
    title: String = "Organize Pages",
    pages: List<PdfPageInfo>,
    onBack: () -> Unit,
    onApplyOrganizedPages: (keptOriginalIndices: List<Int>, rotationDegrees: Float) -> Unit
) {
    val pageList = remember {
        mutableStateListOf<PageItem>().apply {
            addAll(pages.map { PageItem(originalIndex = it.pageIndex, bitmap = it.bitmap) })
        }
    }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var globalRotation by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            PdfNovaTopBar(
                title = title,
                onBack = onBack,
                actions = {
                    TextButton(
                        onClick = {
                            if (selectedIndices.size == pageList.size) {
                                selectedIndices.clear()
                            } else {
                                selectedIndices.clear()
                                selectedIndices.addAll(pageList.indices)
                            }
                        }
                    ) {
                        Text(if (selectedIndices.size == pageList.size) "Deselect All" else "Select All")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Actions row (Rotate, Delete selected)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pageList.size} page(s) remaining",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    globalRotation = (globalRotation + 90f) % 360f
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("organizer_rotate_all_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rotate 90°")
                            }

                            if (selectedIndices.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        val sorted = selectedIndices.sortedDescending()
                                        sorted.forEach { idx ->
                                            if (idx in pageList.indices) pageList.removeAt(idx)
                                        }
                                        selectedIndices.clear()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NovaError),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("organizer_delete_selected_btn")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete (${selectedIndices.size})")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PdfNovaButton(
                        text = "Apply Changes (${pageList.size} Pages)",
                        onClick = {
                            val keptIndices = pageList.map { it.originalIndex }
                            onApplyOrganizedPages(keptIndices, globalRotation)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = pageList.isNotEmpty(),
                        testTag = "organizer_apply_btn"
                    )
                }
            }
        }
    ) { paddingValues ->
        if (pageList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("All pages were removed.", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .testTag("organizer_page_grid"),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(pageList) { index, item ->
                    val isSelected = selectedIndices.contains(index)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) NovaPrimaryLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                if (isSelected) selectedIndices.remove(index)
                                else selectedIndices.add(index)
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (item.bitmap != null) {
                                Image(
                                    bitmap = item.bitmap.asImageBitmap(),
                                    contentDescription = "Page ${index + 1}",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .rotate(globalRotation),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                }
                            }

                            // Page number badge
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            // Selection Checkbox Badge
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIndices.add(index)
                                    else selectedIndices.remove(index)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
