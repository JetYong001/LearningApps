package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.PlannerItem
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class PlannerViewModel : ViewModel() {

    private val _items =
        MutableStateFlow<List<PlannerItem>>(emptyList())

    val items: StateFlow<List<PlannerItem>> =
        _items.asStateFlow()

    private var loadedUserId: String? = null
    private var firstLoadCompleted = false

    private val formatter =
        DateTimeFormatter.ofPattern(
            "dd MMMM yyyy, HH:mm",
            Locale.ENGLISH
        )

    init {
        startMissedChecker()
    }

    suspend fun loadItemsAwait(
        forceRefresh: Boolean = false
    ) {
        val currentUserId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        if (
            !forceRefresh &&
            firstLoadCompleted &&
            loadedUserId == currentUserId
        ) {
            return
        }

        try {
            val result =
                withContext(Dispatchers.IO) {
                    supabase
                        .from("planner_items")
                        .select {
                            filter {
                                eq(
                                    "user_id",
                                    currentUserId
                                )
                            }
                        }
                        .decodeList<PlannerItem>()
                }

            _items.value = result

            loadedUserId =
                currentUserId

            firstLoadCompleted =
                true

            checkAndUpdateMissedItems(
                currentUserId
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun checkAndUpdateMissedItems(
        currentUserId: String
    ) {
        val currentItems =
            _items.value

        val now =
            System.currentTimeMillis()

        val missedItems =
            currentItems.filter { item ->

                !item.status.equals(
                    "Completed",
                    ignoreCase = true
                ) &&
                        !item.status.equals(
                            "Missed",
                            ignoreCase = true
                        ) &&
                        isPastDue(
                            item.dueAt,
                            now
                        )
            }

        if (missedItems.isEmpty()) {
            return
        }

        withContext(Dispatchers.IO) {

            missedItems.forEach { item ->

                try {

                    supabase
                        .from("planner_items")
                        .update(
                            {
                                set(
                                    "status",
                                    "Missed"
                                )
                            }
                        ) {
                            filter {
                                eq(
                                    "id",
                                    item.id
                                )

                                eq(
                                    "user_id",
                                    currentUserId
                                )
                            }
                        }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val missedIds =
            missedItems
                .map { it.id }
                .toSet()

        _items.value =
            currentItems.map { item ->

                if (
                    item.id in missedIds
                ) {
                    item.copy(
                        status = "Missed"
                    )
                } else {
                    item
                }
            }
    }

    private fun startMissedChecker() {

        viewModelScope.launch {

            while (isActive) {

                try {

                    val currentUserId =
                        supabase
                            .auth
                            .currentUserOrNull()
                            ?.id

                    if (
                        currentUserId != null
                    ) {

                        if (
                            firstLoadCompleted &&
                            loadedUserId == currentUserId
                        ) {

                            checkAndUpdateMissedItems(
                                currentUserId
                            )

                        } else {

                            loadItemsAwait(
                                forceRefresh = true
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(30_000L)
            }
        }
    }

    fun loadItems(
        forceRefresh: Boolean = false
    ) {
        if (
            !forceRefresh &&
            firstLoadCompleted
        ) {
            return
        }

        viewModelScope.launch {
            loadItemsAwait(
                forceRefresh
            )
        }
    }

    fun refreshItemsInBackground() {

        viewModelScope.launch {

            loadItemsAwait(
                forceRefresh = true
            )
        }
    }

    fun saveItem(
        item: PlannerItem
    ) {
        val currentUserId =
            supabase
                .auth
                .currentUserOrNull()
                ?.id
                ?: return

        val isNew =
            item.id.isBlank()

        val savedItem =
            item.copy(
                id =
                    if (isNew) {
                        UUID.randomUUID()
                            .toString()
                    } else {
                        item.id
                    },

                userId =
                    currentUserId,

                createdAt =
                    if (isNew) {
                        null
                    } else {
                        item.createdAt
                    }
            )

        val previousItems =
            _items.value

        _items.value =
            if (isNew) {

                previousItems +
                        savedItem

            } else {

                previousItems.map { oldItem ->

                    if (
                        oldItem.id ==
                        savedItem.id
                    ) {
                        savedItem
                    } else {
                        oldItem
                    }
                }
            }

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            try {

                if (isNew) {

                    supabase
                        .from("planner_items")
                        .insert(
                            savedItem
                        )

                } else {

                    supabase
                        .from("planner_items")
                        .update(
                            {

                                set(
                                    "title",
                                    savedItem.title
                                )

                                set(
                                    "description",
                                    savedItem.description
                                )

                                set(
                                    "due_at",
                                    savedItem.dueAt
                                )

                                set(
                                    "status",
                                    savedItem.status
                                )

                                set(
                                    "item_type",
                                    savedItem.itemType
                                )
                            }
                        ) {

                            filter {

                                eq(
                                    "id",
                                    savedItem.id
                                )

                                eq(
                                    "user_id",
                                    currentUserId
                                )
                            }
                        }
                }

                loadedUserId =
                    currentUserId

                firstLoadCompleted =
                    true

            } catch (e: Exception) {

                _items.value =
                    previousItems

                e.printStackTrace()
            }
        }
    }

    fun deleteItem(
        item: PlannerItem
    ) {
        val currentUserId =
            supabase
                .auth
                .currentUserOrNull()
                ?.id
                ?: return

        val previousItems =
            _items.value

        _items.value =
            previousItems.filterNot {
                it.id == item.id
            }

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            try {

                supabase
                    .from("planner_items")
                    .delete {

                        filter {

                            eq(
                                "id",
                                item.id
                            )

                            eq(
                                "user_id",
                                currentUserId
                            )
                        }
                    }

            } catch (e: Exception) {

                _items.value =
                    previousItems

                e.printStackTrace()
            }
        }
    }

    private fun isPastDue(
        dueAt: String,
        currentTime: Long
    ): Boolean {

        return try {

            val localDateTime =
                LocalDateTime.parse(
                    dueAt,
                    formatter
                )

            val dueMillis =
                localDateTime
                    .atZone(
                        ZoneId.systemDefault()
                    )
                    .toInstant()
                    .toEpochMilli()

            dueMillis <
                    currentTime

        } catch (e: Exception) {

            false
        }
    }
}