package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.data.supabase
import com.example.project.viewmodel.FlashcardsViewModel
import com.example.project.viewmodel.NotesViewModel
import io.github.jan.supabase.auth.auth

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

    var currentIndex by remember {
        mutableStateOf(0)
    }

    var isAnswerVisible by remember {
        mutableStateOf(false)
    }

    val currentUserId =
        supabase.auth.currentUserOrNull()?.id

    /*
     * Load notes when entering this screen.
     */
    LaunchedEffect(currentUserId) {
        notesViewModel.loadData()
    }

    val currentSubject = remember(
        subjects,
        subjectName
    ) {
        subjects.find {
            it.name.equals(
                subjectName.trim(),
                ignoreCase = true
            )
        }
    }

    val notes =
        currentSubject?.notes ?: emptyList()

    /*
     * Generate flashcards after notes are loaded.
     */
    LaunchedEffect(
        isLoaded,
        currentSubject
    ) {
        if (
            isLoaded &&
            currentSubject != null &&
            notes.isNotEmpty()
        ) {
            currentIndex = 0
            isAnswerVisible = false

            flashcardsViewModel.generateFlashcards(
                notes
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        /*
         * Top Header
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(
                    horizontal = 6.dp,
                    vertical = 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint =
                        MaterialTheme.colorScheme.onPrimary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        horizontal = 6.dp
                    )
            ) {

                Text(
                    text = "Flashcards",
                    fontSize = 13.sp,
                    color =
                        MaterialTheme.colorScheme.onPrimary
                            .copy(alpha = 0.75f)
                )

                Text(
                    text = subjectName,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = {
                    if (!isLoading) {

                        currentIndex = 0
                        isAnswerVisible = false

                        notesViewModel.loadData()
                    }
                },
                enabled = !isLoading
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint =
                        MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        /*
         * Main Content
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {

            when {

                /*
                 * Loading notes
                 */
                !isLoaded -> {

                    LoadingContent(
                        text = "Loading your notes..."
                    )
                }

                /*
                 * Generating flashcards
                 */
                isLoading -> {

                    LoadingContent(
                        text = "Generating flashcards..."
                    )
                }

                /*
                 * Error
                 */
                errorMessage != null -> {

                    ErrorContent(
                        message =
                            errorMessage
                                ?: "Something went wrong",
                        onRetry = {

                            currentIndex = 0
                            isAnswerVisible = false

                            notesViewModel.loadData()
                        }
                    )
                }

                /*
                 * No notes
                 */
                notes.isEmpty() -> {

                    EmptyContent()
                }

                /*
                 * Flashcards available
                 */
                flashcards.isNotEmpty() -> {

                    /*
                     * Prevent index from becoming invalid
                     */
                    if (currentIndex >= flashcards.size) {
                        currentIndex = 0
                    }

                    val currentCard =
                        flashcards[currentIndex]

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        /*
                         * Card counter
                         */
                        Text(
                            text =
                                "CARD ${currentIndex + 1} OF ${flashcards.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        /*
                         * Progress
                         */
                        LinearProgressIndicator(
                            progress = {
                                (currentIndex + 1).toFloat() /
                                        flashcards.size.toFloat()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp),
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary,
                            trackColor =
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        /*
                         * Flashcard
                         */
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clickable {
                                    isAnswerVisible =
                                        !isAnswerVisible
                                },
                            shape =
                                RoundedCornerShape(28.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        if (isAnswerVisible) {
                                            MaterialTheme
                                                .colorScheme
                                                .secondaryContainer
                                        } else {
                                            MaterialTheme
                                                .colorScheme
                                                .primaryContainer
                                        }
                                ),
                            elevation =
                                CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                )
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(28.dp),
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Column(
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {

                                    /*
                                     * Question / Answer label
                                     */
                                    Surface(
                                        shape =
                                            RoundedCornerShape(
                                                50.dp
                                            ),
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .surface
                                                .copy(
                                                    alpha = 0.75f
                                                )
                                    ) {

                                        Row(
                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 14.dp,
                                                    vertical = 7.dp
                                                ),
                                            verticalAlignment =
                                                Alignment.CenterVertically
                                        ) {

                                            Icon(
                                                imageVector =
                                                    Icons.Default.AutoAwesome,
                                                contentDescription =
                                                    null,
                                                modifier =
                                                    Modifier.size(
                                                        16.dp
                                                    ),
                                                tint =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .primary
                                            )

                                            Spacer(
                                                modifier =
                                                    Modifier.width(6.dp)
                                            )

                                            Text(
                                                text =
                                                    if (
                                                        isAnswerVisible
                                                    ) {
                                                        "ANSWER"
                                                    } else {
                                                        "QUESTION"
                                                    },
                                                fontSize = 12.sp,
                                                fontWeight =
                                                    FontWeight.Bold,
                                                color =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .primary
                                            )
                                        }
                                    }

                                    Spacer(
                                        modifier =
                                            Modifier.height(28.dp)
                                    )

                                    /*
                                     * Question / Answer
                                     */
                                    Text(
                                        text =
                                            if (
                                                isAnswerVisible
                                            ) {
                                                currentCard.answer
                                            } else {
                                                currentCard.question
                                            },
                                        fontSize = 22.sp,
                                        fontWeight =
                                            FontWeight.SemiBold,
                                        textAlign =
                                            TextAlign.Center,
                                        lineHeight = 30.sp,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurface
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(28.dp)
                                    )

                                    /*
                                     * Flip hint
                                     */
                                    Surface(
                                        shape =
                                            RoundedCornerShape(
                                                50.dp
                                            ),
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .surface
                                                .copy(
                                                    alpha = 0.6f
                                                )
                                    ) {

                                        Text(
                                            text = "Tap to flip",
                                            fontSize = 12.sp,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant,
                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 14.dp,
                                                    vertical = 7.dp
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        /*
                         * Previous / Next
                         */
                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            OutlinedButton(
                                onClick = {

                                    if (
                                        currentIndex > 0
                                    ) {

                                        currentIndex--

                                        isAnswerVisible =
                                            false
                                    }
                                },
                                enabled =
                                    currentIndex > 0,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                shape =
                                    RoundedCornerShape(16.dp)
                            ) {

                                Text(
                                    text = "Previous",
                                    fontWeight =
                                        FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {

                                    if (
                                        currentIndex <
                                        flashcards.size - 1
                                    ) {

                                        currentIndex++

                                        isAnswerVisible =
                                            false
                                    }
                                },
                                enabled =
                                    currentIndex <
                                            flashcards.size - 1,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                shape =
                                    RoundedCornerShape(16.dp)
                            ) {

                                Text(
                                    text = "Next",
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }
                }
            }
        }
    }
}


/*
 * Loading UI
 */
@Composable
fun LoadingContent(
    text: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(42.dp)
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}


/*
 * Error UI
 */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "Something went wrong",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = message,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onRetry,
            shape =
                RoundedCornerShape(16.dp)
        ) {

            Icon(
                imageVector =
                    Icons.Default.Refresh,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(6.dp)
            )

            Text(
                text = "Try Again"
            )
        }
    }
}


/*
 * Empty UI
 */
@Composable
fun EmptyContent() {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "No notes found",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                "Add some notes to this subject first.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}