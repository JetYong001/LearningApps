package com.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudySession(
    @SerialName("id")
    val id: String = "",

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("duration_minutes")
    val durationMinutes: Int = 0,

    @SerialName("created_at")
    val createdAt: String = ""
)