package com.example.persona.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.persona.model.Post
import com.example.persona.utils.MockData

@Composable
fun SocialScreen() {
    val viewModel: com.example.persona.viewmodel.SocialViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val friendStatusMap by viewModel.friendStatusMap.collectAsState()
    val likeStatusMap by viewModel.likeStatusMap.collectAsState()

    // 初始化好友状态（每次重组时都会检查并刷新）
    LaunchedEffect(Unit) {
        viewModel.initializeFriendStatus(MockData.posts)
    }


    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "社交广场", 
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        items(MockData.posts) { post ->
            val isFriend = friendStatusMap[post.authorId] ?: post.isFriend
            val likeStatus = likeStatusMap[post.id]
            val isLiked = likeStatus?.first ?: post.isLiked
            val likeCount = likeStatus?.second ?: post.likeCount

            SocialPostItem(
                post = post,
                isFriend = isFriend,
                isLiked = isLiked,
                likeCount = likeCount,
                onAddFriend = {
                    // 从Post内容中提取简短的简介，去除换行和表情符号
                    val bio = post.content
                        .replace("\n", " ")  // 替换换行为空格
                        .replace(Regex("[#@]\\w+"), "")  // 移除话题标签
                        .trim()
                        .take(30)  // 限制长度
                        .let { if (it.length >= 30) "$it..." else it }  // 添加省略号
                        .ifBlank { "来自社交广场的AI好友" }  // 如果为空则使用默认值

                    viewModel.addFriend(
                        authorId = post.authorId,
                        authorName = post.authorName,
                        authorAvatar = post.authorAvatar,
                        authorBio = bio
                    )
                },
                onLikeClick = {
                    viewModel.toggleLike(post.id)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
fun SocialPostItem(
    post: Post,
    isFriend: Boolean = post.isFriend,
    isLiked: Boolean = post.isLiked,
    likeCount: Int = post.likeCount,
    onAddFriend: () -> Unit = {},
    onLikeClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 顶部：头像+名字+时间+添加好友按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 头像
                Surface(
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (post.authorAvatar != null) {
                        AsyncImage(
                            model = post.authorAvatar,
                            contentDescription = "Author Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 名字和时间
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = post.authorName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = post.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 添加好友按钮（仅对非好友显示）
                if (!isFriend) {
                    Button(
                        onClick = {
                            onAddFriend()
                            android.widget.Toast.makeText(
                                context,
                                "已添加 ${post.authorName} 为好友",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加好友",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加好友", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    // 已是好友，显示已添加标识
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "已添加",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "已添加",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
            
            if (post.imageUrl != null) {
                // Image place holder
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("图片内容")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onLikeClick)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = likeCount.toString(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}