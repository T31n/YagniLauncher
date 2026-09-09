package com.eblan.launcher.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = FolderEblanApplicationInfoEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("folderId"),
    ],
)
data class FolderEblanApplicationInfoEntity(
    @PrimaryKey
    val id: String,
    val folderIndex: Int,
    val folderId: String?,
)
