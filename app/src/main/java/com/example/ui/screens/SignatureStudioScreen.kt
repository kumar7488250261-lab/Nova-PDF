package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PdfNovaButton
import com.example.ui.components.PdfNovaOutlinedButton
import com.example.ui.components.PdfNovaTopBar

enum class SignatureMode {
    DRAW, TYPE
}

@Composable
fun SignatureStudioScreen(
    onBack: () -> Unit,
    onSignatureReady: (Bitmap) -> Unit
) {
    var mode by remember { mutableStateOf(SignatureMode.DRAW) }
    var typedName by remember { mutableStateOf("") }
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Scaffold(
        topBar = {
            PdfNovaTopBar(
                title = "Signature Studio",
                onBack = onBack,
                actions = {
                    if (mode == SignatureMode.DRAW && (paths.isNotEmpty() || currentPath.isNotEmpty())) {
                        IconButton(
                            onClick = {
                                paths.clear()
                                currentPath = emptyList()
                            },
                            modifier = Modifier.testTag("clear_signature_btn")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Canvas")
                        }
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
                        text = "Cancel",
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        testTag = "cancel_signature_btn"
                    )
                    PdfNovaButton(
                        text = "Apply Signature",
                        onClick = {
                            val bitmap = if (mode == SignatureMode.DRAW) {
                                renderDrawnSignatureToBitmap(paths + listOfNotNull(currentPath.takeIf { it.isNotEmpty() }))
                            } else {
                                renderTypedSignatureToBitmap(if (typedName.isBlank()) "Signature" else typedName)
                            }
                            onSignatureReady(bitmap)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "apply_signature_btn"
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
                .testTag("signature_studio_screen")
        ) {
            // Mode selector tabs
            TabRow(
                selectedTabIndex = mode.ordinal,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = mode == SignatureMode.DRAW,
                    onClick = { mode = SignatureMode.DRAW },
                    text = { Text("Draw") },
                    icon = { Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_draw_signature")
                )
                Tab(
                    selected = mode == SignatureMode.TYPE,
                    onClick = { mode = SignatureMode.TYPE },
                    text = { Text("Type") },
                    icon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_type_signature")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mode == SignatureMode.DRAW) {
                Text(
                    text = "Sign with your finger on the pad below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    currentPath = currentPath + change.position
                                },
                                onDragEnd = {
                                    if (currentPath.isNotEmpty()) {
                                        paths.add(currentPath)
                                        currentPath = emptyList()
                                    }
                                }
                            )
                        }
                        .testTag("signature_draw_pad")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Base signature guide line
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(40f, size.height * 0.75f),
                            end = Offset(size.width - 40f, size.height * 0.75f),
                            strokeWidth = 2f
                        )

                        // Draw completed paths
                        paths.forEach { pathPoints ->
                            if (pathPoints.size > 1) {
                                val p = Path()
                                p.moveTo(pathPoints.first().x, pathPoints.first().y)
                                for (i in 1 until pathPoints.size) {
                                    p.lineTo(pathPoints[i].x, pathPoints[i].y)
                                }
                                drawPath(
                                    path = p,
                                    color = Color(0xFF1E1B4B),
                                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        // Draw current active path
                        if (currentPath.size > 1) {
                            val p = Path()
                            p.moveTo(currentPath.first().x, currentPath.first().y)
                            for (i in 1 until currentPath.size) {
                                p.lineTo(currentPath[i].x, currentPath[i].y)
                            }
                            drawPath(
                                path = p,
                                color = Color(0xFF4338CA),
                                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    if (paths.isEmpty() && currentPath.isEmpty()) {
                        Text(
                            text = "Sign Here ✍",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            } else {
                Text(
                    text = "Type your full name to generate a signature:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = typedName,
                    onValueChange = { typedName = it },
                    placeholder = { Text("e.g. Johnathan Doe") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("type_signature_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Signature preview card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (typedName.isBlank()) "Your Signature" else typedName,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Cursive,
                            color = Color(0xFF1E1B4B)
                        )
                    }
                }
            }
        }
    }
}

private fun renderDrawnSignatureToBitmap(paths: List<List<Offset>>): Bitmap {
    val bitmap = Bitmap.createBitmap(500, 250, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = AndroidColor.BLACK
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    paths.forEach { points ->
        if (points.size > 1) {
            val path = android.graphics.Path()
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            canvas.drawPath(path, paint)
        }
    }
    return bitmap
}

private fun renderTypedSignatureToBitmap(text: String): Bitmap {
    val bitmap = Bitmap.createBitmap(500, 200, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = AndroidColor.BLACK
        textSize = 52f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.ITALIC)
    }
    canvas.drawText(text, 250f, 115f, paint)
    return bitmap
}
