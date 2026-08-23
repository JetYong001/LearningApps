package com.example.project.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.PlannerItem
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PlannerViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<PlannerItem>>(emptyList())
    val items: StateFlow<List<PlannerItem>> = _items.asStateFlow()

    fun loadItems(context: Context? = null) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = supabase.from("planner_items")
                    .select {
                        filter {
                            eq("user_id", currentUserId)
                        }
                    }
                    .decodeList<PlannerItem>()
                _items.value = result
            } catch (e: Exception) {
                Log.e("PlannerViewModel", "Error loading items", e)
                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            it, "Unable to load planner: ${e.localizedMessage}", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    fun saveItem(item: PlannerItem, context: Context? = null) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
        if (currentUserId.isNullOrBlank()) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val isNew = item.id.isBlank()
        val newItemId = if (isNew) UUID.randomUUID().toString() else item.id
        val savedItem = item.copy(
            id = newItemId,
            userId = currentUserId,
            createdAt = null
        )

        // UI 优先更新
        _items.value = if (isNew) _items.value + savedItem else _items.value.map {
            if (it.id == savedItem.id) savedItem else it
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (isNew) {
                    supabase.from("planner_items").insert(savedItem)
                } else {
                    supabase.from("planner_items").update(savedItem) {
                        filter {
                            eq("id", savedItem.id)
                            eq("user_id", currentUserId)
                        }
                    }
                }
                loadItems(context)
            } catch (e: Exception) {
                Log.e("PlannerViewModel", "Error saving item", e)
                loadItems(context)
                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(it, "Save failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun deleteItem(item: PlannerItem, context: Context? = null) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return
        _items.value = _items.value.filterNot { it.id == item.id }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                supabase.from("planner_items").delete {
                    filter {
                        eq("id", item.id)
                        eq("user_id", currentUserId)
                    }
                }
            } catch (e: Exception) {
                Log.e("PlannerViewModel", "Error deleting item", e)
                loadItems(context)
                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(it, "Unable to delete item: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}