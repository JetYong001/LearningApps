package com.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    viewModel: NotesViewModel,
    noteId: String? = null
) {
    var subjectName by remember {
        mutableStateOf("")
    }

    var title by remember {
        mutableStateOf("")
    }

    var content by remember {
        mutableStateOf("")
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    val subjects by viewModel.subjects.collectAsState()

    val existingSubjects = remember(subjects) {
        subjects.map {
            it.name
        }
    }

    LaunchedEffect(noteId) {

        if (noteId != null) {

            val noteData =
                viewModel.getNoteAndSubject(noteId)

            if (noteData != null) {

                subjectName = noteData.second

                title = noteData.first.title

                content = noteData.first.content
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Back Button
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                // Screen Title
                Text(
                    text = if (noteId == null) {
                        "New Note"
                    } else {
                        "Edit Note"
                    },
                    modifier = Modifier.weight(1f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Save Button
                IconButton(
                    onClick = {

                        if (
                            subjectName.isNotBlank() &&
                            title.isNotBlank()
                        ) {

                            viewModel.saveNote(
                                noteId,
                                subjectName,
                                title,
                                content
                            )

                            navController.popBackStack()
                        }
                    },

                    enabled =
                        subjectName.isNotBlank() &&
                                title.isNotBlank()
                ) {

                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
                    )
                }
            }

            // Note Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                // Subject
                ExposedDropdownMenuBox(
                    expanded = expanded && existingSubjects.isNotEmpty(),
                    onExpandedChange = {
                        expanded = it
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = {
                            subjectName = it
                        },
                        readOnly = true,
                        label = {
                            Text("Subject")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    if (existingSubjects.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            existingSubjects.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = {
                                        Text(suggestion)
                                    },
                                    onClick = {
                                        subjectName = suggestion
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,

                    onValueChange = {
                        title = it
                    },

                    label = {
                        Text("Title")
                    },

                    singleLine = true,

                    modifier = Modifier.fillMaxWidth(),

                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Content
                OutlinedTextField(
                    value = content,

                    onValueChange = {
                        content = it
                    },

                    label = {
                        Text("Content")
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    colors =
                        OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor =
                                Color.Transparent,

                            focusedBorderColor =
                                Color.Transparent
                        )
                )
            }
        }
    }
}