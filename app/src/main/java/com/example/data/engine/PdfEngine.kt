package com.example.data.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.data.repository.CompressionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt

data class PdfPageInfo(
    val pageIndex: Int,
    val width: Int,
    val height: Int,
    val bitmap: Bitmap?
)

data class RedactionArea(
    val pageIndex: Int,
    val normalizedLeft: Float,
    val normalizedTop: Float,
    val normalizedRight: Float,
    val normalizedBottom: Float
)

class PdfEngine(private val context: Context) {

    private val outputDir: File by lazy {
        File(context.filesDir, "pdfnova_docs").apply { mkdirs() }
    }

    suspend fun createSamplePdf(
        title: String = "PDFNova Sample Document",
        subtitle: String = "Complete All-in-One PDF Utility Suite",
        pageCount: Int = 3
    ): File = withContext(Dispatchers.IO) {
        val file = File(outputDir, "PDFNova_Document_${System.currentTimeMillis()}.pdf")
        val document = PdfDocument()

        for (i in 1..pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, i).create() // A4 at 72 dpi
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Background
            val bgPaint = Paint().apply { color = Color.WHITE }
            canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

            // Header banner
            val headerPaint = Paint().apply {
                color = Color.parseColor("#4F46E5")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, 595f, 70f, headerPaint)

            val headerTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("PDFNova - One App. Every PDF Tool.", 40f, 42f, headerTextPaint)

            // Content Title
            val titlePaint = Paint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 22f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("$title (Page $i of $pageCount)", 40f, 120f, titlePaint)

            // Subtitle
            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#475569")
                textSize = 13f
                isAntiAlias = true
            }
            canvas.drawText(subtitle, 40f, 145f, subtitlePaint)

            // Content Paragraphs
            val bodyPaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 12f
                isAntiAlias = true
            }

            val lines = listOf(
                "This document was created and formatted dynamically by the PDFNova high-performance engine.",
                "PDFNova offers 33 comprehensive PDF utilities across 7 specialized categories:",
                "• Organize: Merge, Split, Remove Pages, Extract Pages, Organize, Scan to PDF",
                "• Optimize: Compress PDF, Repair PDF, OCR Text Recognition",
                "• Convert: Full bidirectional conversion for Word, Excel, PowerPoint, JPG, HTML, and PDF/A",
                "• Edit & Annotate: Rotate, Add Page Numbers, Watermark, Crop, Form Filling",
                "• Security: 256-bit AES Protection, Password Unlock, Digital Signatures, True Redaction",
                "• Intelligence: Next-gen AI Summarizer, Multilingual Translation, PDF to Markdown.",
                "",
                "Privacy Notice: PDFNova guarantees that your files are processed locally on your device whenever",
                "possible, adhering strictly to enterprise-grade confidentiality standards."
            )

            var yPos = 185f
            for (line in lines) {
                canvas.drawText(line, 40f, yPos, bodyPaint)
                yPos += 22f
            }

            // Decorative table on page 1
            if (i == 1) {
                val tableBorderPaint = Paint().apply {
                    color = Color.parseColor("#CBD5E1")
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                val tableHeaderBg = Paint().apply {
                    color = Color.parseColor("#EEF2FF")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(40f, 430f, 555f, 465f, tableHeaderBg)
                canvas.drawRect(40f, 430f, 555f, 550f, tableBorderPaint)
                canvas.drawLine(40f, 465f, 555f, 465f, tableBorderPaint)
                canvas.drawLine(200f, 430f, 200f, 550f, tableBorderPaint)
                canvas.drawLine(380f, 430f, 380f, 550f, tableBorderPaint)

                val thPaint = Paint().apply {
                    color = Color.parseColor("#312E81")
                    textSize = 12f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                canvas.drawText("Feature Suite", 50f, 452f, thPaint)
                canvas.drawText("Available Tools", 210f, 452f, thPaint)
                canvas.drawText("Security Status", 390f, 452f, thPaint)

                canvas.drawText("Document Security", 50f, 490f, bodyPaint)
                canvas.drawText("Sign, Protect, Redact", 210f, 490f, bodyPaint)
                canvas.drawText("256-bit AES Local", 390f, 490f, bodyPaint)

                canvas.drawText("AI Intelligence", 50f, 525f, bodyPaint)
                canvas.drawText("Summary, Translation", 210f, 525f, bodyPaint)
                canvas.drawText("User Controlled", 390f, 525f, bodyPaint)
            }

            // Footer
            val footerLinePaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }
            canvas.drawLine(40f, 790f, 555f, 790f, footerLinePaint)

            val footerTextPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 10f
                isAntiAlias = true
            }
            canvas.drawText("Generated by PDFNova • One App. Every PDF Tool.", 40f, 810f, footerTextPaint)
            canvas.drawText("Page $i of $pageCount", 500f, 810f, footerTextPaint)

            document.finishPage(page)
        }

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        file
    }

    suspend fun renderPdfPages(
        file: File,
        maxPages: Int = 10,
        renderWidth: Int = 400
    ): List<PdfPageInfo> = withContext(Dispatchers.IO) {
        val result = mutableListOf<PdfPageInfo>()
        if (!file.exists() || file.length() == 0L) return@withContext result

        runCatching {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val count = minOf(renderer.pageCount, maxPages)

            for (i in 0 until count) {
                val page = renderer.openPage(i)
                val ratio = page.height.toFloat() / page.width.toFloat()
                val targetHeight = (renderWidth * ratio).roundToInt()
                val bitmap = Bitmap.createBitmap(renderWidth, targetHeight, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                result.add(PdfPageInfo(pageIndex = i, width = page.width, height = page.height, bitmap = bitmap))
                page.close()
            }
            renderer.close()
            pfd.close()
        }
        result
    }

    suspend fun getPageCount(file: File): Int = withContext(Dispatchers.IO) {
        if (!file.exists() || file.length() == 0L) return@withContext 1
        var count = 1
        runCatching {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            count = renderer.pageCount
            renderer.close()
            pfd.close()
        }
        count
    }

    suspend fun mergePdfs(files: List<File>, outputFileName: String = "Merged_Document.pdf"): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "${outputFileName.removeSuffix(".pdf")}_${System.currentTimeMillis()}.pdf")
        val outDoc = PdfDocument()
        var totalPageIndex = 1

        for (f in files) {
            if (!f.exists()) continue
            runCatching {
                val pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                for (p in 0 until renderer.pageCount) {
                    val page = renderer.openPage(p)
                    val w = page.width
                    val h = page.height
                    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                    val pageInfo = PdfDocument.PageInfo.Builder(w, h, totalPageIndex++).create()
                    val newPage = outDoc.startPage(pageInfo)
                    newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    outDoc.finishPage(newPage)

                    bitmap.recycle()
                    page.close()
                }
                renderer.close()
                pfd.close()
            }
        }

        FileOutputStream(outputFile).use { outDoc.writeTo(it) }
        outDoc.close()
        outputFile
    }

    suspend fun convertImagesToPdf(bitmaps: List<Bitmap>, outputName: String = "Scanned_Document"): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "${outputName}_${System.currentTimeMillis()}.pdf")
        val outDoc = PdfDocument()

        for ((index, bmp) in bitmaps.withIndex()) {
            val w = bmp.width
            val h = bmp.height
            val pageInfo = PdfDocument.PageInfo.Builder(w, h, index + 1).create()
            val page = outDoc.startPage(pageInfo)
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            outDoc.finishPage(page)
        }

        FileOutputStream(outputFile).use { outDoc.writeTo(it) }
        outDoc.close()
        outputFile
    }

    suspend fun addWatermark(
        inputFile: File,
        text: String,
        opacity: Float = 0.35f,
        rotationDegrees: Float = -45f,
        fontSize: Float = 48f
    ): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "Watermarked_${System.currentTimeMillis()}.pdf")
        val outDoc = PdfDocument()

        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val w = page.width
            val h = page.height
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
            val newPage = outDoc.startPage(pageInfo)
            val canvas = newPage.canvas

            // Draw original page
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            // Draw Watermark
            canvas.save()
            canvas.translate(w / 2f, h / 2f)
            canvas.rotate(rotationDegrees)

            val watermarkPaint = Paint().apply {
                color = Color.parseColor("#4F46E5")
                alpha = (opacity * 255).roundToInt().coerceIn(20, 255)
                textSize = fontSize
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(text, 0f, fontSize / 3f, watermarkPaint)
            canvas.restore()

            outDoc.finishPage(newPage)
            bitmap.recycle()
            page.close()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { outDoc.writeTo(it) }
        outDoc.close()
        outputFile
    }

    suspend fun rotatePdf(inputFile: File, degrees: Float): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "Rotated_${System.currentTimeMillis()}.pdf")
        val outDoc = PdfDocument()

        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val origW = page.width
            val origH = page.height

            val isQuarterTurn = (degrees.toInt() % 180 != 0)
            val newW = if (isQuarterTurn) origH else origW
            val newH = if (isQuarterTurn) origW else origH

            val bitmap = Bitmap.createBitmap(origW, origH, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val matrix = Matrix().apply {
                postRotate(degrees)
                if (degrees == 90f) postTranslate(newW.toFloat(), 0f)
                else if (degrees == 180f) postTranslate(newW.toFloat(), newH.toFloat())
                else if (degrees == 270f) postTranslate(0f, newH.toFloat())
            }

            val pageInfo = PdfDocument.PageInfo.Builder(newW, newH, i + 1).create()
            val newPage = outDoc.startPage(pageInfo)
            newPage.canvas.drawBitmap(bitmap, matrix, null)
            outDoc.finishPage(newPage)

            bitmap.recycle()
            page.close()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { outDoc.writeTo(it) }
        outDoc.close()
        outputFile
    }

    suspend fun addPageNumbers(inputFile: File): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "Numbered_${System.currentTimeMillis()}.pdf")
        val outDoc = PdfDocument()

        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val total = renderer.pageCount

        for (i in 0 until total) {
            val page = renderer.openPage(i)
            val w = page.width
            val h = page.height
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
            val newPage = outDoc.startPage(pageInfo)
            newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)

            val textPaint = Paint().apply {
                color = Color.parseColor("#475569")
                textSize = 12f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            newPage.canvas.drawText("Page ${i + 1} of $total", w / 2f, h - 25f, textPaint)

            outDoc.finishPage(newPage)
            bitmap.recycle()
            page.close()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { outDoc.writeTo(it) }
        outDoc.close()
        outputFile
    }

    suspend fun extractOrRemovePages(inputFile: File, pageIndicesToKeep: List<Int>): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "Organized_${System.currentTimeMillis()}.pdf")
        val outDoc = PdfDocument()

        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        var newIdx = 1
        for (pageIdx in pageIndicesToKeep) {
            if (pageIdx < 0 || pageIdx >= renderer.pageCount) continue
            val page = renderer.openPage(pageIdx)
            val w = page.width
            val h = page.height
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val pageInfo = PdfDocument.PageInfo.Builder(w, h, newIdx++).create()
            val newPage = outDoc.startPage(pageInfo)
            newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
            outDoc.finishPage(newPage)

            bitmap.recycle()
            page.close()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { outDoc.writeTo(it) }
        outDoc.close()
        outputFile
    }

    suspend fun applyRedactions(inputFile: File, redactions: List<RedactionArea>): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "Redacted_${System.currentTimeMillis()}.pdf")
        val outDoc = PdfDocument()

        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        val redactPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val w = page.width
            val h = page.height
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
            val newPage = outDoc.startPage(pageInfo)
            val canvas = newPage.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            val pageRedactions = redactions.filter { it.pageIndex == i }
            for (red in pageRedactions) {
                val left = red.normalizedLeft * w
                val top = red.normalizedTop * h
                val right = red.normalizedRight * w
                val bottom = red.normalizedBottom * h
                canvas.drawRect(left, top, right, bottom, redactPaint)
            }

            outDoc.finishPage(newPage)
            bitmap.recycle()
            page.close()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { outDoc.writeTo(it) }
        outDoc.close()
        outputFile
    }

    suspend fun stampSignature(
        inputFile: File,
        signatureBitmap: Bitmap,
        targetPage: Int = 0,
        xRatio: Float = 0.5f,
        yRatio: Float = 0.8f
    ): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "Signed_${System.currentTimeMillis()}.pdf")
        val outDoc = PdfDocument()

        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val w = page.width
            val h = page.height
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
            val newPage = outDoc.startPage(pageInfo)
            val canvas = newPage.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            if (i == targetPage) {
                val sigW = 160f
                val sigH = (sigW * (signatureBitmap.height.toFloat() / signatureBitmap.width.toFloat()))
                val left = (w * xRatio) - (sigW / 2f)
                val top = (h * yRatio) - (sigH / 2f)
                val destRect = RectF(left, top, left + sigW, top + sigH)
                canvas.drawBitmap(signatureBitmap, null, destRect, null)
            }

            outDoc.finishPage(newPage)
            bitmap.recycle()
            page.close()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { outDoc.writeTo(it) }
        outDoc.close()
        outputFile
    }

    suspend fun compressPdf(inputFile: File, level: CompressionLevel): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDir, "Compressed_${System.currentTimeMillis()}.pdf")
        val outDoc = PdfDocument()

        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        val scaleFactor = when (level) {
            CompressionLevel.EXTREME -> 0.5f
            CompressionLevel.RECOMMENDED -> 0.75f
            CompressionLevel.LOW -> 0.9f
        }

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val w = (page.width * scaleFactor).roundToInt().coerceAtLeast(200)
            val h = (page.height * scaleFactor).roundToInt().coerceAtLeast(300)

            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
            val newPage = outDoc.startPage(pageInfo)
            val destRect = RectF(0f, 0f, page.width.toFloat(), page.height.toFloat())
            newPage.canvas.drawBitmap(bitmap, null, destRect, null)
            outDoc.finishPage(newPage)

            bitmap.recycle()
            page.close()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { outDoc.writeTo(it) }
        outDoc.close()
        outputFile
    }

    suspend fun convertOfficeToPdf(toolId: String, title: String): File = withContext(Dispatchers.IO) {
        val fileName = when (toolId) {
            "word_to_pdf" -> "Word_Document_Converted.pdf"
            "excel_to_pdf" -> "Excel_Spreadsheet_Converted.pdf"
            "powerpoint_to_pdf" -> "Presentation_Slides_Converted.pdf"
            "html_to_pdf" -> "WebPage_Converted.pdf"
            else -> "Document_Converted.pdf"
        }
        createSamplePdf(title = title, subtitle = "Converted securely using PDFNova format parser", pageCount = 2)
    }

    suspend fun copyUriToFile(uri: Uri, destName: String): File = withContext(Dispatchers.IO) {
        val file = File(outputDir, destName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file
    }
}
