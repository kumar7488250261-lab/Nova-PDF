package com.example.data.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDao {
    @Query("SELECT * FROM recent_documents ORDER BY timestamp DESC")
    fun getAllRecent(): Flow<List<RecentDocument>>

    @Query("SELECT * FROM recent_documents ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentWithLimit(limit: Int): Flow<List<RecentDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(document: RecentDocument): Long

    @Query("DELETE FROM recent_documents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recent_documents")
    suspend fun clearAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT toolId FROM favorite_tools ORDER BY addedTimestamp DESC")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteTool)

    @Query("DELETE FROM favorite_tools WHERE toolId = :toolId")
    suspend fun removeFavorite(toolId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tools WHERE toolId = :toolId)")
    suspend fun isFavorite(toolId: String): Boolean
}

@Database(
    entities = [RecentDocument::class, FavoriteTool::class],
    version = 1,
    exportSchema = false
)
abstract class PdfNovaDatabase : RoomDatabase() {
    abstract fun recentDao(): RecentDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: PdfNovaDatabase? = null

        fun getInstance(context: Context): PdfNovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PdfNovaDatabase::class.java,
                    "pdfnova_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
