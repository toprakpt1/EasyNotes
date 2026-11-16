package com.kin.easynotes.presentation.screens.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kin.easynotes.R
import com.kin.easynotes.domain.model.Note
import com.kin.easynotes.domain.model.Template
import com.kin.easynotes.presentation.components.material.MaterialScaffold
import com.kin.easynotes.presentation.components.material.MaterialBar
import com.kin.easynotes.presentation.navigation.NavRoutes
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val notes by viewModel.journalNotes.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val scope = rememberCoroutineScope()

    var showTemplatePicker by remember { mutableStateOf(false) }
    var showTemplateEditor by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<Template?>(null) }

    val dailyTemplateId = settingsViewModel.settings.value.dailyTemplateId
    val selectedTemplate = templates.firstOrNull { it.id == dailyTemplateId }
    val today = viewModel.todayKey()
    val todaysEntry = notes.firstOrNull { it.journalDate == today }

    if (showTemplatePicker) {
        AlertDialog(
            onDismissRequest = { showTemplatePicker = false },
            title = { Text(stringResource(R.string.select_journal_template)) },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                settingsViewModel.update(settingsViewModel.settings.value.copy(dailyTemplateId = 0))
                                showTemplatePicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.no_journal_template))
                        }
                    }
                    items(templates) { template ->
                        TextButton(
                            onClick = {
                                settingsViewModel.update(settingsViewModel.settings.value.copy(dailyTemplateId = template.id))
                                showTemplatePicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(template.name, modifier = Modifier.weight(1f))
                                if (template.id == dailyTemplateId) {
                                    Icon(Icons.Rounded.Check, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTemplatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showTemplateEditor) {
        TemplateEditorDialog(
            template = editingTemplate,
            onDismiss = {
                showTemplateEditor = false
                editingTemplate = null
            },
            onSave = { name, description ->
                scope.launch {
                    if (editingTemplate == null) {
                        settingsViewModel.templateUseCase.addTemplate(Template(name = name, description = description))
                    } else {
                        settingsViewModel.templateUseCase.updateTemplate(
                            editingTemplate!!.copy(name = name, description = description)
                        )
                    }
                }
                showTemplateEditor = false
                editingTemplate = null
            }
        )
    }

    MaterialScaffold(
        topBar = {
            MaterialBar(
                title = stringResource(R.string.journal),
                onBackNavClicked = { navController.navigateUp() }
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    val id = viewModel.resolveOrCreateToday(settingsViewModel.settings.value.dailyTemplateId)
                                    if (id != 0) {
                                        navController.navigate(NavRoutes.Edit.createRoute(id, false))
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Today, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (todaysEntry != null) stringResource(R.string.open_todays_journal)
                                    else stringResource(R.string.write_todays_journal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = viewModel.todayDisplayTitle(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Description, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.journal_template),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = selectedTemplate?.name ?: stringResource(R.string.no_journal_template),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { showTemplatePicker = true }) {
                                    Text(stringResource(R.string.select_template))
                                }
                                TextButton(onClick = {
                                    editingTemplate = null
                                    showTemplateEditor = true
                                }) {
                                    Text(stringResource(R.string.add_journal_template))
                                }
                                if (selectedTemplate != null) {
                                    TextButton(onClick = {
                                        editingTemplate = selectedTemplate
                                        showTemplateEditor = true
                                    }) {
                                        Text(stringResource(R.string.edit_journal_template))
                                    }
                                    IconButton(onClick = {
                                        settingsViewModel.update(settingsViewModel.settings.value.copy(dailyTemplateId = 0))
                                    }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = stringResource(R.string.clear_journal_template))
                                    }
                                }
                            }
                        }
                    }
                }

                if (notes.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_journal_entries),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    items(notes, key = { it.id }) { note ->
                        JournalEntryCard(note = note, onClick = {
                            navController.navigate(NavRoutes.Edit.createRoute(note.id, note.encrypted))
                        })
                    }
                }
            }
        }
    )
}

@Composable
private fun JournalEntryCard(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = note.name.ifBlank { note.journalDate ?: "" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (note.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.description.take(120),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun TemplateEditorDialog(
    template: Template?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(template?.id) { mutableStateOf(template?.name ?: "") }
    var description by remember(template?.id) { mutableStateOf(template?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    id = if (template == null) R.string.template_add else R.string.template_edit
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.template_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.template_content)) },
                    minLines = 6,
                    maxLines = 12,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), description) }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
