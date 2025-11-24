package com.example.persona.ui.persona

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.persona.viewmodel.PersonaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaCreationScreen(
    onBackClick: () -> Unit,
    onPersonaCreated: () -> Unit
) {
    val viewModel: PersonaViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var personality by remember { mutableStateOf("") }
    var backstory by remember { mutableStateOf("") }
    var tone by remember { mutableStateOf("") } // e.g. Casual, Formal
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    // Observe AI generation
    LaunchedEffect(uiState.generatedSettings) {
        uiState.generatedSettings?.let {
            name = it.name
            personality = it.personality
            backstory = it.backstory
            tone = it.tone
            avatarUrl = it.avatarUrl
            viewModel.consumeGeneratedSettings()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建数字人格") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // AI Generate Button
                    IconButton(onClick = { viewModel.generatePersonaSettings() }) {
                        Icon(Icons.Default.Star, contentDescription = "AI Generate", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "打造你的专属 AI Persona",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            // 头像展示
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名称") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = personality,
                onValueChange = { personality = it },
                label = { Text("性格关键词 (如：乐观、高冷)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tone,
                onValueChange = { tone = it },
                label = { Text("说话风格 (如：正式、幽默)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = backstory,
                onValueChange = { backstory = it },
                label = { Text("背景故事") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Button(
                onClick = {
                    viewModel.createPersona(name, personality, backstory, tone, avatarUrl)
                    onPersonaCreated()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && personality.isNotBlank()
            ) {
                Text("完成创建")
            }
        }
    }
}