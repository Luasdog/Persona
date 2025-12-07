package com.example.persona.ui.persona

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.persona.viewmodel.PersonaEditViewModel

/**
 * Persona设定编辑界面
 * 支持修改名字、性格、背景等所有设定
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaEditScreen(
    personaId: String,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val viewModel: PersonaEditViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // 加载Persona数据
    LaunchedEffect(personaId) {
        viewModel.loadPersona(personaId)
    }

    // 监听保存成功
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBackClick()
        }
    }

    val persona = uiState.persona

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (persona != null) "编辑 ${persona.name}" else "加载中...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 删除按钮
                    IconButton(
                        onClick = {
                            viewModel.showDeleteConfirmation()
                        }
                    ) {
                        Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                    }

                    // 保存按钮
                    IconButton(
                        onClick = { viewModel.savePersona() },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Check, "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (persona == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PersonaEditForm(
                modifier = Modifier.padding(paddingValues),
                viewModel = viewModel,
                uiState = uiState
            )
        }

        // 删除确认对话框
        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteConfirmation() },
                title = { Text("确认删除") },
                text = { Text("确定要删除 ${persona?.name} 吗？删除后将无法恢复。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePersona()
                            onDeleteClick()
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
fun PersonaEditForm(
    modifier: Modifier = Modifier,
    viewModel: PersonaEditViewModel,
    uiState: PersonaEditViewModel.UiState
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 头像选择
        AvatarSelector(
            modifier = Modifier.fillMaxWidth(),
            currentAvatarUrl = uiState.persona?.avatarUrl,
            onAvatarSelected = { url ->
                viewModel.updateAvatar(url)
            },
            onGenerateAvatar = {
                viewModel.generateAvatar()
            },
            isGenerating = uiState.isGeneratingAvatar
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 基础信息
        Text(
            "基础信息",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // 名字
        OutlinedTextField(
            value = uiState.editedName,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("名字") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 性格特征
        OutlinedTextField(
            value = uiState.editedPersonality,
            onValueChange = { viewModel.updatePersonality(it) },
            label = { Text("性格特征") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例如：热情开朗、理性冷静、幽默风趣") }
        )

        // 背景故事
        OutlinedTextField(
            value = uiState.editedBackstory,
            onValueChange = { viewModel.updateBackstory(it) },
            label = { Text("背景故事") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            placeholder = { Text("描述这个Persona的来历、经历等") }
        )

        // 说话语气
        OutlinedTextField(
            value = uiState.editedTone,
            onValueChange = { viewModel.updateTone(it) },
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

        // 兴趣爱好
        OutlinedTextField(
            value = uiState.editedInterests,
            onValueChange = { viewModel.updateInterests(it) },
            label = { Text("兴趣爱好") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("用逗号分隔，例如：编程,科幻小说,音乐") }
        )

        // 擅长领域
        OutlinedTextField(
            value = uiState.editedStrengths,
            onValueChange = { viewModel.updateStrengths(it) },
            label = { Text("擅长领域") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("用逗号分隔，例如：Python,数据分析,创意写作") }
        )

        // 不擅长的
        OutlinedTextField(
            value = uiState.editedWeaknesses,
            onValueChange = { viewModel.updateWeaknesses(it) },
            label = { Text("不擅长/弱点") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("用逗号分隔，让Persona更真实") }
        )

        Spacer(modifier = Modifier.height(8.dp))


        // 成长记录
        Text(
            "成长记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // 统计信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "成长记录",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // 统计信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("对话次数", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${uiState.persona?.conversationCount ?: 0}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("创建时间", style = MaterialTheme.typography.bodySmall)
                        Text(
                            formatDate(uiState.persona?.createdAt),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                HorizontalDivider()

                // 成长备注
                OutlinedTextField(
                    value = uiState.editedGrowthNotes,
                    onValueChange = { viewModel.updateGrowthNotes(it) },
                    label = { Text("成长备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("记录与这个Persona的互动、发现和成长") }
                )
            }
        }

        // 底部提示
        Text(
            "💡 提示：修改设定后，AI将根据新的人设进行对话",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // 错误提示
        if (uiState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    uiState.errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "未知"
    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
    return date
}

