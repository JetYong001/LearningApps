package com.example.project.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.Note
import com.example.project.model.SubjectCategory
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import androidx.core.graphics.toColorInt

data class SubjectUiModel(
    val name: String,
    val cardColor: Color,
    val notes: List<Note>
)

class NotesViewModel : ViewModel() {

    private val _subjects = MutableStateFlow<List<SubjectUiModel>>(emptyList())
    val subjects: StateFlow<List<SubjectUiModel>> = _subjects.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val availableHexColors = listOf(
        "#4FC3F7", "#BA68C8", "#FF8A65", "#AED581", "#FFD54F"
    )

    init {
        loadData()
    }

    fun loadData(context: Context? = null) {
        _isLoaded.value = false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteCategories = supabase.from("subject_categories").select().decodeList<SubjectCategory>()
                val remoteNotes = supabase.from("notes").select().decodeList<Note>()

                val uiModels = remoteCategories.map { category ->
                    val parsedColor = try {
                        Color(category.colorHex.toColorInt())
                    } catch (e: Exception) {
                        Color(0xFF4FC3F7)
                    }
                    SubjectUiModel(
                        name = category.name,
                        cardColor = parsedColor,
                        notes = remoteNotes.filter { it.subjectName.equals(category.name, ignoreCase = true) }
                    )
                }
                _subjects.value = uiModels
            } catch (e: Exception) {
                if (context != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Load: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            } finally {
                _isLoaded.value = true
            }
        }
    }

    fun createSubject(subjectName: String, context: Context? = null) {
        val trimmed = subjectName.trim()
        if (trimmed.isEmpty()) return

        val chosenColor = availableHexColors.random()
        val parsedColor = try {
            Color(chosenColor.toColorInt())
        } catch (e: Exception) {
            Color(0xFF4FC3F7)
        }

        val currentList = _subjects.value.toMutableList()
        if (currentList.none { it.name.equals(trimmed, ignoreCase = true) }) {
            currentList.add(SubjectUiModel(name = trimmed, cardColor = parsedColor, notes = emptyList()))
            _subjects.value = currentList
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newCategory = SubjectCategory(name = trimmed, colorHex = chosenColor)
                supabase.from("subject_categories").insert(newCategory)
            } catch (e: Exception) {
                if (context != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Create: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun saveNote(noteId: String?, subjectName: String, title: String, content: String, context: Context? = null) {
        val trimmedSubject = subjectName.trim()
        val trimmedTitle = title.trim()
        if (trimmedSubject.isEmpty() || trimmedTitle.isEmpty()) return

        val targetNoteId = noteId ?: UUID.randomUUID().toString()
        val newNote = Note(id = targetNoteId, title = trimmedTitle, content = content, subjectName = trimmedSubject)

        val currentList = _subjects.value.toMutableList()
        val index = currentList.indexOfFirst { it.name.equals(trimmedSubject, ignoreCase = true) }

        if (index != -1) {
            val targetSubject = currentList[index]
            val updatedNotes = targetSubject.notes.filterNot { it.id == targetNoteId }.toMutableList()
            updatedNotes.add(0, newNote)
            currentList[index] = targetSubject.copy(notes = updatedNotes)
        } else {
            val chosenColor = availableHexColors.random()
            val parsedColor = try {
                Color(chosenColor.toColorInt())
            } catch (e: Exception) {
                Color(0xFF4FC3F7)
            }
            currentList.add(SubjectUiModel(name = trimmedSubject, cardColor = parsedColor, notes = listOf(newNote)))
        }

        _subjects.value = currentList

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (index == -1) {
                    val chosenColor = availableHexColors.random()
                    val newCategory = SubjectCategory(name = trimmedSubject, colorHex = chosenColor)
                    supabase.from("subject_categories").insert(newCategory)
                }

                if (noteId != null) {
                    supabase.from("notes").update(newNote) { filter { eq("id", noteId) } }
                } else {
                    supabase.from("notes").insert(newNote)
                }
            } catch (e: Exception) {
                if (context != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Save: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun getNoteAndSubject(noteId: String): Pair<Note, String>? {
        _subjects.value.forEach { uiModel ->
            val note = uiModel.notes.find { it.id == noteId }
            if (note != null) return Pair(note, uiModel.name)
        }
        return null
    }

    fun deleteSubject(subjectName: String, context: Context? = null) {
        val currentList = _subjects.value.toMutableList()
        val index = currentList.indexOfFirst { it.name.equals(subjectName, ignoreCase = true) }

        if (index != -1) {
            currentList.removeAt(index)
            _subjects.value = currentList

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    supabase.from("notes").delete { filter { eq("subject_name", subjectName) } }
                    supabase.from("subject_categories").delete { filter { eq("name", subjectName) } }
                } catch (e: Exception) {
                    if (context != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Delete Subject: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    fun deleteNote(noteId: String, context: Context? = null) {
        val currentList = _subjects.value.toMutableList()
        var isUpdated = false

        for (i in currentList.indices) {
            val subject = currentList[i]
            if (subject.notes.any { it.id == noteId }) {
                val updatedNotes = subject.notes.filterNot { it.id == noteId }
                currentList[i] = subject.copy(notes = updatedNotes)
                isUpdated = true
                break
            }
        }

        if (isUpdated) {
            _subjects.value = currentList

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    supabase.from("notes").delete { filter { eq("id", noteId) } }
                } catch (e: Exception) {
                    if (context != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Delete Note: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}