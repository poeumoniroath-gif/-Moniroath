package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.model.UserRole
import com.example.model.UserSession

object SessionManager {
    private const val PREF_NAME = "jolly_slushie_session_pref"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USERNAME = "username"
    private const val KEY_ROLE = "user_role"
    private const val KEY_TIMESTAMP = "login_timestamp"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, session: UserSession) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USERNAME, session.username)
            .putString(KEY_ROLE, session.role.name)
            .putLong(KEY_TIMESTAMP, session.loginTimestamp)
            .apply()
    }

    fun getSession(context: Context): UserSession? {
        val prefs = getPrefs(context)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val roleStr = prefs.getString(KEY_ROLE, null) ?: return null
        val timestamp = prefs.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

        return try {
            val role = UserRole.valueOf(roleStr)
            UserSession(username = username, role = role, loginTimestamp = timestamp)
        } catch (_: Exception) {
            null
        }
    }

    fun clearSession(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().clear().apply()
    }
}
