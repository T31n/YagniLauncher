package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.eblan.launcher.data.room.entity.FolderEblanApplicationInfoEntity
import com.eblan.launcher.data.room.entity.FolderEblanApplicationInfoWrapperEntity
import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemWrapperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderEblanApplicationInfoDao {
    @Transaction
    @Query("SELECT * FROM FolderEblanApplicationInfoEntity")
    fun getFolderEblanApplicationInfoEntities(): List<FolderEblanApplicationInfoEntity>

    @Transaction
    @Query("SELECT * FROM FolderEblanApplicationInfoEntity")
    fun getFolderEblanApplicationInfoWrapperEntitiesFlow(): Flow<List<FolderEblanApplicationInfoWrapperEntity>>

    @Transaction
    @Query("SELECT * FROM FolderEblanApplicationInfoEntity WHERE id = :id")
    suspend fun getFolderEblanApplicationInfoWrapperEntity(id: String): FolderEblanApplicationInfoWrapperEntity?
}