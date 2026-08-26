package com.example.project.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {

    private val _profile =
        MutableStateFlow<Profile?>(null)

    val profile: StateFlow<Profile?> =
        _profile.asStateFlow()

    private val _isUpdating =
        MutableStateFlow(false)

    val isUpdating: StateFlow<Boolean> =
        _isUpdating.asStateFlow()

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    private var loadedUserId: String? = null

    suspend fun loadProfileAwait(
        forceRefresh: Boolean = false
    ) {
        val userId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        if (
            !forceRefresh &&
            loadedUserId == userId &&
            _profile.value != null
        ) {
            return
        }

        try {
            val profile =
                withContext(Dispatchers.IO) {
                    supabase
                        .from("profiles")
                        .select {
                            filter {
                                eq(
                                    "id",
                                    userId
                                )
                            }
                        }
                        .decodeSingle<Profile>()
                }

            _profile.value = profile
            loadedUserId = userId

        } catch (e: Exception) {
            e.printStackTrace()

            _errorMessage.value =
                e.message
                    ?: "Failed to load profile"
        }
    }

    fun loadProfile(
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            loadProfileAwait(
                forceRefresh = forceRefresh
            )
        }
    }

    fun refreshProfileInBackground() {
        viewModelScope.launch {
            loadProfileAwait(
                forceRefresh = true
            )
        }
    }

    fun updateUsername(
        username: String,
        onSuccess: () -> Unit
    ) {
        val userId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        val oldProfile =
            _profile.value

        val updatedProfile =
            oldProfile?.copy(
                username = username
            )

        _profile.value =
            updatedProfile

        viewModelScope.launch {

            _isUpdating.value = true
            _errorMessage.value = null

            try {

                withContext(Dispatchers.IO) {
                    supabase
                        .from("profiles")
                        .update(
                            {
                                set(
                                    "username",
                                    username
                                )
                            }
                        ) {
                            filter {
                                eq(
                                    "id",
                                    userId
                                )
                            }
                        }
                }

                loadedUserId =
                    userId

                onSuccess()

            } catch (e: Exception) {

                _profile.value =
                    oldProfile

                _errorMessage.value =
                    e.message
                        ?: "Failed to update username"

            } finally {

                _isUpdating.value = false
            }
        }
    }

    fun updateProfilePicture(
        context: Context,
        imageUri: Uri
    ) {
        val userId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        viewModelScope.launch {

            _isUpdating.value = true
            _errorMessage.value = null

            try {

                val imageBytes =
                    withContext(Dispatchers.IO) {
                        context.contentResolver
                            .openInputStream(imageUri)
                            ?.use {
                                it.readBytes()
                            }
                            ?: throw Exception(
                                "Unable to open selected image"
                            )
                    }

                val filePath =
                    "$userId/avatar.jpg"

                withContext(Dispatchers.IO) {
                    supabase
                        .storage
                        .from("profile-pictures")
                        .upload(
                            path = filePath,
                            data = imageBytes
                        ) {
                            upsert = true
                        }
                }

                val baseUrl =
                    withContext(Dispatchers.IO) {
                        supabase
                            .storage
                            .from("profile-pictures")
                            .publicUrl(
                                filePath
                            )
                    }

                val profilePictureUrl =
                    "$baseUrl?v=${System.currentTimeMillis()}"

                withContext(Dispatchers.IO) {
                    supabase
                        .from("profiles")
                        .update(
                            {
                                set(
                                    "profile_picture",
                                    profilePictureUrl
                                )
                            }
                        ) {
                            filter {
                                eq(
                                    "id",
                                    userId
                                )
                            }
                        }
                }

                _profile.value =
                    _profile.value?.copy(
                        profile_picture =
                            profilePictureUrl
                    )

                loadedUserId =
                    userId

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Failed to update profile picture"

            } finally {

                _isUpdating.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearProfile() {
        _profile.value = null
        loadedUserId = null
    }
}