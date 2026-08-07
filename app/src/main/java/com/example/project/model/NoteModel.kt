package com.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("content") val content: String = "",
    @SerialName("subject_name") val subjectName: String = ""
)

@Serializable
data class SubjectCategory(
    @SerialName("name") val name: String = "",
    @SerialName("color_hex") val colorHex: String = ""
)