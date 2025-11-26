package com.example.persona.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.model.Contact
import com.example.persona.model.PersonaSettings
import com.example.persona.repository.ContactRepository
import com.example.persona.utils.MockData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonaUiState(
    val isLoading: Boolean = false,
    val generatedSettings: PersonaSettings? = null,
    val error: String? = null
)

@HiltViewModel
class PersonaViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PersonaUiState())
    val uiState: StateFlow<PersonaUiState> = _uiState

    fun generatePersonaSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1500) // Simulate AI generation delay
            
            // Mock AI generation
            val mockPersona = PersonaSettings(
                name = "Nova",
                avatarUrl = MockData.getRandomAvatar(), // AI 生成时随机分配一个头像
                personality = "Curious, Witty",
                backstory = "A digital explorer originating from the lost data fragments of the old internet.",
                tone = "Playful"
            )
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                generatedSettings = mockPersona
            )
        }
    }
    
    fun consumeGeneratedSettings() {
        _uiState.value = _uiState.value.copy(generatedSettings = null)
    }

    fun createPersona(name: String, personality: String, backstory: String, tone: String, avatarUrl: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(500) // Simulate processing
            
            // 如果没有指定头像，随机分配一个（如果是手动创建的情况）
            val finalAvatarUrl = avatarUrl ?: MockData.getRandomAvatar()
            
            val newContact = Contact(
                id = System.currentTimeMillis().toString(),
                name = name,
                bio = personality, 
                avatarUrl = finalAvatarUrl,
                isPersona = true
            )
            
            contactRepository.addContact(newContact)
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}