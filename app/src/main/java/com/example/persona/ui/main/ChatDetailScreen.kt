package com.example.persona.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.persona.model.Message
import com.example.persona.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    onBackClick: () -> Unit,
    onEditPersona: (String) -> Unit = {}
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val messages by viewModel.messages.collectAsState()
    val contactName by viewModel.contactName.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    // 剪贴板管理器
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // 复制消息到剪贴板
    val copyToClipboard: (String) -> Unit = { text ->
        clipboardManager.setText(AnnotatedString(text))
        // 可以添加Toast提示
        android.widget.Toast.makeText(context, "已复制到剪贴板", android.widget.Toast.LENGTH_SHORT).show()
    }

    // 复制所有消息
    val copyAllMessages: () -> Unit = {
        val allText = messages
            .filter { !it.isTyping && it.id != "_ai_temp" }
            .joinToString("\n\n") { msg ->
                val sender = if (msg.isFromUser) "我" else contactName
                "$sender: ${msg.text}"
            }
        copyToClipboard(allText)
    }

    // List state to control scrolling
    val listState = rememberLazyListState()

    // Auto scroll to bottom when last message content changes (covers incremental updates)
    LaunchedEffect(messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            // small delay can help layout settle when content height grows
            // (tunable: remove or adjust if not desired)
            delay(50)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contactName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // 更多选项按钮
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }

                    // 下拉菜单
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("⚙️ 编辑Persona设定") },
                            onClick = {
                                onEditPersona(chatId)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📋 复制全部对话") },
                            onClick = {
                                copyAllMessages()
                                showMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Message List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState,
                reverseLayout = false,
                contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp)
            ) {
                items(messages) { message ->
                    MessageItem(
                        message = message,
                        onCopyMessage = copyToClipboard
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send, 
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: Message,
    onCopyMessage: (String) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        // 如果是"正在输入"状态
        if (message.isTyping) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 16.dp
                ),
                modifier = Modifier.widthIn(min = 80.dp, max = 280.dp)
            ) {
                com.example.persona.ui.components.TypingIndicator(
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            // 普通消息
            Box {
                Surface(
                    color = if (message.isFromUser) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                    ),
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .combinedClickable(
                            onClick = { },
                            onLongClick = { showMenu = true },
                            onClickLabel = "查看消息",
                            onLongClickLabel = "复制消息"
                        )
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        if (message.isMarkdown && !message.isFromUser) {
                            // AI消息使用Markdown渲染
                            val isStreaming = message.id == "_ai_temp"
                            com.example.persona.ui.components.StreamingMarkdownText(
                                text = message.text,
                                isComplete = !isStreaming,
                                isFromUser = false
                            )
                        } else {
                            // 用户消息使用普通文本
                            Text(
                                text = message.text,
                                color = if (message.isFromUser) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // 长按菜单
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("📄 复制") },
                        onClick = {
                            onCopyMessage(message.text)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}