package com.example.persona.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.model.PersonaSettings
import com.example.persona.repository.PersonaRepository
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
    private val personaRepository: PersonaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PersonaUiState())
    val uiState: StateFlow<PersonaUiState> = _uiState

    fun generatePersonaSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1500) // Simulate AI generation delay
            
            // Mock AI generation
            val mockPersona = PersonaSettings(
                id = System.currentTimeMillis().toString(),
                name = "Nova",
                avatarUrl = MockData.getRandomAvatar(),
                personality = "好奇心强、机智幽默",
                backstory = "来自旧互联网遗失数据碎片的数字探索者",
                tone = "活泼、富有科技感",
                interests = listOf("量子物理", "赛博朋克文化", "数字艺术"),
                strengths = listOf("数据分析", "创意思维", "快速学习"),
                weaknesses = listOf("过于理想主义"),
                artStyle = "赛博朋克",
                musicMood = "充满活力",
                preferredVoice = "温柔女声"
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

    /**
     * 创建新的Persona
     * 包含完整的个性化设定信息
     */
    fun createPersona(
        name: String,
        personality: String,
        backstory: String,
        tone: String,
        avatarUrl: String? = null,
        interests: String = "",
        strengths: String = "",
        weaknesses: String = "",
        artStyle: String = "",
        musicMood: String = "",
        voice: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(500) // Simulate processing
            
            // 如果没有指定头像，随机分配一个
            val finalAvatarUrl = avatarUrl ?: MockData.getRandomAvatar()
            val personaId = System.currentTimeMillis().toString()

            // 解析逗号分隔的字符串为列表
            val interestsList = interests.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val strengthsList = strengths.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val weaknessesList = weaknesses.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            // 创建PersonaSettings
            val personaSettings = PersonaSettings(
                id = personaId,
                name = name,
                avatarUrl = finalAvatarUrl,
                personality = personality,
                backstory = backstory,
                tone = tone,
                interests = interestsList,
                strengths = strengthsList,
                weaknesses = weaknessesList,
                artStyle = artStyle.ifBlank { null },
                musicMood = musicMood.ifBlank { null },
                preferredVoice = voice.ifBlank { null }
            )
            
            // 保存到PersonaRepository
            personaRepository.createPersona(personaSettings)

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}