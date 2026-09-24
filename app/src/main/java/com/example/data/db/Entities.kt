package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_documents")
data class RecentDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val pageCount: Int,
    val toolUsed: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_tools")
data class FavoriteTool(
    @PrimaryKey
    val toolId: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)
