package com.example.persona.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class User(
    val id: String,
    val username: String,
    val email: String,
    val avatar: String? = null,
    val createdAt: Long
) : Parcelable