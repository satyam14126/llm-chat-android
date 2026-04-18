package com.llmchat.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.llmchat.app.domain.model.ProviderProfile
import com.llmchat.app.ui.common.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    var showAddProfile by remember { mutableStateOf(false) }
    var editProfile by remember { mutableStateOf<ProviderProfile?>(null) }

    if (showAddProfile || editProfile != null) {
        ProviderProfileDialog(
            profile = editProfile,
            onDismiss = { showAddProfile = false; editProfile = null },
            onSave = { profile ->
                if (editProfile != null) viewModel.updateProfile(profile)
                else viewModel.addProfile(profile)
                showAddProfile = false
                editProfile = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { SectionHeader("Appearance") }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DarkMode, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Dark Mode", modifier = Modifier.weight(1f))
                        Switch(
                            checked = darkMode ?: false,
                            onCheckedChange = { viewModel.setDarkMode(it) }
                        )
                    }
                }
            }

            item { SectionHeader("Provider Profiles") }
            item {
                Button(
                    onClick = { showAddProfile = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Provider Profile")
                }
            }

            items(profiles, key = { it.id }) { profile ->
                ProviderProfileCard(
                    profile = profile,
                    onEdit = { editProfile = profile },
                    onDelete = { viewModel.deleteProfile(profile.id) },
                    onSetDefault = { viewModel.setDefaultProfile(profile.id) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ProviderProfileCard(
    profile: ProviderProfile,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Profile") },
            text = { Text("Delete \"${profile.name}\"?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.isDefault)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(profile.name, style = MaterialTheme.typography.titleSmall)
                        if (profile.isDefault) {
                            Spacer(Modifier.width(8.dp))
                            Badge { Text("Default") }
                        }
                    }
                    Text(
                        profile.baseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Model: ${profile.model}  •  Temp: ${profile.temperature}  •  Max: ${profile.maxTokens} tokens",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (profile.streamingEnabled) {
                        Text(
                            "Streaming enabled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (!profile.isDefault) {
                    TextButton(onClick = onSetDefault) { Text("Set Default") }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", Modifier.size(20.dp))
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "Delete", Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ProviderProfileDialog(
    profile: ProviderProfile?,
    onDismiss: () -> Unit,
    onSave: (ProviderProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var baseUrl by remember { mutableStateOf(profile?.baseUrl ?: "https://api.openai.com") }
    var apiKey by remember { mutableStateOf(profile?.apiKey ?: "") }
    var model by remember { mutableStateOf(profile?.model ?: "gpt-4o-mini") }
    var temperature by remember { mutableStateOf(profile?.temperature ?: 0.7f) }
    var maxTokens by remember { mutableStateOf((profile?.maxTokens ?: 4096).toString()) }
    var streamingEnabled by remember { mutableStateOf(profile?.streamingEnabled ?: true) }
    var systemPrompt by remember { mutableStateOf(profile?.systemPrompt ?: "") }
    var apiKeyVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (profile == null) "Add Provider" else "Edit Provider") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Profile Name") },
                        placeholder = { Text("e.g. OpenAI, Local Ollama") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL") },
                        placeholder = { Text("https://api.openai.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        placeholder = { Text("sk-...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (apiKeyVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                Icon(
                                    if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null
                                )
                            }
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        placeholder = { Text("gpt-4o-mini, claude-3-haiku, etc.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Column {
                        Text(
                            "Temperature: ${"%.1f".format(temperature)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = temperature,
                            onValueChange = { temperature = it },
                            valueRange = 0f..2f,
                            steps = 19
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = maxTokens,
                        onValueChange = { maxTokens = it.filter { c -> c.isDigit() } },
                        label = { Text("Max Tokens") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Streaming", modifier = Modifier.weight(1f))
                        Switch(checked = streamingEnabled, onCheckedChange = { streamingEnabled = it })
                    }
                }
                item {
                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it },
                        label = { Text("System Prompt (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp),
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && baseUrl.isNotBlank()) {
                        onSave(
                            ProviderProfile(
                                id = profile?.id ?: 0,
                                name = name.trim(),
                                baseUrl = baseUrl.trim(),
                                apiKey = apiKey.trim(),
                                model = model.trim().ifBlank { "gpt-4o-mini" },
                                temperature = temperature,
                                maxTokens = maxTokens.toIntOrNull() ?: 4096,
                                streamingEnabled = streamingEnabled,
                                systemPrompt = systemPrompt.trim(),
                                isDefault = profile?.isDefault ?: false
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && baseUrl.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
