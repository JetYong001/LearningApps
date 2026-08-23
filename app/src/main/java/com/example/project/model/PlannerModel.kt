package com.example.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlannerItem(
    @SerialName("id") val id: String? = null,
    @SerialName("item_type") val itemType: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("status") val status: String = "",
    @SerialName("created_at") val createdAt: String? = null
)