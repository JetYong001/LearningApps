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


/*
 * =========================================
 * SUBJECT DATABASE MODEL
 * =========================================
 */

@Serializable
private data class SubjectCategoryRow(

    @SerialName("name")
    val name: String = "",

    @SerialName("color_hex")
    val colorHex: String = "#7BD5F5",

    @SerialName("user_id")
    val userId: String = ""
)


/*
 * =========================================
 * SUBJECT UI MODEL
 * =========================================
 */

data class SubjectUiModel(

    val name: String = "",

    val colorHex: String = "#7BD5F5",

    val notes: List<Note> = emptyList()

) {

    val cardColor: Color
        get() {

            return try {

                Color(
                    colorHex.toColorInt()
                )

            } catch (e: Exception) {

                Color(0xFF7BD5F5)
            }
        }
}


/*
 * =========================================
 * VIEW MODEL
 * =========================================
 */

class NotesViewModel : ViewModel() {


    /*
     * =====================================
     * NOTES
     * =====================================
     */

    private val _notes =
        MutableStateFlow<List<Note>>(emptyList())

    val notes: StateFlow<List<Note>> =
        _notes.asStateFlow()


    /*
     * =====================================
     * SUBJECTS
     * =====================================
     */

    private val _subjects =
        MutableStateFlow<List<SubjectUiModel>>(emptyList())

    val subjects: StateFlow<List<SubjectUiModel>> =
        _subjects.asStateFlow()


    /*
     * =====================================
     * LOADING
     * =====================================
     */

    private val _isLoaded =
        MutableStateFlow(false)

    val isLoaded: StateFlow<Boolean> =
        _isLoaded.asStateFlow()


    /*
     * =========================================
     * LOAD DATA
     * =========================================
     */

    fun loadData(
        context: Context? = null
    ) {

        val currentUserId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        viewModelScope.launch(Dispatchers.IO) {

            _isLoaded.value = false

            try {

                /*
                 * LOAD NOTES
                 */

                val fetchedNotes =
                    supabase
                        .from("notes")
                        .select {

                            filter {

                                eq(
                                    "user_id",
                                    currentUserId
                                )
                            }
                        }
                        .decodeList<Note>()


                /*
                 * LOAD SUBJECTS
                 */

                val fetchedSubjects =
                    supabase
                        .from("subject_categories")
                        .select {

                            filter {

                                eq(
                                    "user_id",
                                    currentUserId
                                )
                            }
                        }
                        .decodeList<SubjectCategoryRow>()


                /*
                 * SAVE NOTES
                 */

                _notes.value = fetchedNotes


                /*
                 * GROUP NOTES UNDER SUBJECT
                 */

                val subjectList =
                    fetchedSubjects.map { subject ->

                        SubjectUiModel(

                            name = subject.name,

                            colorHex = subject.colorHex,

                            notes =
                                fetchedNotes.filter { note ->

                                    note.subjectName.equals(
                                        subject.name,
                                        ignoreCase = true
                                    )
                                }
                        )
                    }


                /*
                 * SAVE SUBJECTS
                 */

                _subjects.value = subjectList


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


    /*
     * =========================================
     * GET NOTE BY ID
     * =========================================
     */

    fun getNoteById(
        noteId: String
    ): Note? {

        return _notes.value.find {

            it.id == noteId
        }
    }


    /*
     * =========================================
     * SAVE NOTE
     * =========================================
     */

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

                /*
                 * =================================
                 * CREATE NEW NOTE
                 * =================================
                 */

                if (
                    noteId.isNullOrBlank() ||
                    noteId == "new"
                ) {

                    val newNote = Note(

                        title = title.trim(),

                        content = content,

                        subjectName = subjectName.trim(),

                        userId = currentUserId
                    )

                    supabase
                        .from("notes")
                        .insert(newNote)

                }

                /*
                 * =================================
                 * UPDATE EXISTING NOTE
                 * =================================
                 */

                else {

                    val updatedNote = Note(

                        id = noteId,

                        title = title.trim(),

                        content = content,

                        subjectName = subjectName.trim(),

                        userId = currentUserId
                    )

                    supabase
                        .from("notes")
                        .update(updatedNote) {

                            filter {

                                eq(
                                    "id",
                                    noteId
                                )

                                eq(
                                    "user_id",
                                    currentUserId
                                )
                            }
                        }
                }


                /*
                 * =================================
                 * RELOAD
                 * =================================
                 */

                loadData()


                withContext(Dispatchers.Main) {

                    onSuccess?.invoke()
                }


            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }


    /*
     * =========================================
     * DELETE NOTE
     * =========================================
     */

    fun deleteNote(

        noteId: String,

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

                            eq(
                                "id",
                                noteId
                            )

                            eq(
                                "user_id",
                                currentUserId
                            )
                        }
                    }


                loadData(context)


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


    /*
     * =========================================
     * CREATE SUBJECT
     * =========================================
     */

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

                /*
                 * CHECK DUPLICATE SUBJECT
                 */

                val alreadyExists =
                    _subjects.value.any {

                        it.name.equals(
                            cleanName,
                            ignoreCase = true
                        )
                    }


                if (alreadyExists) {

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


                /*
                 * CREATE SUBJECT
                 */

                val newSubject =
                    SubjectCategoryRow(

                        name = cleanName,

                        colorHex = colorHex,

                        userId = currentUserId
                    )


                supabase
                    .from("subject_categories")
                    .insert(newSubject)


                /*
                 * RELOAD
                 */

                loadData(context)


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


    /*
     * =========================================
     * DELETE SUBJECT
     * =========================================
     */

    fun deleteSubject(

        subjectName: String,

        context: Context? = null

    ) {

        val currentUserId =
            supabase.auth.currentUserOrNull()?.id
                ?: return


        viewModelScope.launch(Dispatchers.IO) {

            try {

                /*
                 * DELETE NOTES
                 */

                supabase
                    .from("notes")
                    .delete {

                        filter {

                            eq(
                                "subject_name",
                                subjectName
                            )

                            eq(
                                "user_id",
                                currentUserId
                            )
                        }
                    }


                /*
                 * DELETE SUBJECT
                 */

                supabase
                    .from("subject_categories")
                    .delete {

                        filter {

                            eq(
                                "name",
                                subjectName
                            )

                            eq(
                                "user_id",
                                currentUserId
                            )
                        }
                    }


                /*
                 * RELOAD
                 */

                loadData(context)


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


    /*
     * =========================================
     * GET SUBJECT NAMES
     * =========================================
     */

    fun getSubjectNames(): List<String> {

        return _subjects.value
            .map {
                it.name
            }
            .filter {
                it.isNotBlank()
            }
    }
}