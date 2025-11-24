package com.example.persona.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.persona.model.Contact
import com.example.persona.utils.MockData

@Composable
fun ContactScreen() {
    // Contact list only, FAB removed as it is now in MainScreen or can be kept here if we want "Add Contact" specifically
    // But the requirement was to separate social plaza and keep contacts clean.
    // The "Add Persona" FAB is now on MainScreen when on Contact tab.
    
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
        
        items(MockData.contacts) { contact ->
            ContactItem(contact = contact, onClick = { /* TODO: View Detail */ })
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            color = if (contact.isPersona) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = if (contact.isPersona) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contact.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (contact.isPersona) {
             Spacer(modifier = Modifier.width(8.dp))
             Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                 Text("AI", color = MaterialTheme.colorScheme.onTertiary)
             }
        }
    }
}