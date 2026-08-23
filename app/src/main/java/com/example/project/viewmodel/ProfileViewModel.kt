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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val userId =
                    supabase.auth
                        .currentUserOrNull()
                        ?.id
                        ?: return@launch

                val result =
                    supabase
                        .from("profiles")
                        .select {
                            filter {
                                eq("id", userId)
                            }
                        }
                        .decodeSingle<Profile>()

                _profile.value = result

            } catch (e: Exception) {
                _errorMessage.value =
                    e.message
                        ?: "Failed to load profile"
            }
        }
    }

    fun updateUsername(
        username: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId =
                    supabase.auth
                        .currentUserOrNull()
                        ?.id
                        ?: return@launch

                _isUpdating.value = true
                _errorMessage.value = null

                supabase
                    .from("profiles")
                    .update({
                        set(
                            "username",
                            username
                        )
                    }) {
                        filter {
                            eq(
                                "id",
                                userId
                            )
                        }
                    }

                val updatedProfile =
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

                _profile.value = updatedProfile

                onSuccess()

            } catch (e: Exception) {
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
        viewModelScope.launch {
            try {
                val userId =
                    supabase.auth
                        .currentUserOrNull()
                        ?.id
                        ?: return@launch

                _isUpdating.value = true
                _errorMessage.value = null

                val inputStream =
                    context.contentResolver
                        .openInputStream(imageUri)

                if (inputStream == null) {
                    _errorMessage.value =
                        "Unable to open selected image"

                    return@launch
                }

                val imageBytes =
                    inputStream.use {
                        it.readBytes()
                    }

                val filePath =
                    "$userId/avatar.jpg"

                supabase
                    .storage
                    .from("profile-pictures")
                    .upload(
                        path = filePath,
                        data = imageBytes
                    ) {
                        upsert = true
                    }

                val baseUrl =
                    supabase
                        .storage
                        .from("profile-pictures")
                        .publicUrl(filePath)

                val profilePictureUrl =
                    "$baseUrl?v=${System.currentTimeMillis()}"

                supabase
                    .from("profiles")
                    .update({
                        set(
                            "profile_picture",
                            profilePictureUrl
                        )
                    }) {
                        filter {
                            eq(
                                "id",
                                userId
                            )
                        }
                    }

                val updatedProfile =
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

                _profile.value = updatedProfile

            } catch (e: Exception) {
                _errorMessage.value =
                    e.message
                        ?: "Failed to update profile picture"
            } finally {
                _isUpdating.value = false
            }
        }
    }
}