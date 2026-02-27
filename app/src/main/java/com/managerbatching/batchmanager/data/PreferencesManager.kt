package com.managerbatching.batchmanager.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.managerbatching.batchmanager.model.Batch

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("batch_manager_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_BATCHES = "batches"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_USER_NAME = "user_name"
    }

    fun saveBatches(batches: List<Batch>) {
        prefs.edit().putString(KEY_BATCHES, gson.toJson(batches)).apply()
    }

    fun loadBatches(): List<Batch> {
        val json = prefs.getString(KEY_BATCHES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Batch>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    fun setOnboardingDone() = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun setUserName(name: String) = prefs.edit().putString(KEY_USER_NAME, name).apply()
}