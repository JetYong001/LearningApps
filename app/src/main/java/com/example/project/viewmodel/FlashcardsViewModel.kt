package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.model.Note
import com.google.firebase.ai.ai
import com.google.firebase.Firebase
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

    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun generateFlashcards(notes: List<Note>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _flashcards.value = emptyList()

            if (notes.isEmpty()) {
                _errorMessage.value = "No notes found in this subject."
                _isLoading.value = false
                return@launch
            }

            val notesContent = notes.joinToString("\n\n") { "Title: ${it.title}\nContent: ${it.content}" }

            val prompt = """
                Based on the following study notes, generate a list of flashcards (questions and concise answers) to help test understanding.
                Format each flashcard strictly as follows:
                Question | Answer
                
                Do not include any bullet points, numbering, or additional text outside of this pattern.
                
                Notes:
                $notesContent
            """.trimIndent()

            try {
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: ""

                val parsedCards = responseText.lines().mapNotNull { line ->
                    val parts = line.split("|")
                    if (parts.size == 2) {
                        Flashcard(question = parts[0].trim(), answer = parts[1].trim())
                    } else {
                        null
                    }
                }

                if (parsedCards.isNotEmpty()) {
                    _flashcards.value = parsedCards
                } else {
                    _errorMessage.value = "Failed to parse questions from response."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error generating content: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}