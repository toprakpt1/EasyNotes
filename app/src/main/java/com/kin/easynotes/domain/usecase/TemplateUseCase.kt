package com.kin.easynotes.domain.usecase

import com.kin.easynotes.data.repository.TemplateRepositoryImpl
import com.kin.easynotes.domain.model.Template
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepositoryImpl
) {
    var templates: List<Template> = emptyList()
        private set

    fun getAllTemplates(): Flow<List<Template>> {
        return templateRepository.getAllTemplates()
    }

    suspend fun addTemplate(template: Template) {
        templateRepository.addTemplate(template)
    }

    suspend fun updateTemplate(template: Template) {
        templateRepository.updateTemplate(template)
    }

    suspend fun deleteTemplate(template: Template) {
        templateRepository.deleteTemplate(template)
    }

    fun getTemplateById(id: Int): Flow<Template> {
        return templateRepository.getTemplateById(id)
    }
}
