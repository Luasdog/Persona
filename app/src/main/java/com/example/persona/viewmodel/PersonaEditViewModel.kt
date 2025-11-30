package com.example.persona.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.model.PersonaSettings
import com.example.persona.repository.PersonaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonaEditViewModel @Inject constructor(
    private val personaRepository: PersonaRepository,
    private val contactRepository: com.example.persona.repository.ContactRepository
) : ViewModel() {

    data class UiState(
        val persona: PersonaSettings? = null,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val saveSuccess: Boolean = false,
        val errorMessage: String? = null,
        val showDeleteDialog: Boolean = false,

        // 编辑中的字段
        val editedName: String = "",
        val editedPersonality: String = "",
        val editedBackstory: String = "",
        val editedTone: String = "",
        val editedInterests: String = "",
        val editedStrengths: String = "",
        val editedWeaknesses: String = "",
        val editedArtStyle: String = "",
        val editedMusicMood: String = "",
        val editedVoice: String = "",
        val editedGrowthNotes: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 加载Persona数据
     */
    fun loadPersona(personaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                android.util.Log.d("PersonaEditVM", "Loading persona: $personaId")

                // 尝试获取Persona，如果不存在则从Contact创建
                var persona = personaRepository.getPersonaById(personaId)
                if (persona == null) {
                    android.util.Log.d("PersonaEditVM", "Persona not found, checking contact...")
                    // 尝试通过Contact ID获取并创建
                    val contact = contactRepository.getContactById(personaId)
                    if (contact != null && contact.isPersona) {
                        persona = personaRepository.ensurePersonaForContact(contact)
                    }
                }

                if (persona != null) {
                    android.util.Log.d("PersonaEditVM", "Persona loaded successfully: ${persona.name}")
                    _uiState.value = _uiState.value.copy(
                        persona = persona,
                        isLoading = false,
                        // 初始化编辑字段
                        editedName = persona.name,
                        editedPersonality = persona.personality,
                        editedBackstory = persona.backstory,
                        editedTone = persona.tone,
                        editedInterests = persona.interests.joinToString(", "),
                        editedStrengths = persona.strengths.joinToString(", "),
                        editedWeaknesses = persona.weaknesses.joinToString(", "),
                        editedArtStyle = persona.artStyle ?: "",
                        editedMusicMood = persona.musicMood ?: "",
                        editedVoice = persona.preferredVoice ?: "",
                        editedGrowthNotes = persona.growthNotes
                    )
                } else {
                    android.util.Log.e("PersonaEditVM", "Cannot find or create persona for ID: $personaId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "未找到该Persona"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "加载失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 保存Persona设定
     */
    fun savePersona() {
        val currentPersona = _uiState.value.persona ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            try {
                val updatedPersona = currentPersona.copy(
                    name = _uiState.value.editedName.trim(),
                    personality = _uiState.value.editedPersonality.trim(),
                    backstory = _uiState.value.editedBackstory.trim(),
                    tone = _uiState.value.editedTone.trim(),
                    interests = parseCommaSeparated(_uiState.value.editedInterests),
                    strengths = parseCommaSeparated(_uiState.value.editedStrengths),
                    weaknesses = parseCommaSeparated(_uiState.value.editedWeaknesses),
                    artStyle = _uiState.value.editedArtStyle.trim().ifBlank { null },
                    musicMood = _uiState.value.editedMusicMood.trim().ifBlank { null },
                    preferredVoice = _uiState.value.editedVoice.trim().ifBlank { null },
                    growthNotes = _uiState.value.editedGrowthNotes.trim(),
                    lastUpdated = System.currentTimeMillis()
                )

                // 验证必填字段
                if (updatedPersona.name.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "名字不能为空"
                    )
                    return@launch
                }

                if (updatedPersona.personality.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "性格特征不能为空"
                    )
                    return@launch
                }

                // 保存到repository
                personaRepository.updatePersona(updatedPersona)

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    persona = updatedPersona
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "保存失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 删除Persona（同时删除Contact和对话记录）
     */
    fun deletePersona() {
        val currentPersona = _uiState.value.persona ?: return

        viewModelScope.launch {
            try {
                // 删除Persona设定
                personaRepository.deletePersona(currentPersona.id)

                // 同时删除Contact和对话记录
                contactRepository.deleteContact(currentPersona.id)

                _uiState.value = _uiState.value.copy(showDeleteDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "删除失败: ${e.message}",
                    showDeleteDialog = false
                )
            }
        }
    }

    // ===== 更新方法 =====

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(editedName = name)
    }

    fun updatePersonality(personality: String) {
        _uiState.value = _uiState.value.copy(editedPersonality = personality)
    }

    fun updateBackstory(backstory: String) {
        _uiState.value = _uiState.value.copy(editedBackstory = backstory)
    }

    fun updateTone(tone: String) {
        _uiState.value = _uiState.value.copy(editedTone = tone)
    }

    fun updateInterests(interests: String) {
        _uiState.value = _uiState.value.copy(editedInterests = interests)
    }

    fun updateStrengths(strengths: String) {
        _uiState.value = _uiState.value.copy(editedStrengths = strengths)
    }

    fun updateWeaknesses(weaknesses: String) {
        _uiState.value = _uiState.value.copy(editedWeaknesses = weaknesses)
    }

    fun updateArtStyle(artStyle: String) {
        _uiState.value = _uiState.value.copy(editedArtStyle = artStyle)
    }

    fun updateMusicMood(musicMood: String) {
        _uiState.value = _uiState.value.copy(editedMusicMood = musicMood)
    }

    fun updateVoice(voice: String) {
        _uiState.value = _uiState.value.copy(editedVoice = voice)
    }

    fun updateGrowthNotes(notes: String) {
        _uiState.value = _uiState.value.copy(editedGrowthNotes = notes)
    }

    fun showDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    /**
     * 解析逗号分隔的字符串为列表
     */
    private fun parseCommaSeparated(text: String): List<String> {
        return text.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}

