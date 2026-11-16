package com.kin.easynotes.presentation.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kin.easynotes.domain.model.Note
import com.kin.easynotes.domain.model.Template
import com.kin.easynotes.domain.usecase.NoteUseCase
import com.kin.easynotes.domain.usecase.TemplateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    val noteUseCase: NoteUseCase,
    val templateUseCase: TemplateUseCase,
) : ViewModel() {

    private val _journalNotes = MutableStateFlow<List<Note>>(emptyList())
    val journalNotes: StateFlow<List<Note>> = _journalNotes.asStateFlow()

    private val _templates = MutableStateFlow<List<Template>>(emptyList())
    val templates: StateFlow<List<Template>> = _templates.asStateFlow()

    init {
        observeJournals()
        observeTemplates()
    }

    private fun observeJournals() {
        viewModelScope.launch {
            noteUseCase.getJournalNotes().collectLatest { notes ->
                _journalNotes.value = notes.sortedWith(
                    compareBy<Note> { it.journalDate ?: "" }.thenBy { it.createdAt }
                )
            }
        }
    }

    private fun observeTemplates() {
        viewModelScope.launch {
            templateUseCase.getAllTemplates().collectLatest { templates ->
                _templates.value = templates
            }
        }
    }

    fun todayKey(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun todayDisplayTitle(): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
        return LocalDate.now().format(formatter)
    }

    fun getTodaysJournal(): Note? = _journalNotes.value.firstOrNull { it.journalDate == todayKey() }

    suspend fun resolveOrCreateToday(dailyTemplateId: Int): Int {
        val today = todayKey()
        val existing = noteUseCase.getJournalByDate(today)
        if (existing != null) return existing.id

        val selectedTemplate = _templates.value.firstOrNull { it.id == dailyTemplateId }
        val description = selectedTemplate?.description ?: ""

        val note = Note(
            name = todayDisplayTitle(),
            description = description,
            isJournal = true,
            journalDate = today
        )
        noteUseCase.addNote(note)
        val created = noteUseCase.getJournalByDate(today)
        return created?.id ?: 0
    }
}
