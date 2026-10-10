package com.kin.easynotes.data.repository

import com.kin.easynotes.data.local.database.NoteDatabaseProvider
import com.kin.easynotes.domain.model.Template
import com.kin.easynotes.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val provider: NoteDatabaseProvider
) : TemplateRepository {
    override fun getAllTemplates(): Flow<List<Template>> {
        return provider.templateDao().getAllTemplates()
    }

    override suspend fun addTemplate(template: Template) {
        provider.templateDao().addTemplate(template)
    }

    override suspend fun updateTemplate(template: Template) {
        provider.templateDao().updateTemplate(template)
    }

    override suspend fun deleteTemplate(template: Template) {
        provider.templateDao().deleteTemplate(template)
    }

    override fun getTemplateById(id: Int): Flow<Template> {
        return provider.templateDao().getTemplateById(id)
    }
}
