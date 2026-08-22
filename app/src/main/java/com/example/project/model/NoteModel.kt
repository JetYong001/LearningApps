package com.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import java.util.UUID

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),

    val title: String = "",

    val content: String = "",

    @SerialName("subject_name")
    val subjectName: String = "",

    @SerialName("user_id")
    val userId: String = ""
)