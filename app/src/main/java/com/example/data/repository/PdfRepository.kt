package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.FavoriteDao
import com.example.data.db.FavoriteTool
import com.example.data.db.PdfNovaDatabase
import com.example.data.db.RecentDao
import com.example.data.db.RecentDocument
import com.example.data.model.PdfTool
import com.example.data.model.PdfToolsRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class CompressionLevel(val label: String, val description: String) {
    RECOMMENDED("Recommended", "Good quality, medium size (70% reduction)"),
    EXTREME("Extreme", "Lower quality, smallest size (90% reduction)"),
    LOW("Low", "Best quality, minor size reduction (30% reduction)")
}

data class AppPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val language: String = "English",
    val defaultCompression: CompressionLevel = CompressionLevel.RECOMMENDED,
    val defaultDpi: String = "150 DPI",
    val localProcessingOnly: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)

class PdfRepository(private val context: Context) {
    private val database = PdfNovaDatabase.getInstance(context)
    private val recentDao: RecentDao = database.recentDao()
    private val favoriteDao: FavoriteDao = database.favoriteDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("pdfnova_settings", Context.MODE_PRIVATE)

    private val _appPreferences = MutableStateFlow(loadPreferences())
    val appPreferences: StateFlow<AppPreferences> = _appPreferences.asStateFlow()

    val allRecentDocuments: Flow<List<RecentDocument>> = recentDao.getAllRecent()
    val homeRecentDocuments: Flow<List<RecentDocument>> = recentDao.getRecentWithLimit(5)
    val favoriteToolIds: Flow<List<String>> = favoriteDao.getAllFavoriteIds()

    private fun loadPreferences(): AppPreferences {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val theme = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM)
        val lang = prefs.getString("language", "English") ?: "English"
        val compStr = prefs.getString("compression", CompressionLevel.RECOMMENDED.name) ?: CompressionLevel.RECOMMENDED.name
        val comp = runCatching { CompressionLevel.valueOf(compStr) }.getOrDefault(CompressionLevel.RECOMMENDED)
        val dpi = prefs.getString("dpi", "150 DPI") ?: "150 DPI"
        val localOnly = prefs.getBoolean("local_only", true)
        val onboarding = prefs.getBoolean("onboarding_complete", false)
        return AppPreferences(
            themeMode = theme,
            language = lang,
            defaultCompression = comp,
            defaultDpi = dpi,
            localProcessingOnly = localOnly,
            hasCompletedOnboarding = onboarding
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) = withContext(Dispatchers.IO) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _appPreferences.value = _appPreferences.value.copy(themeMode = mode)
    }

    suspend fun setLanguage(lang: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString("language", lang).apply()
        _appPreferences.value = _appPreferences.value.copy(language = lang)
    }

    suspend fun setCompressionLevel(level: CompressionLevel) = withContext(Dispatchers.IO) {
        prefs.edit().putString("compression", level.name).apply()
        _appPreferences.value = _appPreferences.value.copy(defaultCompression = level)
    }

    suspend fun setOnboardingComplete(complete: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean("onboarding_complete", complete).apply()
        _appPreferences.value = _appPreferences.value.copy(hasCompletedOnboarding = complete)
    }

    suspend fun setLocalProcessingOnly(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean("local_only", enabled).apply()
        _appPreferences.value = _appPreferences.value.copy(localProcessingOnly = enabled)
    }

    suspend fun addRecentDocument(fileName: String, filePath: String, fileSize: Long, pageCount: Int, toolUsed: String): Long = withContext(Dispatchers.IO) {
        val doc = RecentDocument(
            fileName = fileName,
            filePath = filePath,
            fileSizeBytes = fileSize,
            pageCount = pageCount,
            toolUsed = toolUsed
        )
        recentDao.insertRecent(doc)
    }

    suspend fun deleteRecentDocument(id: Long) = withContext(Dispatchers.IO) {
        recentDao.deleteById(id)
    }

    suspend fun clearAllRecent() = withContext(Dispatchers.IO) {
        recentDao.clearAll()
    }

    suspend fun toggleFavorite(toolId: String) = withContext(Dispatchers.IO) {
        if (favoriteDao.isFavorite(toolId)) {
            favoriteDao.removeFavorite(toolId)
        } else {
            favoriteDao.addFavorite(FavoriteTool(toolId = toolId))
        }
    }

    suspend fun isFavorite(toolId: String): Boolean = withContext(Dispatchers.IO) {
        favoriteDao.isFavorite(toolId)
    }
}
