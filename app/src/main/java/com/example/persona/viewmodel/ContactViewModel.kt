package com.example.persona.viewmodel

import androidx.lifecycle.ViewModel
import com.example.persona.model.Contact
import com.example.persona.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val personaRepository: com.example.persona.repository.PersonaRepository
) : ViewModel() {

    val contacts: StateFlow<List<Contact>> = contactRepository.contacts

    /**
     * 删除联系人及其相关数据
     */
    fun deleteContact(contactId: String) {
        // 删除Contact和对话记录
        contactRepository.deleteContact(contactId)

        // 同时删除Persona设定
        personaRepository.deletePersona(contactId)
    }
}