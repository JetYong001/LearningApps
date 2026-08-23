package com.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskItem(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("title")
    val title: String,
    @SerialName("time")
    val time: String,
    @SerialName("is_completed")
    val isCompleted: Boolean = false
)