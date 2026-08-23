package com.example.project.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val username: String,
    val profile_picture: String? = null
)