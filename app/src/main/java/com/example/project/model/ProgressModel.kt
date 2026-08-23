package com.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName(value = "full_name") val fullName: String = "",
    @SerialName(value = "role") val role: String = ""
)

@Serializable
data class StudySession(
    @SerialName(value = "id") val id: Int? = null,
    @SerialName(value = "duration_minutes") val durationMinutes: Int = 0,
    @SerialName(value = "session_date") val sessionDate: String = ""
)