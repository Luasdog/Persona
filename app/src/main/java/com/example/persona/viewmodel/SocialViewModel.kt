package com.example.persona.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persona.repository.ContactRepository
import com.example.persona.repository.PersonaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val personaRepository: PersonaRepository
) : ViewModel() {

    // 好友状态映射：authorId -> isFriend
    private val _friendStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val friendStatusMap: StateFlow<Map<String, Boolean>> = _friendStatusMap.asStateFlow()

    // 点赞状态映射：postId -> (isLiked, likeCount)
    private val _likeStatusMap = MutableStateFlow<Map<String, Pair<Boolean, Int>>>(emptyMap())
    val likeStatusMap: StateFlow<Map<String, Pair<Boolean, Int>>> = _likeStatusMap.asStateFlow()

    // 缓存当前的Posts列表，用于自动刷新
    private var cachedPosts: List<com.example.persona.model.Post> = emptyList()

    init {
        // 监听联系人列表变化，自动刷新好友状态
        viewModelScope.launch {
            contactRepository.contacts.collect { contacts ->
                // 当联系人列表变化时，自动刷新所有好友状态
                if (cachedPosts.isNotEmpty()) {
                    val statusMap = mutableMapOf<String, Boolean>()
                    cachedPosts.forEach { post ->
                        // 只根据是否在联系人列表中判断，忽略Mock数据的isFriend字段
                        val isInContacts = contacts.any { it.id == post.authorId }
                        statusMap[post.authorId] = isInContacts
                    }
                    _friendStatusMap.value = statusMap
                }
            }
        }
    }

    /**
     * 初始化好友状态
     * @param posts 当前的Post列表
     */
    fun initializeFriendStatus(posts: List<com.example.persona.model.Post>) {
        // 缓存posts列表
        cachedPosts = posts

        val statusMap = mutableMapOf<String, Boolean>()
        val likeMap = mutableMapOf<String, Pair<Boolean, Int>>()
        posts.forEach { post ->
            // 只根据是否在联系人列表中判断，忽略Mock数据的isFriend字段
            val isInContacts = contactRepository.isContactExists(post.authorId)
            statusMap[post.authorId] = isInContacts

            // 初始化点赞状态
            likeMap[post.id] = Pair(post.isLiked, post.likeCount)
        }
        _friendStatusMap.value = statusMap
        _likeStatusMap.value = likeMap
    }

    /**
     * 刷新特定作者的好友状态
     */
    fun refreshFriendStatus(authorId: String) {
        val isInContacts = contactRepository.isContactExists(authorId)
        _friendStatusMap.value = _friendStatusMap.value + (authorId to isInContacts)
    }

    /**
     * 添加好友
     */
    fun addFriend(
        authorId: String,
        authorName: String,
        authorAvatar: String?,
        authorBio: String = "来自社交广场"
    ) {
        viewModelScope.launch {
            try {
                // 添加到联系人
                contactRepository.addFriendFromPost(
                    authorId = authorId,
                    authorName = authorName,
                    authorAvatar = authorAvatar,
                    authorBio = authorBio
                )

                // 同时确保PersonaRepository中有对应的Persona设定
                val contact = contactRepository.getContactByIdOrNull(authorId)
                if (contact != null && contact.isPersona) {
                    personaRepository.ensurePersonaForContact(contact)
                }

                // 更新状态
                _friendStatusMap.value = _friendStatusMap.value + (authorId to true)
            } catch (e: Exception) {
                android.util.Log.e("SocialViewModel", "添加好友失败", e)
            }
        }
    }

    /**
     * 检查是否为好友
     */
    fun isFriend(authorId: String): Boolean {
        return _friendStatusMap.value[authorId] ?: false
    }

    /**
     * 切换点赞状态
     * @param postId 帖子ID
     */
    fun toggleLike(postId: String) {
        val currentStatus = _likeStatusMap.value[postId] ?: return
        val (isLiked, likeCount) = currentStatus

        // 切换点赞状态并更新点赞数
        val newIsLiked = !isLiked
        val newLikeCount = if (newIsLiked) likeCount + 1 else likeCount - 1

        _likeStatusMap.value = _likeStatusMap.value + (postId to Pair(newIsLiked, newLikeCount))
    }
}

