package com.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudySession(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("duration_seconds")
    val durationSeconds: Int = 0,

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class StudySessionInsert(
    @SerialName("user_id")
    val userId: String,

    @SerialName("duration_seconds")
    val durationSeconds: Int
)