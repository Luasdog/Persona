package com.example.persona.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.persona.model.Contact
import com.example.persona.viewmodel.ContactViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactScreen(onContactClick: (String) -> Unit = {}) {
    val viewModel: ContactViewModel = hiltViewModel()
    val contacts by viewModel.contacts.collectAsState()

    var contactToDelete by remember { mutableStateOf<Contact?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "联系人",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        items(contacts, key = { it.id }) { contact ->
            ContactItem(
                contact = contact,
                onClick = { onContactClick(contact.id) },
                onLongClick = {
                    contactToDelete = contact
                    showDeleteDialog = true
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && contactToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除好友") },
            text = { Text("确定要删除 ${contactToDelete?.name} 吗？删除后将清空所有对话记录。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        contactToDelete?.let { contact ->
                            viewModel.deleteContact(contact.id)
                        }
                        showDeleteDialog = false
                        contactToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    contactToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactItem(
    contact: Contact,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = "删除好友"
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            color = if (contact.isPersona) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        ) {
            if (contact.avatarUrl != null) {
                AsyncImage(
                    model = contact.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = if (contact.isPersona) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contact.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        
        if (contact.isPersona) {
             Spacer(modifier = Modifier.width(8.dp))
             Badge(
                 containerColor = MaterialTheme.colorScheme.tertiary,
                 modifier = Modifier.padding(end = 4.dp)
             ) {
                 Text(
                     text = "AI",
                     color = MaterialTheme.colorScheme.onTertiary,
                     style = MaterialTheme.typography.labelSmall
                 )
             }
        }
    }
}