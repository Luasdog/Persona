package com.example.persona.repository

import com.example.persona.model.Contact
import com.example.persona.model.PersonaSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persona数据仓库
 * 管理所有Persona的设定和数据
 */
@Singleton
class PersonaRepository @Inject constructor(
    private val contactRepository: ContactRepository
) {

    // Persona设定映射：ID -> PersonaSettings
    private val _personas = MutableStateFlow<Map<String, PersonaSettings>>(emptyMap())
    val personas: StateFlow<Map<String, PersonaSettings>> = _personas.asStateFlow()

    init {
        // 为所有现有的AI Contact创建默认Persona设定
        initializeDefaultPersonas()
    }

    /**
     * 初始化默认Persona设定
     */
    private fun initializeDefaultPersonas() {
        val contacts = contactRepository.contacts.value
        val defaultPersonas = mutableMapOf<String, PersonaSettings>()

        contacts.filter { it.isPersona }.forEach { contact ->
            defaultPersonas[contact.id] = PersonaSettings(
                id = contact.id,
                name = contact.name,
                personality = contact.bio,
                backstory = "一个神秘的AI助手，等待与你一起成长",
                tone = "友好、专业",
                avatarUrl = contact.avatarUrl,
                interests = emptyList(),
                strengths = emptyList(),
                weaknesses = emptyList()
            )
        }

        _personas.value = defaultPersonas
    }

    /**
     * 根据ID获取Persona设定
     */
    fun getPersonaById(id: String): PersonaSettings? {
        return _personas.value[id]
    }

    /**
     * 根据Contact ID获取Persona设定
     */
    fun getPersonaByContactId(contactId: String): PersonaSettings? {
        return _personas.value[contactId]
    }

    /**
     * 创建新的Persona
     */
    fun createPersona(settings: PersonaSettings) {
        _personas.update { current ->
            current + (settings.id to settings)
        }

        // 同时创建Contact
        val contact = Contact(
            id = settings.id,
            name = settings.name,
            bio = settings.personality,
            avatarUrl = settings.avatarUrl,
            isPersona = true
        )
        contactRepository.addContact(contact)
    }

    /**
     * 更新Persona设定
     */
    fun updatePersona(settings: PersonaSettings) {
        _personas.update { current ->
            current + (settings.id to settings)
        }

        // 同步更新Contact信息
        val contact = contactRepository.getContactById(settings.id)
        if (contact != null) {
            val updatedContact = contact.copy(
                name = settings.name,
                bio = settings.personality,
                avatarUrl = settings.avatarUrl
            )
            contactRepository.updateContact(updatedContact)
        }
    }

    /**
     * 删除Persona
     */
    fun deletePersona(id: String) {
        _personas.update { current ->
            current - id
        }

        // 同时删除Contact和对话记录
        contactRepository.deleteContact(id)
    }

    /**
     * 增加对话次数
     */
    fun incrementConversationCount(id: String) {
        _personas.update { current ->
            val persona = current[id]
            if (persona != null) {
                current + (id to persona.copy(
                    conversationCount = persona.conversationCount + 1,
                    lastUpdated = System.currentTimeMillis()
                ))
            } else {
                current
            }
        }
    }

    /**
     * 获取所有Persona
     */
    fun getAllPersonas(): List<PersonaSettings> {
        return _personas.value.values.toList()
    }

    /**
     * 根据Contact创建Persona设定（如果不存在）
     */
    fun ensurePersonaForContact(contact: Contact): PersonaSettings {
        val existing = getPersonaByContactId(contact.id)
        if (existing != null) return existing

        // 创建默认Persona设定
        val newPersona = PersonaSettings(
            id = contact.id,
            name = contact.name,
            personality = contact.bio,
            backstory = "一个神秘的AI助手",
            tone = "友好、专业",
            avatarUrl = contact.avatarUrl
        )

        _personas.update { current ->
            current + (newPersona.id to newPersona)
        }

        return newPersona
    }
}

