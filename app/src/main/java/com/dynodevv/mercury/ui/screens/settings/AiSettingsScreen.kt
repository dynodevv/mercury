package com.dynodevv.mercury.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dynodevv.mercury.R
import com.dynodevv.mercury.domain.model.AiProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val aiConfig by viewModel.aiProviderConfig.collectAsState()
    val aiSearchEnabled by viewModel.aiSearchEnabled.collectAsState()

    var provider by remember { mutableStateOf(aiConfig?.provider ?: "openai") }
    var apiKey by remember { mutableStateOf(aiConfig?.apiKey ?: "") }
    var model by remember { mutableStateOf(aiConfig?.model ?: "gpt-4o-mini") }
    var customEndpoint by remember { mutableStateOf(aiConfig?.customEndpoint ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ai)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.setAiProviderConfig(
                            AiProvider(
                                provider = provider,
                                apiKey = apiKey,
                                model = model,
                                customEndpoint = customEndpoint.takeIf { it.isNotBlank() }
                            )
                        )
                        onNavigateBack()
                    }) {
                        Text(stringResource(R.string.save))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AI Provider",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val providers = listOf("openai", "gemini", "groq", "custom")
                        providers.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { provider = p }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = p.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                if (provider == p) {
                                    Text("✓", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(stringResource(R.string.api_key)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text(stringResource(R.string.model)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (provider == "custom") {
                item {
                    OutlinedTextField(
                        value = customEndpoint,
                        onValueChange = { customEndpoint = it },
                        label = { Text(stringResource(R.string.custom_endpoint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable AI Search",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = aiSearchEnabled,
                        onCheckedChange = { viewModel.setAiSearchEnabled(it) }
                    )
                }
            }
        }
    }
}


