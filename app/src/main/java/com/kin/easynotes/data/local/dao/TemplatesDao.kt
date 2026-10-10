package com.kin.easynotes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kin.easynotes.domain.model.Template
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTemplate(template: Template)

    @Query("SELECT * FROM `templates-table`")
    fun getAllTemplates(): Flow<List<Template>>

    @Update
    suspend fun updateTemplate(template: Template)

    @Delete
    suspend fun deleteTemplate(template: Template)

    @Query("SELECT * FROM `templates-table` WHERE id=:id")
    fun getTemplateById(id: Int): Flow<Template>
}
