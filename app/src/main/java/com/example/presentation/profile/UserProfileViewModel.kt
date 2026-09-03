package com.example.presentation.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.userProfileDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile_prefs")

class UserProfileViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AVATAR_URI = stringPreferencesKey("user_avatar_uri")
    }

    val userName: StateFlow<String> = appContext.userProfileDataStore.data
        .map { prefs -> prefs[PreferencesKeys.USER_NAME] ?: "Müziksever" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Müziksever")

    val userAvatarUri: StateFlow<String?> = appContext.userProfileDataStore.data
        .map { prefs -> prefs[PreferencesKeys.USER_AVATAR_URI] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateProfile(name: String, avatarUri: String?) {
        viewModelScope.launch {
            appContext.userProfileDataStore.edit { prefs ->
                prefs[PreferencesKeys.USER_NAME] = name.ifBlank { "Müziksever" }
                if (!avatarUri.isNullOrBlank()) {
                    prefs[PreferencesKeys.USER_AVATAR_URI] = avatarUri
                } else {
                    prefs.remove(PreferencesKeys.USER_AVATAR_URI)
                }
            }
        }
    }
}
