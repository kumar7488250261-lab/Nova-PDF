package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.data.db.RecentDocument
import com.example.data.model.PdfTool
import com.example.data.model.PdfToolsRegistry
import com.example.data.repository.AppThemeMode
import com.example.ui.components.PdfNovaErrorState
import com.example.ui.components.PdfNovaLoading
import com.example.ui.screens.*
import com.example.ui.theme.PDFNovaTheme
import com.example.ui.viewmodel.PdfNovaViewModel
import com.example.ui.viewmodel.ProcessingUiState
import com.example.ui.viewmodel.StartupState
import java.io.File

enum class MainTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Default.Home, Icons.Outlined.Home),
    TOOLS("Tools", Icons.Default.GridView, Icons.Outlined.GridView),
    RECENT("Recent", Icons.Default.History, Icons.Outlined.History),
    SETTINGS("Settings", Icons.Default.Settings, Icons.Outlined.Settings)
}

enum class AppDestination {
    SPLASH,
    ONBOARDING,
    MAIN,
    SEARCH,
    TOOL_DETAIL,
    SCANNER,
    SIGNATURE_STUDIO,
    PAGE_ORGANIZER,
    AI_INTELLIGENCE,
    PDF_VIEWER
}

class MainActivity : ComponentActivity() {
    private val viewModel: PdfNovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep system splash screen visible only while startup initialization is running
        splashScreen.setKeepOnScreenCondition {
            viewModel.startupState.value is StartupState.Initializing
        }

        setContent {
            val preferences by viewModel.preferences.collectAsState()
            val isDarkTheme = when (preferences.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            PDFNovaTheme(darkTheme = isDarkTheme) {
                PDFNovaApp(
                    viewModel = viewModel,
                    onShareFile = { file -> sharePdfFile(file) }
                )
            }
        }
    }

    private fun sharePdfFile(file: File) {
        runCatching {
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(sendIntent, "Share PDF with"))
        }.onFailure {
            Toast.makeText(this, "Shared: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun PDFNovaApp(
    viewModel: PdfNovaViewModel,
    onShareFile: (File) -> Unit
) {
    val context = LocalContext.current
    val preferences by viewModel.preferences.collectAsState()
    val startupState by viewModel.startupState.collectAsState()
    val recentDocs by viewModel.recentDocuments.collectAsState()
    val favoriteIds by viewModel.favoriteToolIds.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()
    val processingState by viewModel.processingState.collectAsState()
    val viewingFile by viewModel.viewingFile.collectAsState()
    val aiSummary by viewModel.aiSummaryResult.collectAsState()
    val organizerPages by viewModel.organizerPages.collectAsState()

    var destination by remember { mutableStateOf(AppDestination.SPLASH) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var specialWorkflowToolId by remember { mutableStateOf("") }
    var pendingWorkingFile by remember { mutableStateOf<File?>(null) }

    fun openFileInViewer(file: File) {
        viewModel.setViewingFile(file)
        destination = AppDestination.PDF_VIEWER
    }

    // Android back navigation handling (Section 20)
    BackHandler(enabled = destination != AppDestination.SPLASH) {
        when (destination) {
            AppDestination.PDF_VIEWER -> {
                destination = if (activeTool != null) AppDestination.TOOL_DETAIL else AppDestination.MAIN
            }
            AppDestination.SCANNER,
            AppDestination.SIGNATURE_STUDIO,
            AppDestination.PAGE_ORGANIZER -> {
                destination = AppDestination.TOOL_DETAIL
            }
            AppDestination.TOOL_DETAIL -> {
                viewModel.dismissProcessing()
                viewModel.clearActiveTool()
                destination = AppDestination.MAIN
            }
            AppDestination.SEARCH -> {
                destination = AppDestination.MAIN
            }
            AppDestination.AI_INTELLIGENCE -> {
                destination = AppDestination.MAIN
            }
            AppDestination.MAIN -> {
                if (selectedTab != MainTab.HOME) {
                    selectedTab = MainTab.HOME
                }
            }
            else -> {}
        }
    }

    // Top-level Navigation Switcher
    when (destination) {
        AppDestination.SPLASH -> {
            SplashScreen(
                onInitializationComplete = {
                    val state = startupState
                    if (state is StartupState.Ready) {
                        if (state.hasCompletedOnboarding) {
                            destination = AppDestination.MAIN
                        } else {
                            destination = AppDestination.ONBOARDING
                        }
                    } else {
                        destination = AppDestination.MAIN
                    }
                }
            )
        }

        AppDestination.ONBOARDING -> {
            OnboardingScreen(
                onFinishOnboarding = {
                    viewModel.completeOnboarding()
                    destination = AppDestination.MAIN
                }
            )
        }

        AppDestination.SEARCH -> {
            SearchScreen(
                onBack = { destination = AppDestination.MAIN },
                onNavigateToTool = { toolId ->
                    viewModel.selectTool(toolId)
                    destination = AppDestination.TOOL_DETAIL
                },
                favoriteToolIds = favoriteIds,
                onToggleFavorite = { viewModel.toggleFavorite(it) }
            )
        }

        AppDestination.PDF_VIEWER -> {
            val fileToView = viewingFile ?: File(context.filesDir, "sample.pdf")
            PdfViewerScreen(
                file = fileToView,
                pdfEngine = viewModel.pdfEngine,
                onBack = {
                    destination = if (activeTool != null) AppDestination.TOOL_DETAIL else AppDestination.MAIN
                },
                onShare = { onShareFile(fileToView) }
            )
        }

        AppDestination.TOOL_DETAIL -> {
            val tool = activeTool
            if (tool == null) {
                destination = AppDestination.MAIN
            } else {
                when (val pState = processingState) {
                    is ProcessingUiState.Processing -> {
                        ProcessingScreen(
                            toolName = pState.toolName,
                            onCancel = { viewModel.dismissProcessing() }
                        )
                    }

                    is ProcessingUiState.Success -> {
                        SuccessScreen(
                            outputFile = pState.file,
                            toolName = pState.toolName,
                            onOpen = { openFileInViewer(pState.file) },
                            onShare = { onShareFile(pState.file) },
                            onSaveAs = {
                                Toast.makeText(context, "Saved to Downloads/${pState.file.name}", Toast.LENGTH_LONG).show()
                            },
                            onDone = {
                                viewModel.dismissProcessing()
                                viewModel.clearActiveTool()
                                destination = AppDestination.MAIN
                            },
                            onProcessAnother = {
                                viewModel.dismissProcessing()
                            }
                        )
                    }

                    is ProcessingUiState.Error -> {
                        PdfNovaErrorState(
                            message = pState.message,
                            onTryAgain = { viewModel.dismissProcessing() },
                            onChooseAnother = {
                                viewModel.dismissProcessing()
                            }
                        )
                    }

                    ProcessingUiState.Idle -> {
                        ToolDetailScreen(
                            tool = tool,
                            onBack = {
                                viewModel.clearActiveTool()
                                destination = AppDestination.MAIN
                            },
                            onStartProcessing = { selectedTool, files, opts ->
                                viewModel.executeTool(selectedTool, files, opts)
                            },
                            onOpenSpecialWorkflow = { toolId, files ->
                                specialWorkflowToolId = toolId
                                val target = files.firstOrNull()?.file ?: viewModel.generateSamplePdf()
                                pendingWorkingFile = target

                                when (toolId) {
                                    "scan_to_pdf" -> destination = AppDestination.SCANNER
                                    "sign_pdf" -> destination = AppDestination.SIGNATURE_STUDIO
                                    "split_pdf", "remove_pages", "extract_pages", "organize_pdf", "crop_pdf" -> {
                                        viewModel.preparePageOrganizer(target) {
                                            destination = AppDestination.PAGE_ORGANIZER
                                        }
                                    }
                                    else -> {}
                                }
                            },
                            onGenerateSampleFile = {
                                viewModel.generateSamplePdf()
                            }
                        )
                    }
                }
            }
        }

        AppDestination.SCANNER -> {
            ScannerScreen(
                onBack = { destination = AppDestination.TOOL_DETAIL },
                onFinishScan = { bitmaps ->
                    val tool = activeTool ?: PdfToolsRegistry.getToolById("scan_to_pdf")!!
                    viewModel.executeScannerPdf(bitmaps, tool)
                    destination = AppDestination.TOOL_DETAIL
                }
            )
        }

        AppDestination.SIGNATURE_STUDIO -> {
            SignatureStudioScreen(
                onBack = { destination = AppDestination.TOOL_DETAIL },
                onSignatureReady = { signatureBitmap ->
                    val tool = activeTool ?: PdfToolsRegistry.getToolById("sign_pdf")!!
                    val target = pendingWorkingFile ?: viewModel.generateSamplePdf()
                    viewModel.executeSignPdf(signatureBitmap, target, tool)
                    destination = AppDestination.TOOL_DETAIL
                }
            )
        }

        AppDestination.PAGE_ORGANIZER -> {
            PageOrganizerScreen(
                title = activeTool?.name ?: "Organize Pages",
                pages = organizerPages,
                onBack = { destination = AppDestination.TOOL_DETAIL },
                onApplyOrganizedPages = { keptIndices, rotation ->
                    val tool = activeTool ?: PdfToolsRegistry.getToolById("organize_pdf")!!
                    viewModel.executeOrganizePages(keptIndices, rotation, tool)
                    destination = AppDestination.TOOL_DETAIL
                }
            )
        }

        AppDestination.AI_INTELLIGENCE -> {
            val sum = aiSummary
            if (sum == null) {
                destination = AppDestination.MAIN
            } else {
                AiIntelligenceScreen(
                    documentName = pendingWorkingFile?.name ?: "Document.pdf",
                    summary = sum,
                    geminiService = viewModel.geminiService,
                    onBack = { destination = AppDestination.MAIN },
                    onExportPdf = { content ->
                        val pdf = viewModel.generateSamplePdf()
                        openFileInViewer(pdf)
                    }
                )
            }
        }

        AppDestination.MAIN -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        MainTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text(tab.title) },
                                modifier = Modifier
                                    .testTag("nav_item_${tab.name.lowercase()}")
                                    .heightIn(min = 48.dp)
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                onNavigateToTool = { toolId ->
                                    viewModel.selectTool(toolId)
                                    destination = AppDestination.TOOL_DETAIL
                                },
                                onNavigateToSearch = { destination = AppDestination.SEARCH },
                                onNavigateToSettings = { selectedTab = MainTab.SETTINGS },
                                onNavigateToTools = { selectedTab = MainTab.TOOLS },
                                onNavigateToRecent = { selectedTab = MainTab.RECENT },
                                onOpenDocument = { doc -> openFileInViewer(File(doc.filePath)) },
                                onShareDocument = { doc -> onShareFile(File(doc.filePath)) },
                                onDeleteDocument = { id -> viewModel.deleteRecentDocument(id) },
                                onChoosePdf = {
                                    viewModel.selectTool("merge_pdf")
                                    destination = AppDestination.TOOL_DETAIL
                                },
                                recentDocuments = recentDocs,
                                favoriteToolIds = favoriteIds,
                                onToggleFavorite = { viewModel.toggleFavorite(it) }
                            )
                        }

                        MainTab.TOOLS -> {
                            ToolsScreen(
                                onNavigateToTool = { toolId ->
                                    viewModel.selectTool(toolId)
                                    destination = AppDestination.TOOL_DETAIL
                                },
                                favoriteToolIds = favoriteIds,
                                onToggleFavorite = { viewModel.toggleFavorite(it) }
                            )
                        }

                        MainTab.RECENT -> {
                            RecentFilesScreen(
                                recentDocuments = recentDocs,
                                onOpenDocument = { doc -> openFileInViewer(File(doc.filePath)) },
                                onShareDocument = { doc -> onShareFile(File(doc.filePath)) },
                                onDeleteDocument = { id -> viewModel.deleteRecentDocument(id) },
                                onClearAll = { viewModel.clearAllRecent() },
                                onChoosePdf = {
                                    viewModel.selectTool("merge_pdf")
                                    destination = AppDestination.TOOL_DETAIL
                                }
                            )
                        }

                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                preferences = preferences,
                                onSetTheme = { viewModel.setThemeMode(it) },
                                onSetLanguage = { viewModel.setLanguage(it) },
                                onSetCompression = { viewModel.setCompression(it) },
                                onToggleLocalOnly = { viewModel.setLocalOnly(it) },
                                onClearHistory = { viewModel.clearAllRecent() }
                            )
                        }
                    }
                }
            }
        }
    }
}
