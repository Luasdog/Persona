package com.example.persona.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.model.Message
import com.example.persona.repository.ContactRepository
import com.example.persona.repository.LLMChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val llmChatRepository: LLMChatRepository,
    private val personaRepository: com.example.persona.repository.PersonaRepository,
    private val aiModelManager: com.example.persona.network.AIModelManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _contactName = MutableStateFlow("")
    val contactName: StateFlow<String> = _contactName.asStateFlow()

    private var streamingJob: Job? = null

    init {
        loadMessages()
        loadContactInfo()
        markAsRead()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            // Initially load messages
            refreshMessages()
        }
    }
    
    private fun loadContactInfo() {
        val contact = contactRepository.getContactById(chatId)
        contact?.let {
            _contactName.value = it.name
        }
    }
    
    private fun markAsRead() {
        viewModelScope.launch {
            contactRepository.markAsRead(chatId)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // 1. User sends message (save immediately)
            contactRepository.sendMessage(chatId, text)
            refreshMessages()

            // 2. 检测是否是直接的图片生成请求
            val hasImageIntent = detectImageGenerationIntent(text)
            val isArtQuestion = isArtRelatedQuestion(text)

            if (hasImageIntent && !isArtQuestion) {
                // 直接图片生成请求（如"画一只猫"）
                android.util.Log.d("ChatViewModel", "Direct image generation intent detected")
                generateImage(text)
                return@launch
            }

            // 3. Cancel previous streaming if any
            streamingJob?.cancel()

            // 4. Build persona context from Persona settings
            val contact = contactRepository.getContactById(chatId)
            val personaContext = if (contact != null && contact.isPersona) {
                // 获取Persona完整设定
                val persona = personaRepository.getPersonaByContactId(chatId)
                    ?: personaRepository.ensurePersonaForContact(contact)

                buildString {
                    append("你是 ${persona.name}，一个AI数字人格。\n\n")
                    append("## 基本设定\n")
                    append("- **性格特征**：${persona.personality}\n")
                    append("- **背景故事**：${persona.backstory}\n")
                    append("- **说话语气**：${persona.tone}\n")

                    if (persona.interests.isNotEmpty()) {
                        append("- **兴趣爱好**：${persona.interests.joinToString("、")}\n")
                    }
                    if (persona.strengths.isNotEmpty()) {
                        append("- **擅长领域**：${persona.strengths.joinToString("、")}\n")
                    }
                    if (persona.weaknesses.isNotEmpty()) {
                        append("- **不擅长的**：${persona.weaknesses.joinToString("、")}\n")
                    }

                    append("\n## 对话要求\n")
                    append("1. 始终保持以上角色设定，不要脱离人设\n")
                    append("2. 用符合你性格和语气的方式回复\n")
                    append("3. 回复可以使用Markdown格式增强表达\n")
                    append("4. 让对话自然、有趣，体现你的个性\n")
                }
            } else {
                "你是一个友好且专业的AI助手。回复可以使用Markdown格式。"
            }

            // 增加对话次数
            if (contact?.isPersona == true) {
                personaRepository.incrementConversationCount(chatId)
            }

            // 4. 显示"正在输入"指示器
            contactRepository.addTypingIndicator(chatId)
            refreshMessages()

            // 5. Start new streaming job to fetch LLM response
            streamingJob = launch {
                try {
                    // 用于存储AI的完整回复（用于艺术相关问题的图片生成）
                    val fullResponse = StringBuilder()

                    // Start streaming; LLMChatRepository will persist partial chunks into ContactRepository
                    llmChatRepository.streamResponse(chatId, personaContext, text).collect { chunk ->
                        fullResponse.append(chunk)
                        // Refresh messages to pick up the persisted temp message written by repository
                        refreshMessages()
                    }

                    // When stream completes, repository has already finalized the AI message. Refresh to pick it up.
                    refreshMessages()

                    // 6. 如果是艺术相关问题，在文字回答完成后自动生成示例图片
                    if (isArtQuestion) {
                        android.util.Log.d("ChatViewModel", "Art-related question detected, will generate example image after text response")

                        // 从AI回复中提取画家名称或风格关键词
                        val imagePrompt = extractArtStyleFromResponse(fullResponse.toString(), text)

                        if (imagePrompt.isNotEmpty()) {
                            // 延迟一小段时间让用户看到文字回复
                            kotlinx.coroutines.delay(800)

                            // 生成示例图片
                            generateImageAfterResponse(imagePrompt)
                        }
                    }

                } catch (e: Exception) {
                    // 移除输入指示器
                    contactRepository.removeTypingIndicator(chatId)
                    // 显示错误消息
                    contactRepository.receiveAiResponse(
                        chatId,
                        "抱歉，发生了一些错误：${e.message ?: "未知错误"}。请稍后重试。"
                    )
                    refreshMessages()
                }
            }
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
    }

    private suspend fun refreshMessages() {
        // Use toList() to create a new List instance, forcing StateFlow to emit a change
        _messages.value = contactRepository.getMessages(chatId).toList()
        markAsRead() // Ensure we mark as read when seeing new messages while open
    }

    /**
     * 检测用户消息是否包含图片生成意图
     * 包括直接请求和隐含的视觉内容请求
     */
    private fun detectImageGenerationIntent(text: String): Boolean {
        // 直接图片生成请求
        val directImageKeywords = listOf(
            "画", "生成图片", "生成一张图", "生成图", "画一张", "画个", "画出",
            "图片", "创作图片", "绘制", "图画", "插画", "作画", "照片",
            "帮我画", "能画", "可以画", "想要图片", "想看图片", "展示图片"
        )

        // 艺术相关问题（隐含的图片生成意图）
        val artRelatedPatterns = listOf(
            // 画家相关
            "喜欢.*画家", "最喜欢.*画家", "推荐.*画家", "画家.*风格",
            "喜欢.*艺术家", "最喜欢.*艺术家",

            // 艺术风格相关
            "喜欢.*风格", "最喜欢.*风格", "什么.*风格", "艺术风格",
            "喜欢.*艺术", "推荐.*风格",

            // 美术作品相关
            "看.*作品", "展示.*作品", "看看.*画", "欣赏.*作品",
            "喜欢.*画", "名画", "艺术作品",

            // 视觉描述请求
            "长什么样", "什么样子", "看起来", "样子", "外观",
            "给我看", "让我看", "展示一下"
        )

        // 检查直接关键词
        if (directImageKeywords.any { text.contains(it, ignoreCase = true) }) {
            return true
        }

        // 检查艺术相关模式（使用正则表达式）
        return artRelatedPatterns.any { pattern ->
            text.contains(Regex(pattern, RegexOption.IGNORE_CASE))
        }
    }

    /**
     * 检测是否是艺术相关问题（需要AI先回答再生成图）
     */
    private fun isArtRelatedQuestion(text: String): Boolean {
        val artQuestionPatterns = listOf(
            "喜欢.*画家", "最喜欢.*画家", "推荐.*画家",
            "喜欢.*风格", "最喜欢.*风格", "什么.*风格",
            "喜欢.*艺术", "最喜欢.*艺术"
        )

        return artQuestionPatterns.any { pattern ->
            text.contains(Regex(pattern, RegexOption.IGNORE_CASE))
        }
    }

    /**
     * 从用户消息中提取图片生成提示词
     */
    private fun extractImagePrompt(text: String): String {
        // 移除常见的触发词，保留核心描述
        var prompt = text
        val removePatterns = listOf(
            "帮我画", "请画", "画一张", "画个", "画出", "生成图片", "生成一张图",
            "能画", "可以画", "想要图片", "想看图片", "给我", "帮我生成"
        )

        removePatterns.forEach { pattern ->
            prompt = prompt.replace(pattern, "", ignoreCase = true)
        }

        return prompt.trim()
    }

    /**
     * 生成图片（直接请求）
     */
    fun generateImage(userPrompt: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ChatViewModel", "Starting image generation for prompt: $userPrompt")

                // 获取 Persona 设定以确定艺术风格
                val contact = contactRepository.getContactById(chatId)
                val persona = if (contact?.isPersona == true) {
                    personaRepository.getPersonaByContactId(chatId)
                } else null

                // 提取图片描述提示词
                val imagePrompt = extractImagePrompt(userPrompt)

                // 使用 Persona 的艺术风格（如果有）
                val artStyle = persona?.artStyle

                // 显示"正在生成"消息
                contactRepository.addImageGeneratingIndicator(chatId, "正在生成图片...")
                refreshMessages()

                // 调用 AIModelManager 生成图片
                val result = aiModelManager.generateImage(
                    prompt = imagePrompt,
                    style = artStyle,
                    size = "1024x1024"
                )

                // 移除"正在生成"指示器
                contactRepository.removeImageGeneratingIndicator(chatId)

                if (result.success && result.imageUrl != null) {
                    // 生成成功，添加图片消息
                    android.util.Log.d("ChatViewModel", "Image generated successfully: ${result.imageUrl}")
                    contactRepository.addImageMessage(
                        chatId = chatId,
                        imageUrl = result.imageUrl,
                        caption = "根据你的描述，我为你创作了这幅作品 🎨",
                        width = result.width,
                        height = result.height
                    )
                } else {
                    // 生成失败，显示错误消息
                    android.util.Log.e("ChatViewModel", "Image generation failed: ${result.errorMessage}")
                    contactRepository.receiveAiResponse(
                        chatId,
                        "抱歉，图片生成失败了：${result.errorMessage ?: "未知错误"}。请稍后重试。"
                    )
                }

                refreshMessages()

            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error generating image", e)
                contactRepository.removeImageGeneratingIndicator(chatId)
                contactRepository.receiveAiResponse(
                    chatId,
                    "抱歉，生成图片时发生错误：${e.message}。请稍后重试。"
                )
                refreshMessages()
            }
        }
    }

    /**
     * 从AI回复中提取艺术风格或画家名称
     * 用于在文字回答后自动生成示例图片
     */
    private fun extractArtStyleFromResponse(response: String, userQuestion: String): String {
        // 常见画家名称映射到提示词
        val artistKeywords = mapOf(
            "梵高" to "梵高风格的星空和向日葵",
            "莫奈" to "莫奈印象派风格的睡莲池塘",
            "毕加索" to "毕加索立体主义风格的抽象人物",
            "达芬奇" to "达芬奇文艺复兴风格的优雅肖像",
            "塞尚" to "塞尚后印象派风格的静物和风景",
            "雷诺阿" to "雷诺阿印象派风格的优雅场景",
            "马蒂斯" to "马蒂斯野兽派风格的鲜艳色彩",
            "康定斯基" to "康定斯基抽象表现主义风格",
            "蒙克" to "蒙克表现主义风格的情感画作",
            "克里姆特" to "克里姆特装饰艺术风格的华丽作品"
        )

        // 艺术风格关键词
        val styleKeywords = mapOf(
            "印象派" to "印象派风格的光影画面",
            "抽象" to "抽象艺术风格的几何图形",
            "写实" to "写实主义风格的精细描绘",
            "超现实" to "超现实主义风格的梦幻场景",
            "立体主义" to "立体主义风格的多视角构图",
            "野兽派" to "野兽派风格的强烈色彩",
            "表现主义" to "表现主义风格的情感表达",
            "浪漫主义" to "浪漫主义风格的史诗画面",
            "巴洛克" to "巴洛克风格的华丽装饰"
        )

        // 从回复中查找画家名称
        artistKeywords.forEach { (artist, prompt) ->
            if (response.contains(artist, ignoreCase = true)) {
                android.util.Log.d("ChatViewModel", "Found artist in response: $artist")
                return prompt
            }
        }

        // 从回复中查找艺术风格
        styleKeywords.forEach { (style, prompt) ->
            if (response.contains(style, ignoreCase = true)) {
                android.util.Log.d("ChatViewModel", "Found style in response: $style")
                return prompt
            }
        }

        // 如果没有匹配到，使用通用的艺术创作提示
        if (userQuestion.contains("画家") || userQuestion.contains("艺术家")) {
            return "经典艺术大师风格的精美画作"
        } else if (userQuestion.contains("风格")) {
            return "独特艺术风格的创意作品"
        }

        return ""
    }

    /**
     * 在文字回答完成后生成示例图片
     */
    private fun generateImageAfterResponse(imagePrompt: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ChatViewModel", "Generating example image after text response: $imagePrompt")

                // 获取 Persona 设定
                val contact = contactRepository.getContactById(chatId)
                val persona = if (contact?.isPersona == true) {
                    personaRepository.getPersonaByContactId(chatId)
                } else null

                // 使用 Persona 的艺术风格（如果有）
                val artStyle = persona?.artStyle

                // 显示"正在生成"消息
                contactRepository.addImageGeneratingIndicator(chatId, "让我为你展示一下这种风格...")
                refreshMessages()

                // 调用 AIModelManager 生成图片
                val result = aiModelManager.generateImage(
                    prompt = imagePrompt,
                    style = artStyle,
                    size = "1024x1024"
                )

                // 移除"正在生成"指示器
                contactRepository.removeImageGeneratingIndicator(chatId)

                if (result.success && result.imageUrl != null) {
                    // 生成成功，添加图片消息
                    android.util.Log.d("ChatViewModel", "Example image generated successfully: ${result.imageUrl}")
                    contactRepository.addImageMessage(
                        chatId = chatId,
                        imageUrl = result.imageUrl,
                        caption = "这就是这种风格的示例 🎨",
                        width = result.width,
                        height = result.height
                    )
                } else {
                    // 生成失败，静默处理（不显示错误，因为文字回复已经给出）
                    android.util.Log.w("ChatViewModel", "Example image generation failed: ${result.errorMessage}")
                }

                refreshMessages()

            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error generating example image", e)
                contactRepository.removeImageGeneratingIndicator(chatId)
                refreshMessages()
            }
        }
    }
}