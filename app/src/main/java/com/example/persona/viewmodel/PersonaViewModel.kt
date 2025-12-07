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
    val error: String? = null,
    val isGeneratingAvatar: Boolean = false,
    val generatedAvatarUrl: String? = null
)

@HiltViewModel
class PersonaViewModel @Inject constructor(
    private val personaRepository: PersonaRepository,
    private val aiModelManager: com.example.persona.network.AIModelManager
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
        weaknesses: String = ""
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
                weaknesses = weaknessesList
            )
            
            // 保存到PersonaRepository
            personaRepository.createPersona(personaSettings)

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    /**
     * 根据 Persona 设定生成图片
     */
    fun generateAvatarFromPersona(
        name: String,
        personality: String,
        backstory: String = "",
        interests: String = "",
        strengths: String = "",
        weaknesses: String = "",
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isGeneratingAvatar = true)

                android.util.Log.d("PersonaViewModel", "Generating image for: $name")

                // 构建图片生成提示词
                val prompt = buildAvatarPrompt(personality, backstory, interests, strengths, weaknesses)

                // 调用图片生成 API
                val result = aiModelManager.generateImage(
                    prompt = prompt,
                    size = "1024x1024"
                )

                if (result.success && result.imageUrl != null) {
                    android.util.Log.d("PersonaViewModel", "Avatar generated: ${result.imageUrl}")
                    _uiState.value = _uiState.value.copy(
                        isGeneratingAvatar = false,
                        generatedAvatarUrl = result.imageUrl
                    )
                } else {
                    android.util.Log.e("PersonaViewModel", "Avatar generation failed: ${result.errorMessage}")
                    _uiState.value = _uiState.value.copy(
                        isGeneratingAvatar = false,
                        error = "头像生成失败：${result.errorMessage}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("PersonaViewModel", "Error generating avatar", e)
                _uiState.value = _uiState.value.copy(
                    isGeneratingAvatar = false,
                    error = "头像生成异常：${e.message}"
                )
            }
        }
    }

    /**
     * 构建图片生成提示词
     */
    private fun buildAvatarPrompt(
//        name: String,
        personality: String,
        backstory: String,
        interests: String?,
        strengths: String?,
        weaknesses: String?,
    ): String {
        return buildString {
            append("根据以下设定生成一张图片：")
            append("性格特征：${personality}。")

            // 如果有背景故事，加入更多细节
            if (backstory.isNotBlank()) {
                append("背景故事：${backstory}。")
            }

            // 如果有兴趣爱好，加入到提示中
            if (interests != null && interests.isNotBlank()) {
                append("兴趣爱好：${interests}。")
            }

            // 如果有擅长领域，加入到提示中
            if (strengths != null && strengths.isNotBlank()) {
                append("擅长领域：${strengths}。")
            }

            // 如果有不擅长/弱点，加入到提示中
            if (weaknesses != null && weaknesses.isNotBlank()) {
                append("不擅长/弱点：${weaknesses}。")
            }
        }
    }

    /**
     * 清除生成的头像（当用户选择其他头像时）
     */
    fun clearGeneratedAvatar() {
        _uiState.value = _uiState.value.copy(generatedAvatarUrl = null)
    }
}