package com.example.persona.ui.persona

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.persona.utils.MockData

/**
 * 头像选择组件
 * 支持：本地上传、默认头像库、AI生成
 */
@Composable
fun AvatarSelector(
    modifier: Modifier = Modifier,
    currentAvatarUrl: String?,
    onAvatarSelected: (String) -> Unit,
    onGenerateAvatar: () -> Unit,
    isGenerating: Boolean = false
) {
    var showAvatarDialog by remember { mutableStateOf(false) }

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 将 URI 转换为字符串用于显示
            onAvatarSelected(it.toString())
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 头像显示
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable { showAvatarDialog = true }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shape = CircleShape
            ) {
                if (currentAvatarUrl != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = currentAvatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = 1.15f  // 放大 15% 以裁掉可能的边框
                                    scaleY = 1.15f
                                }
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Default Avatar",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 操作按钮行
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选择头像按钮
            OutlinedButton(
                onClick = { showAvatarDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("选择头像")
            }

            // AI 生成按钮
            Button(
                onClick = onGenerateAvatar,
                enabled = !isGenerating,
                modifier = Modifier.weight(1f)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI生成")
            }
        }
    }

    // 头像选择对话框
    if (showAvatarDialog) {
        AvatarSelectionDialog(
            currentAvatarUrl = currentAvatarUrl,
            onDismiss = { showAvatarDialog = false },
            onAvatarSelected = { url ->
                onAvatarSelected(url)
                showAvatarDialog = false
            },
            onUploadClick = {
                showAvatarDialog = false
                imagePickerLauncher.launch("image/*")
            }
        )
    }
}

/**
 * 头像选择对话框
 */
@Composable
fun AvatarSelectionDialog(
    currentAvatarUrl: String?,
    onDismiss: () -> Unit,
    onAvatarSelected: (String) -> Unit,
    onUploadClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择头像",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 上传本地图片按钮
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onUploadClick)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "从本地上传",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "选择一张图片作为头像",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 默认头像库标题
                Text(
                    text = "默认头像库",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 默认头像网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(MockData.avatarList) { avatarUrl ->
                        AvatarOption(
                            avatarUrl = avatarUrl,
                            isSelected = avatarUrl == currentAvatarUrl,
                            onClick = { onAvatarSelected(avatarUrl) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单个头像选项
 */
@Composable
fun AvatarOption(
    avatarUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            border = if (isSelected) {
                BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar Option",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 1.15f  // 放大 15% 以裁掉边框
                            scaleY = 1.15f
                        }
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 选中标记
        if (isSelected) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier
                        .padding(2.dp)
                        .fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

