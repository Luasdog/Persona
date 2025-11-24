package com.example.persona.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.model.Contact
import com.example.persona.model.PersonaSettings
import com.example.persona.repository.ContactRepository
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

    fun createPersona(name: String, personality: String, backstory: String, tone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(500) // Simulate processing
            
            val newContact = Contact(
                id = System.currentTimeMillis().toString(),
                name = name,
                bio = personality, // 简单映射
                isPersona = true
            )
            
            contactRepository.addContact(newContact)
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}