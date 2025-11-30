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
    var tone by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf("") }
    var strengths by remember { mutableStateOf("") }
    var weaknesses by remember { mutableStateOf("") }
    var artStyle by remember { mutableStateOf("") }
    var musicMood by remember { mutableStateOf("") }
    var voice by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    // Observe AI generation
    LaunchedEffect(uiState.generatedSettings) {
        uiState.generatedSettings?.let {
            name = it.name
            personality = it.personality
            backstory = it.backstory
            tone = it.tone
            interests = it.interests.joinToString(", ")
            strengths = it.strengths.joinToString(", ")
            weaknesses = it.weaknesses.joinToString(", ")
            artStyle = it.artStyle ?: ""
            musicMood = it.musicMood ?: ""
            voice = it.preferredVoice ?: ""
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

            // 基础信息
            Text(
                "基础信息",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名字") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = personality,
                onValueChange = { personality = it },
                label = { Text("性格特征") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如：热情开朗、理性冷静、幽默风趣") }
            )

            OutlinedTextField(
                value = backstory,
                onValueChange = { backstory = it },
                label = { Text("背景故事") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                placeholder = { Text("描述这个Persona的来历、经历等") }
            )

            OutlinedTextField(
                value = tone,
                onValueChange = { tone = it },
                label = { Text("说话语气") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如：正式、随意、文艺、科技感") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 兴趣与特长
            Text(
                "兴趣与特长",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = interests,
                onValueChange = { interests = it },
                label = { Text("兴趣爱好") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("用逗号分隔，例如：编程,科幻小说,音乐") }
            )

            OutlinedTextField(
                value = strengths,
                onValueChange = { strengths = it },
                label = { Text("擅长领域") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("用逗号分隔，例如：Python,数据分析,创意写作") }
            )

            OutlinedTextField(
                value = weaknesses,
                onValueChange = { weaknesses = it },
                label = { Text("不擅长/弱点") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("用逗号分隔，让Persona更真实") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AI偏好设置
            Text(
                "AI能力偏好",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "配置这个Persona的专属AI能力",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = artStyle,
                onValueChange = { artStyle = it },
                label = { Text("艺术风格（图片生成）") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如：动漫、赛博朋克、水彩画") }
            )

            OutlinedTextField(
                value = musicMood,
                onValueChange = { musicMood = it },
                label = { Text("音乐情绪（音乐生成）") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如：平静、充满活力、忧郁") }
            )

            OutlinedTextField(
                value = voice,
                onValueChange = { voice = it },
                label = { Text("语音类型（语音合成）") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如：温柔女声、低沉男声") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 提示信息
            Text(
                "💡 提示：修改设定后，AI将根据新的人设进行对话",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Button(
                onClick = {
                    viewModel.createPersona(
                        name,
                        personality,
                        backstory,
                        tone,
                        avatarUrl,
                        interests,
                        strengths,
                        weaknesses,
                        artStyle,
                        musicMood,
                        voice
                    )
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