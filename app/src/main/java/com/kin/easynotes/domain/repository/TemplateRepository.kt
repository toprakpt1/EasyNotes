package com.kin.easynotes.domain.repository

import com.kin.easynotes.domain.model.Template
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getAllTemplates(): Flow<List<Template>>
    suspend fun addTemplate(template: Template)
    suspend fun updateTemplate(template: Template)
    suspend fun deleteTemplate(template: Template)
    fun getTemplateById(id: Int): Flow<Template>
}
