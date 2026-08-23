package com.example.project.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PlannerItem(
    @SerialName("id")
    val id: String = "",

    @SerialName("title")
    val title: String = "",

    @SerialName("description")
    val description: String = "",

    @SerialName("due_at")
    val dueAt: String = "",

    @SerialName("status")
    val status: String = "In progress",

    @SerialName("item_type")
    val itemType: String = "task",

    @SerialName("created_at")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val createdAt: String? = null,

    @SerialName("user_id")
    val userId: String? = null
)