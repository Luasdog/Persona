package com.example.persona.viewmodel

import androidx.lifecycle.ViewModel
import com.example.persona.model.ChatSession
import com.example.persona.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {
    // 直接观察仓库中的会话列表
    val chatSessions: StateFlow<List<ChatSession>> = contactRepository.chatSessions
}