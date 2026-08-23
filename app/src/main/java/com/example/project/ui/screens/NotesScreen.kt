package com.example.project.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import com.example.project.model.Note
import com.example.project.navigation.Screen
import com.example.project.viewmodel.NotesViewModel
import com.example.project.viewmodel.SubjectUiModel

@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NotesViewModel
) {
    val context = LocalContext.current

    var showCreateSubjectDialog by remember {
        mutableStateOf(false)
    }

    var newSubjectName by remember {
        mutableStateOf("")
    }

    var subjectToDelete by remember {
        mutableStateOf<String?>(null)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    val subjects by viewModel.subjects.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData(context)
    }

    val filteredSubjects = remember(
        subjects,
        searchText
    ) {
        if (searchText.isBlank()) {
            subjects
        } else {
            subjects.mapNotNull { subject ->

                val subjectMatches =
                    subject.name.contains(
                        searchText,
                        ignoreCase = true
                    )

                val filteredNotes =
                    subject.notes.filter { note ->
                        note.title.contains(
                            searchText,
                            ignoreCase = true
                        )
                    }

                when {
                    subjectMatches -> subject

                    filteredNotes.isNotEmpty() ->
                        subject.copy(
                            notes = filteredNotes
                        )

                    else -> null
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
                    .clip(
                        RoundedCornerShape(50.dp)
                    )
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
                    .padding(
                        vertical = 12.dp
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Notes",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    placeholder = {
                        Text("Search notes...")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchText = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor =
                            MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor =
                            MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Button(
                    onClick = {
                        showCreateSubjectDialog = true
                    },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            MaterialTheme.colorScheme.primary,
                        contentColor =
                            MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp
                    )
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Subject",
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(4.dp)
                    )

                    Text(
                        text = "Create Subject",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (
                searchText.isNotBlank() &&
                filteredSubjects.isEmpty()
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Text(
                            text = "No notes found",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Try another search",
                            fontSize = 13.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 4.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    items(
                        items = filteredSubjects,
                        key = {
                            it.name
                        }
                    ) { subject ->

                        SubjectSection(
                            subject = subject,
                            onNoteClick = { note ->
                                navController.navigate(
                                    Screen.NoteView.createRoute(
                                        note.id
                                    )
                                )
                            },
                            onDeleteSubject = {
                                subjectToDelete =
                                    subject.name
                            },
                            onGenerateFlashcards = {
                                navController.navigate(
                                    "flashcards_screen/${subject.name}"
                                )
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {

                if (subjects.isNotEmpty()) {

                    navController.navigate(
                        Screen.NoteDetail.route
                    )

                } else {

                    Toast.makeText(
                        context,
                        "Please create a subject first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp),
            shape = CircleShape,
            containerColor =
                if (subjects.isNotEmpty()) {
                    Color(0xFFFFD166)
                } else {
                    Color(0xFFD0D0D0)
                },
            contentColor =
                if (subjects.isNotEmpty()) {
                    Color(0xFF5A4612)
                } else {
                    Color(0xFF888888)
                }
        ) {

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Note"
            )
        }
    }

    if (showCreateSubjectDialog) {

        AlertDialog(
            onDismissRequest = {
                showCreateSubjectDialog = false
            },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Create New Subject",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {

                OutlinedTextField(
                    value = newSubjectName,
                    onValueChange = {
                        newSubjectName = it
                    },
                    label = {
                        Text("Subject Name")
                    },
                    placeholder = {
                        Text("Enter subject name")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        if (newSubjectName.isNotBlank()) {

                            viewModel.createSubject(
                                newSubjectName,
                                context
                            )

                            newSubjectName = ""

                            showCreateSubjectDialog = false
                        }
                    }
                ) {

                    Text(
                        text = "Create",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {

                        showCreateSubjectDialog = false
                        newSubjectName = ""
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    if (subjectToDelete != null) {

        AlertDialog(
            onDismissRequest = {
                subjectToDelete = null
            },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Delete Subject",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text =
                        "Are you sure you want to delete this subject and all its notes? This action cannot be undone."
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel.deleteSubject(
                            subjectToDelete!!,
                            context
                        )

                        subjectToDelete = null
                    }
                ) {

                    Text(
                        text = "Delete",
                        color =
                            MaterialTheme
                                .colorScheme
                                .error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        subjectToDelete = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SubjectSection(
    subject: SubjectUiModel,
    onNoteClick: (Note) -> Unit,
    onDeleteSubject: () -> Unit,
    onGenerateFlashcards: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(
                vertical = 10.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = subject.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text =
                            "${subject.notes.size} notes",
                        fontSize = 12.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick =
                            onGenerateFlashcards
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.AutoAwesome,
                            contentDescription =
                                "Generate Flashcards",
                            tint =
                                Color(0xFF9B7EDE)
                        )
                    }

                    IconButton(
                        onClick =
                            onDeleteSubject
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Delete,
                            contentDescription =
                                "Delete Subject",
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            LazyRow(
                contentPadding =
                    PaddingValues(
                        horizontal = 14.dp
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = subject.notes,
                    key = {
                        it.id
                    }
                ) { note ->

                    NoteCard(
                        note = note,
                        onCardClick = {
                            onNoteClick(note)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onCardClick: () -> Unit
) {

    val backgroundColor = remember(
        note.color
    ) {

        try {
            Color(
                note.color.toColorInt()
            )
        } catch (e: Exception) {
            Color(0xFFFFE5B4)
        }
    }

    Card(
        modifier = Modifier
            .width(158.dp)
            .height(158.dp)
            .clickable {
                onCardClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(5.dp)
                    .clip(
                        RoundedCornerShape(50.dp)
                    )
                    .background(
                        Color.Black.copy(
                            alpha = 0.12f
                        )
                    )
                    .align(
                        Alignment.TopStart
                    )
            )

            Text(
                text = note.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF332E2E),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .padding(
                        top = 18.dp
                    )
            )
        }
    }
}