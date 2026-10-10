package com.kin.easynotes.presentation.screens.settings.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kin.easynotes.R
import com.kin.easynotes.domain.model.Template
import com.kin.easynotes.presentation.screens.settings.SettingsScaffold
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
import com.kin.easynotes.presentation.screens.settings.widgets.ActionType
import com.kin.easynotes.presentation.screens.settings.widgets.SettingsBox
import com.kin.easynotes.presentation.screens.settings.settings.shapeManager
import kotlinx.coroutines.launch

@Composable
fun TemplatesScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var templates by remember { mutableStateOf<List<Template>>(emptyList()) }
    var editingTemplate by remember { mutableStateOf<Template?>(null) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<Template?>(null) }

    LaunchedEffect(Unit) {
        settingsViewModel.templateUseCase.getAllTemplates().collect { templates = it }
    }

    if (isDialogVisible) {
        TemplateDialog(
            template = editingTemplate,
            settingsViewModel = settingsViewModel,
            onSave = { name, description ->
                coroutineScope.launch {
                    if (editingTemplate == null) {
                        settingsViewModel.templateUseCase.addTemplate(Template(name = name, description = description))
                    } else {
                        settingsViewModel.templateUseCase.updateTemplate(
                            editingTemplate!!.copy(name = name, description = description)
                        )
                    }
                }
                isDialogVisible = false
                editingTemplate = null
            },
            onDismiss = {
                isDialogVisible = false
                editingTemplate = null
            }
        )
    }

    templateToDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text(stringResource(id = R.string.template_delete_title)) },
            text = { Text(stringResource(id = R.string.template_delete_description, template.name)) },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch { settingsViewModel.templateUseCase.deleteTemplate(template) }
                    templateToDelete = null
                }) {
                    Text(stringResource(id = R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.templates),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.template_add),
                    description = stringResource(id = R.string.template_add_description),
                    icon = Icons.Rounded.Add,
                    actionType = ActionType.CUSTOM,
                    radius = shapeManager(isFirst = true, radius = settingsViewModel.settings.value.cornerRadius),
                    customAction = { onExit ->
                        onExit()
                        editingTemplate = null
                        isDialogVisible = true
                    },
                    customButton = { Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
            items(templates) { template ->
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = template.name,
                    description = template.description.take(60).let {
                        if (template.description.length > 60) "$it…" else it
                    },
                    icon = Icons.Rounded.Description,
                    actionType = ActionType.CUSTOM,
                    radius = shapeManager(isLast = true, radius = settingsViewModel.settings.value.cornerRadius),
                    customAction = { onExit ->
                        onExit()
                        editingTemplate = template
                        isDialogVisible = true
                    },
                    customButton = {
                        Row {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable { templateToDelete = template }
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TemplateDialog(
    template: Template?,
    settingsViewModel: SettingsViewModel,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(template?.name ?: "") }
    var description by remember { mutableStateOf(template?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    id = if (template == null) R.string.template_add else R.string.template_edit
                )
            )
        },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(id = R.string.template_name)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(id = R.string.template_content)) },
                        minLines = 8,
                        maxLines = 14,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), description) }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
