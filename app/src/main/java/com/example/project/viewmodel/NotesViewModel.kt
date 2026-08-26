package com.example.project.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.Note
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class SubjectCategoryRow(
    @SerialName("name")
    val name: String = "",

    @SerialName("color_hex")
    val colorHex: String = "#7BD5F5",

    @SerialName("user_id")
    val userId: String = ""
)

data class SubjectUiModel(
    val name: String = "",
    val colorHex: String = "#7BD5F5",
    val notes: List<Note> = emptyList()
) {
    val cardColor: Color
        get() {
            return try {
                Color(colorHex.toColorInt())
            } catch (e: Exception) {
                Color(0xFF7BD5F5)
            }
        }
}

class NotesViewModel : ViewModel() {

    private val noteColors = listOf(
        "#FFE5B4",
        "#E8D7FF",
        "#FFD6E0",
        "#FFF0B3",
        "#D8F3DC",
        "#FFDAB9",
        "#F8D7DA",
        "#E2E8CB",
        "#FFE0AC",
        "#E6D5F7",
        "#FFCCD5",
        "#E3F0D5"
    )

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _subjects = MutableStateFlow<List<SubjectUiModel>>(emptyList())
    val subjects: StateFlow<List<SubjectUiModel>> = _subjects.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    init {
        loadData()
    }

    fun loadData(context: Context? = null) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoaded.value = false
                refreshData(currentUserId)
            } catch (e: Exception) {
                e.printStackTrace()

                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            it,
                            "Unable to load notes: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } finally {
                _isLoaded.value = true
            }
        }
    }

    private suspend fun refreshData(userId: String) {
        val fetchedNotes =
            supabase
                .from("notes")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Note>()

        val fetchedSubjects =
            supabase
                .from("subject_categories")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SubjectCategoryRow>()

        val subjectList =
            fetchedSubjects.map { subject ->
                SubjectUiModel(
                    name = subject.name,
                    colorHex = subject.colorHex,
                    notes = fetchedNotes.filter { note ->
                        note.subjectName.equals(
                            subject.name,
                            ignoreCase = true
                        )
                    }
                )
            }

        _notes.value = fetchedNotes
        _subjects.value = subjectList
    }

    fun getNoteById(noteId: String): Note? {
        return _notes.value.find {
            it.id == noteId
        }
    }

    fun saveNote(
        noteId: String?,
        subjectName: String,
        title: String,
        content: String,
        onSuccess: (() -> Unit)? = null
    ) {
        val currentUserId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {

                if (noteId.isNullOrBlank() || noteId == "new") {

                    val newNote =
                        Note(
                            title = title.trim(),
                            content = content,
                            subjectName = subjectName.trim(),
                            userId = currentUserId,
                            color = noteColors.random()
                        )

                    supabase
                        .from("notes")
                        .insert(newNote)

                } else {

                    val existingNote =
                        _notes.value.find {
                            it.id == noteId
                        }

                    val noteColor =
                        existingNote?.color
                            ?: noteColors.random()

                    val updatedNote =
                        Note(
                            id = noteId,
                            title = title.trim(),
                            content = content,
                            subjectName = subjectName.trim(),
                            userId = currentUserId,
                            color = noteColor
                        )

                    supabase
                        .from("notes")
                        .update(updatedNote) {
                            filter {
                                eq("id", noteId)
                                eq("user_id", currentUserId)
                            }
                        }
                }

                refreshData(currentUserId)

                withContext(Dispatchers.Main) {
                    onSuccess?.invoke()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteNote(
        noteId: String,
        context: Context? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        val currentUserId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {

                supabase
                    .from("notes")
                    .delete {
                        filter {
                            eq("id", noteId)
                            eq("user_id", currentUserId)
                        }
                    }

                refreshData(currentUserId)

                withContext(Dispatchers.Main) {
                    onSuccess?.invoke()
                }

            } catch (e: Exception) {
                e.printStackTrace()

                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            it,
                            "Unable to delete note: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    fun createSubject(
        name: String,
        context: Context? = null,
        colorHex: String = "#7BD5F5"
    ) {
        val currentUserId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        val cleanName = name.trim()

        if (cleanName.isBlank()) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {

                val existing =
                    supabase
                        .from("subject_categories")
                        .select {
                            filter {
                                eq("user_id", currentUserId)
                                ilike("name", cleanName)
                            }
                        }
                        .decodeList<SubjectCategoryRow>()

                if (existing.isNotEmpty()) {

                    context?.let {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                it,
                                "Subject already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    return@launch
                }

                val newSubject =
                    SubjectCategoryRow(
                        name = cleanName,
                        colorHex = colorHex,
                        userId = currentUserId
                    )

                supabase
                    .from("subject_categories")
                    .insert(newSubject)

                refreshData(currentUserId)

            } catch (e: Exception) {

                e.printStackTrace()

                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            it,
                            "Unable to create subject: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    fun deleteSubject(
        subjectName: String,
        context: Context? = null
    ) {
        val currentUserId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {

                supabase
                    .from("notes")
                    .delete {
                        filter {
                            eq("subject_name", subjectName)
                            eq("user_id", currentUserId)
                        }
                    }

                supabase
                    .from("subject_categories")
                    .delete {
                        filter {
                            eq("name", subjectName)
                            eq("user_id", currentUserId)
                        }
                    }

                refreshData(currentUserId)

            } catch (e: Exception) {

                e.printStackTrace()

                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            it,
                            "Unable to delete subject: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    fun getSubjectNames(): List<String> {
        return _subjects.value
            .map { it.name }
            .filter { it.isNotBlank() }
    }
}