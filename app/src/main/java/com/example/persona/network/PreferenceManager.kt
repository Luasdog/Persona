package com.example.persona.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.persona.model.User
import com.google.gson.Gson

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(Constants.KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(Constants.KEY_TOKEN, null)
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(Constants.KEY_USER, userJson).apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(Constants.KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else null
    }

    fun clearUserData() {
        sharedPreferences.edit()
            .remove(Constants.KEY_TOKEN)
            .remove(Constants.KEY_USER)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
