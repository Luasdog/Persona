package com.example.persona.viewmodel

import androidx.lifecycle.ViewModel
import com.example.persona.model.Contact
import com.example.persona.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    val contacts: StateFlow<List<Contact>> = contactRepository.contacts
}