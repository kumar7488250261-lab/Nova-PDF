package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PdfNovaButton
import com.example.ui.components.PdfNovaOutlinedButton
import com.example.ui.components.PdfNovaTopBar

enum class ScanFilter(val label: String) {
    ORIGINAL("Original"),
    COLOR("Enhanced Color"),
    GRAYSCALE("Grayscale"),
    BW("B&W Document")
}

data class ScannedPage(
    val id: Long = System.currentTimeMillis(),
    val originalBitmap: Bitmap,
    var processedBitmap: Bitmap,
    var filter: ScanFilter = ScanFilter.ORIGINAL,
    var rotation: Float = 0f
)

@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onFinishScan: (List<Bitmap>) -> Unit
) {
    val context = LocalContext.current
    val scannedPages = remember { mutableStateListOf<ScannedPage>() }
    var selectedPageIndex by remember { mutableIntStateOf(0) }

    // Sample scanned placeholder bitmap generator if camera not present
    fun createSampleScannedPage(index: Int): Bitmap {
        val bmp = Bitmap.createBitmap(600, 850, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bgPaint = Paint().apply { color = android.graphics.Color.WHITE }
        canvas.drawRect(0f, 0f, 600f, 850f, bgPaint)

        val headerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#4F46E5")
            textSize = 24f
            isFakeBoldText = true
        }
        canvas.drawText("Scanned Receipt / Document #$index", 40f, 60f, headerPaint)

        val textPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 14f
        }
        canvas.drawText("Item 1: Professional Office Supplies ... $42.50", 40f, 120f, textPaint)
        canvas.drawText("Item 2: Software License Renewal ....... $99.00", 40f, 150f, textPaint)
        canvas.drawText("Total Paid: $141.50 • Verified via PDFNova Scanner", 40f, 210f, textPaint)
        return bmp
    }

    // Photo picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        scannedPages.add(ScannedPage(originalBitmap = bmp, processedBitmap = bmp))
                    }
                }
            }
        }
    }

    // Populate with 1 page on start if empty
    LaunchedEffect(Unit) {
        if (scannedPages.isEmpty()) {
            val sampleBmp = createSampleScannedPage(1)
            scannedPages.add(ScannedPage(originalBitmap = sampleBmp, processedBitmap = sampleBmp))
        }
    }

    val currentPage = scannedPages.getOrNull(selectedPageIndex)

    Scaffold(
        topBar = {
            PdfNovaTopBar(
                title = "Scan to PDF",
                onBack = onBack,
                actions = {
                    if (scannedPages.isNotEmpty()) {
                        Text(
                            text = "Page ${selectedPageIndex + 1}/${scannedPages.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
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
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PdfNovaOutlinedButton(
                        text = "+ Add Page",
                        onClick = {
                            val newBmp = createSampleScannedPage(scannedPages.size + 1)
                            scannedPages.add(ScannedPage(originalBitmap = newBmp, processedBitmap = newBmp))
                            selectedPageIndex = scannedPages.lastIndex
                        },
                        icon = Icons.Default.AddPhotoAlternate,
                        modifier = Modifier.weight(1f),
                        testTag = "scanner_add_page_btn"
                    )

                    PdfNovaButton(
                        text = "Create PDF (${scannedPages.size})",
                        onClick = {
                            val bitmaps = scannedPages.map { it.processedBitmap }
                            onFinishScan(bitmaps)
                        },
                        icon = Icons.Default.PictureAsPdf,
                        modifier = Modifier.weight(1.2f),
                        testTag = "scanner_create_pdf_btn"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .testTag("scanner_screen")
        ) {
            // Main page preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.DarkGray)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (currentPage != null) {
                    Image(
                        bitmap = currentPage.processedBitmap.asImageBitmap(),
                        contentDescription = "Scanned Page",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter options chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ScanFilter.values().forEach { filter ->
                    val isSelected = currentPage?.filter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (currentPage != null) {
                                currentPage.filter = filter
                                currentPage.processedBitmap = applyFilterToBitmap(currentPage.originalBitmap, filter)
                            }
                        },
                        label = { Text(filter.label, fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thumbnail strip of pages
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(scannedPages) { index, page ->
                    val isSelected = selectedPageIndex == index
                    Box(
                        modifier = Modifier
                            .size(70.dp, 90.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedPageIndex = index }
                    ) {
                        Image(
                            bitmap = page.processedBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                item {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Pick Image")
                    }
                }
            }
        }
    }
}

private fun applyFilterToBitmap(src: Bitmap, filter: ScanFilter): Bitmap {
    val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    val paint = Paint()

    val cm = ColorMatrix()
    when (filter) {
        ScanFilter.ORIGINAL -> {
            // No filter
        }
        ScanFilter.COLOR -> {
            cm.setSaturation(1.3f)
            paint.colorFilter = ColorMatrixColorFilter(cm)
        }
        ScanFilter.GRAYSCALE -> {
            cm.setSaturation(0f)
            paint.colorFilter = ColorMatrixColorFilter(cm)
        }
        ScanFilter.BW -> {
            cm.setSaturation(0f)
            val contrast = 1.6f
            val scale = contrast
            val translate = (-0.5f * contrast + 0.5f) * 255f
            cm.set(floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
            paint.colorFilter = ColorMatrixColorFilter(cm)
        }
    }
    canvas.drawBitmap(src, 0f, 0f, paint)
    return out
}
