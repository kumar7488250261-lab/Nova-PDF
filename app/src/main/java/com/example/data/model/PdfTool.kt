package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val title: String, val subtitle: String) {
    ORGANIZE("Organize PDF", "Merge, split, extract and manage pages"),
    OPTIMIZE("Optimize PDF", "Compress, repair and recognize text"),
    CONVERT_TO("Convert to PDF", "Transform images, docs and web pages to PDF"),
    CONVERT_FROM("Convert from PDF", "Export PDF to Word, Excel, JPG and more"),
    EDIT("Edit PDF", "Rotate, watermark, annotate and fill forms"),
    SECURITY("PDF Security", "Protect, sign, redact and unlock documents"),
    INTELLIGENCE("PDF Intelligence", "AI summarization, translation and markdown")
}

data class PdfTool(
    val id: String,
    val name: String,
    val category: ToolCategory,
    val description: String,
    val icon: ImageVector,
    val keywords: List<String>,
    val isQuickAction: Boolean = false,
    val isAi: Boolean = false,
    val requiresMultiFile: Boolean = false,
    val acceptMimeTypes: List<String> = listOf("application/pdf")
)

object PdfToolsRegistry {
    val allTools: List<PdfTool> = listOf(
        // CATEGORY 1: Organize PDF (6 tools)
        PdfTool(
            id = "merge_pdf",
            name = "Merge PDF",
            category = ToolCategory.ORGANIZE,
            description = "Combine multiple PDF files into one.",
            icon = Icons.AutoMirrored.Filled.CallMerge,
            keywords = listOf("merge", "combine", "join", "stitch", "append", "bundle"),
            isQuickAction = true,
            requiresMultiFile = true
        ),
        PdfTool(
            id = "split_pdf",
            name = "Split PDF",
            category = ToolCategory.ORGANIZE,
            description = "Separate one PDF into multiple documents.",
            icon = Icons.AutoMirrored.Filled.CallSplit,
            keywords = listOf("split", "separate", "divide", "break", "cut", "pages")
        ),
        PdfTool(
            id = "remove_pages",
            name = "Remove Pages",
            category = ToolCategory.ORGANIZE,
            description = "Delete unwanted pages from your document.",
            icon = Icons.Default.DeleteSweep,
            keywords = listOf("remove", "delete", "trash", "discard", "cut pages")
        ),
        PdfTool(
            id = "extract_pages",
            name = "Extract Pages",
            category = ToolCategory.ORGANIZE,
            description = "Extract specific pages and create a new PDF.",
            icon = Icons.Default.FilterFrames,
            keywords = listOf("extract", "select", "pull", "export pages", "isolate")
        ),
        PdfTool(
            id = "organize_pdf",
            name = "Organize PDF",
            category = ToolCategory.ORGANIZE,
            description = "Reorder, rotate, or delete pages visually.",
            icon = Icons.Default.GridView,
            keywords = listOf("organize", "reorder", "sort", "arrange", "manage", "swap")
        ),
        PdfTool(
            id = "scan_to_pdf",
            name = "Scan to PDF",
            category = ToolCategory.ORGANIZE,
            description = "Capture paper documents using your camera.",
            icon = Icons.Default.DocumentScanner,
            keywords = listOf("scan", "camera", "photo", "paper", "digitize", "scanner"),
            isQuickAction = true,
            acceptMimeTypes = listOf("image/*", "application/pdf")
        ),

        // CATEGORY 2: Optimize PDF (3 tools)
        PdfTool(
            id = "compress_pdf",
            name = "Compress PDF",
            category = ToolCategory.OPTIMIZE,
            description = "Reduce file size while preserving quality.",
            icon = Icons.Default.Compress,
            keywords = listOf("compress", "reduce", "shrink", "downsize", "smaller", "kb", "mb"),
            isQuickAction = true
        ),
        PdfTool(
            id = "repair_pdf",
            name = "Repair PDF",
            category = ToolCategory.OPTIMIZE,
            description = "Fix damaged or unreadable PDF files.",
            icon = Icons.Default.Build,
            keywords = listOf("repair", "fix", "recover", "corrupt", "damaged", "restore")
        ),
        PdfTool(
            id = "ocr_pdf",
            name = "OCR PDF",
            category = ToolCategory.OPTIMIZE,
            description = "Convert scanned documents into searchable text.",
            icon = Icons.Default.FindInPage,
            keywords = listOf("ocr", "recognize", "text", "searchable", "scan text", "extract text")
        ),

        // CATEGORY 3: Convert to PDF (5 tools)
        PdfTool(
            id = "jpg_to_pdf",
            name = "JPG to PDF",
            category = ToolCategory.CONVERT_TO,
            description = "Convert images and photos into high-quality PDFs.",
            icon = Icons.Default.Image,
            keywords = listOf("jpg", "jpeg", "png", "image", "photo", "convert to pdf", "picture"),
            acceptMimeTypes = listOf("image/*")
        ),
        PdfTool(
            id = "word_to_pdf",
            name = "Word to PDF",
            category = ToolCategory.CONVERT_TO,
            description = "Convert DOCX documents to standardized PDF.",
            icon = Icons.Default.Description,
            keywords = listOf("word", "doc", "docx", "microsoft word", "office to pdf"),
            acceptMimeTypes = listOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword")
        ),
        PdfTool(
            id = "powerpoint_to_pdf",
            name = "PowerPoint to PDF",
            category = ToolCategory.CONVERT_TO,
            description = "Convert PPT presentations into portable PDF slides.",
            icon = Icons.Default.Slideshow,
            keywords = listOf("powerpoint", "ppt", "pptx", "slides", "presentation to pdf"),
            acceptMimeTypes = listOf("application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/vnd.ms-powerpoint")
        ),
        PdfTool(
            id = "excel_to_pdf",
            name = "Excel to PDF",
            category = ToolCategory.CONVERT_TO,
            description = "Convert spreadsheets and sheets to PDF tables.",
            icon = Icons.Default.TableChart,
            keywords = listOf("excel", "xls", "xlsx", "spreadsheet", "table to pdf"),
            acceptMimeTypes = listOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel")
        ),
        PdfTool(
            id = "html_to_pdf",
            name = "HTML to PDF",
            category = ToolCategory.CONVERT_TO,
            description = "Convert web pages and HTML code into PDF.",
            icon = Icons.Default.Html,
            keywords = listOf("html", "web", "webpage", "url", "link", "website to pdf"),
            acceptMimeTypes = listOf("text/html", "text/plain")
        ),

        // CATEGORY 4: Convert from PDF (5 tools)
        PdfTool(
            id = "pdf_to_jpg",
            name = "PDF to JPG",
            category = ToolCategory.CONVERT_FROM,
            description = "Extract pages as high-resolution images.",
            icon = Icons.Default.PhotoLibrary,
            keywords = listOf("pdf to jpg", "pdf to image", "png", "export images", "extract photos")
        ),
        PdfTool(
            id = "pdf_to_word",
            name = "PDF to Word",
            category = ToolCategory.CONVERT_FROM,
            description = "Convert PDF documents into editable Word files.",
            icon = Icons.AutoMirrored.Filled.Article,
            keywords = listOf("pdf to word", "doc", "docx", "editable text", "word converter"),
            isQuickAction = true
        ),
        PdfTool(
            id = "pdf_to_powerpoint",
            name = "PDF to PowerPoint",
            category = ToolCategory.CONVERT_FROM,
            description = "Turn PDF slides into editable presentations.",
            icon = Icons.Default.PresentToAll,
            keywords = listOf("pdf to ppt", "pptx", "slides", "presentation", "powerpoint")
        ),
        PdfTool(
            id = "pdf_to_excel",
            name = "PDF to Excel",
            category = ToolCategory.CONVERT_FROM,
            description = "Extract PDF tables into editable Excel sheets.",
            icon = Icons.Default.BorderAll,
            keywords = listOf("pdf to excel", "xls", "xlsx", "table", "csv", "spreadsheet")
        ),
        PdfTool(
            id = "pdf_to_pdfa",
            name = "PDF to PDF/A",
            category = ToolCategory.CONVERT_FROM,
            description = "Convert to ISO standard for long-term archiving.",
            icon = Icons.Default.Archive,
            keywords = listOf("pdf/a", "archive", "iso standard", "long term", "compliance")
        ),

        // CATEGORY 5: Edit PDF (6 tools)
        PdfTool(
            id = "rotate_pdf",
            name = "Rotate PDF",
            category = ToolCategory.EDIT,
            description = "Rotate pages 90, 180, or 270 degrees.",
            icon = Icons.AutoMirrored.Filled.RotateRight,
            keywords = listOf("rotate", "orientation", "landscape", "portrait", "turn pages")
        ),
        PdfTool(
            id = "add_page_numbers",
            name = "Add Page Numbers",
            category = ToolCategory.EDIT,
            description = "Insert customizable page numbers and headers.",
            icon = Icons.Default.FormatListNumbered,
            keywords = listOf("page numbers", "header", "footer", "numbering", "pagination")
        ),
        PdfTool(
            id = "add_watermark",
            name = "Add Watermark",
            category = ToolCategory.EDIT,
            description = "Apply confidential, draft, or custom watermarks.",
            icon = Icons.AutoMirrored.Filled.BrandingWatermark,
            keywords = listOf("watermark", "stamp", "draft", "confidential", "brand", "text overlay")
        ),
        PdfTool(
            id = "crop_pdf",
            name = "Crop PDF",
            category = ToolCategory.EDIT,
            description = "Trim document margins and page dimensions.",
            icon = Icons.Default.Crop,
            keywords = listOf("crop", "trim", "margins", "cut margins", "dimensions")
        ),
        PdfTool(
            id = "edit_pdf",
            name = "Edit PDF",
            category = ToolCategory.EDIT,
            description = "Add text, annotations, highlights, and drawings.",
            icon = Icons.Default.Edit,
            keywords = listOf("edit", "annotate", "highlight", "draw", "drawings", "notes")
        ),
        PdfTool(
            id = "pdf_forms",
            name = "PDF Forms",
            category = ToolCategory.EDIT,
            description = "Fill out interactive PDF forms and checkboxes.",
            icon = Icons.AutoMirrored.Filled.Assignment,
            keywords = listOf("form", "fill", "interactive", "checkboxes", "survey", "inputs")
        ),

        // CATEGORY 6: PDF Security (5 tools)
        PdfTool(
            id = "unlock_pdf",
            name = "Unlock PDF",
            category = ToolCategory.SECURITY,
            description = "Remove password protection from secured PDFs.",
            icon = Icons.Default.LockOpen,
            keywords = listOf("unlock", "remove password", "decrypt", "open locked pdf")
        ),
        PdfTool(
            id = "protect_pdf",
            name = "Protect PDF",
            category = ToolCategory.SECURITY,
            description = "Encrypt your PDF with strong 256-bit AES password.",
            icon = Icons.Default.Security,
            keywords = listOf("protect", "password", "encrypt", "aes", "lock", "secure pdf")
        ),
        PdfTool(
            id = "sign_pdf",
            name = "Sign PDF",
            category = ToolCategory.SECURITY,
            description = "Draw, type, or stamp your legally recognized signature.",
            icon = Icons.Default.Draw,
            keywords = listOf("sign", "signature", "e-sign", "draw signature", "stamp sign")
        ),
        PdfTool(
            id = "redact_pdf",
            name = "Redact PDF",
            category = ToolCategory.SECURITY,
            description = "Permanently remove sensitive text and graphics.",
            icon = Icons.Default.VisibilityOff,
            keywords = listOf("redact", "blackout", "censor", "hide", "sensitive info", "privacy")
        ),
        PdfTool(
            id = "compare_pdf",
            name = "Compare PDF",
            category = ToolCategory.SECURITY,
            description = "Compare two PDFs side by side to spot differences.",
            icon = Icons.AutoMirrored.Filled.CompareArrows,
            keywords = listOf("compare", "diff", "differences", "side by side", "changes"),
            requiresMultiFile = true
        ),

        // CATEGORY 7: PDF Intelligence (3 tools)
        PdfTool(
            id = "ai_summarizer",
            name = "AI Summarizer",
            category = ToolCategory.INTELLIGENCE,
            description = "Generate quick summaries and key bullet points.",
            icon = Icons.Default.AutoAwesome,
            keywords = listOf("ai", "gemini", "summary", "summarize", "bullet points", "overview", "key points"),
            isAi = true
        ),
        PdfTool(
            id = "translate_pdf",
            name = "Translate PDF",
            category = ToolCategory.INTELLIGENCE,
            description = "Translate PDF content into over 50 languages.",
            icon = Icons.Default.Translate,
            keywords = listOf("translate", "language", "multilingual", "spanish", "hindi", "french"),
            isAi = true
        ),
        PdfTool(
            id = "pdf_to_markdown",
            name = "PDF to Markdown",
            category = ToolCategory.INTELLIGENCE,
            description = "Convert formatted PDF content to clean Markdown.",
            icon = Icons.Default.Code,
            keywords = listOf("markdown", "md", "convert to md", "clean text", "code formatting"),
            isAi = true
        )
    )

    fun getToolById(id: String): PdfTool? = allTools.find { it.id == id }

    fun searchTools(query: String): List<PdfTool> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return allTools
        return allTools.filter { tool ->
            tool.name.lowercase().contains(q) ||
            tool.description.lowercase().contains(q) ||
            tool.category.title.lowercase().contains(q) ||
            tool.keywords.any { it.contains(q) }
        }
    }

    val quickActionTools: List<PdfTool> = allTools.filter { it.isQuickAction }
}
