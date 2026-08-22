package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.viewmodel.FlashcardsViewModel
import com.example.project.viewmodel.NotesViewModel

@Composable
fun FlashcardsScreen(
    navController: NavController,
    subjectName: String,
    notesViewModel: NotesViewModel,
    flashcardsViewModel: FlashcardsViewModel
) {
    val flashcards by flashcardsViewModel.flashcards.collectAsState()
    val isLoading by flashcardsViewModel.isLoading.collectAsState()
    val errorMessage by flashcardsViewModel.errorMessage.collectAsState()
    val subjects by notesViewModel.subjects.collectAsState()
    val isLoaded by notesViewModel.isLoaded.collectAsState()

    var currentIndex by remember { mutableStateOf(0) }
    var isAnswerVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        notesViewModel.loadData()
    }

    val currentSubject = remember(subjects, subjectName) {
        subjects.find { it.name.equals(subjectName.trim(), ignoreCase = true) }
    }

    val notes = currentSubject?.notes ?: emptyList()

    LaunchedEffect(isLoaded, currentSubject) {
        if (isLoaded && currentSubject != null && notes.isNotEmpty()) {
            currentIndex = 0
            isAnswerVisible = false
            flashcardsViewModel.generateFlashcards(notes)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Flashcards: $subjectName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            IconButton(
                onClick = {
                    notesViewModel.loadData()
                },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                !isLoaded -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading subject notes...",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                isLoading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Gemini is generating flashcards...",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                notes.isEmpty() -> {
                    Text(
                        text = "No notes found in this subject.",
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }

                flashcards.isNotEmpty() -> {
                    val currentCard = flashcards[currentIndex]

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Card ${currentIndex + 1} of ${flashcards.size}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 16.dp)
                                .clickable { isAnswerVisible = !isAnswerVisible },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (isAnswerVisible) "ANSWER" else "QUESTION",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (isAnswerVisible) currentCard.answer else currentCard.question,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = "Tap to flip",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    if (currentIndex > 0) {
                                        currentIndex--
                                        isAnswerVisible = false
                                    }
                                },
                                enabled = currentIndex > 0
                            ) {
                                Text("Previous")
                            }

                            Button(
                                onClick = {
                                    if (currentIndex < flashcards.size - 1) {
                                        currentIndex++
                                        isAnswerVisible = false
                                    }
                                },
                                enabled = currentIndex < flashcards.size - 1
                            ) {
                                Text("Next")
                            }
                        }
                    }
                }
            }
        }
    }
}