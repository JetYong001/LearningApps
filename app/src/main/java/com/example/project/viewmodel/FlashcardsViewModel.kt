package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.model.Note
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Flashcard(
    val question: String,
    val answer: String
)

class FlashcardsViewModel : ViewModel() {

    private val generativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-3.6-flash"
    )

    private val _flashcards =
        MutableStateFlow<List<Flashcard>>(emptyList())

    val flashcards: StateFlow<List<Flashcard>> =
        _flashcards

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage

    private var lastNotesKey: String? = null

    private var generationId = 0

    fun generateFlashcards(
        notes: List<Note>,
        forceRefresh: Boolean = false
    ) {

        if (notes.isEmpty()) {

            _flashcards.value = emptyList()

            _errorMessage.value =
                "No notes found in this subject."

            return
        }

        val notesKey =
            notes
                .sortedBy { it.id }
                .joinToString("|") {
                    "${it.id}:${it.title}:${it.content}"
                }

        if (
            !forceRefresh &&
            notesKey == lastNotesKey &&
            _flashcards.value.isNotEmpty()
        ) {
            return
        }

        if (_isLoading.value) {
            return
        }

        generationId++

        val currentGeneration =
            generationId

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            try {

                val notesContent =
                    notes
                        .take(5)
                        .joinToString("\n\n") {
                            "Title: ${it.title}\nContent: ${it.content.take(1500)}"
                        }

                val prompt = """
                    Create 2 to 10 flashcards from these notes based on the content of notes.
                    Use the same language as the notes.
                    Format: Question | Answer
                    No numbering or extra text.
                    
                    $notesContent
                    """.trimIndent()
                val response =
                    generativeModel.generateContent(prompt)

                if (currentGeneration != generationId) {
                    return@launch
                }

                val responseText =
                    response.text
                        ?.trim()
                        ?: ""

                val parsedCards =
                    responseText
                        .lines()
                        .mapNotNull { line ->

                            val cleanLine =
                                line
                                    .trim()
                                    .removePrefix("- ")
                                    .removePrefix("* ")

                            val parts =
                                cleanLine.split(
                                    "|",
                                    limit = 2
                                )

                            if (
                                parts.size == 2 &&
                                parts[0].isNotBlank() &&
                                parts[1].isNotBlank()
                            ) {

                                Flashcard(
                                    question =
                                        parts[0].trim(),

                                    answer =
                                        parts[1].trim()
                                )

                            } else {
                                null
                            }
                        }
                        .take(5)

                if (parsedCards.isNotEmpty()) {

                    _flashcards.value =
                        parsedCards

                    lastNotesKey =
                        notesKey

                } else {

                    _errorMessage.value =
                        "Unable to create flashcards. Please try again."
                }

            } catch (e: Exception) {

                if (currentGeneration == generationId) {

                    _errorMessage.value =
                        e.localizedMessage
                            ?: "Unable to generate flashcards."
                }

            } finally {

                if (currentGeneration == generationId) {

                    _isLoading.value = false
                }
            }
        }
    }
}