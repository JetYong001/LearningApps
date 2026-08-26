package com.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    noteId: String?
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val subjects by viewModel.subjects.collectAsState()

    val isNewNote =
        noteId.isNullOrBlank() || noteId == "new"

    LaunchedEffect(noteId) {
        if (!isNewNote) {
            val note = viewModel.getNoteById(noteId!!)

            if (note != null) {
                title = note.title
                subject = note.subjectName
                content = note.content
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isNewNote) "New Note" else "Edit Note",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isNewNote) {
                        "Create something new"
                    } else {
                        "Update your note"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledIconButton(
                onClick = {
                    if (
                        title.isNotBlank() &&
                        subject.isNotBlank()
                    ) {
                        viewModel.saveNote(
                            noteId = noteId,
                            subjectName = subject,
                            title = title,
                            content = content,
                            onSuccess = {
                                navController.popBackStack()
                            }
                        )
                    }
                },
                enabled = title.isNotBlank() && subject.isNotBlank(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "Title",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        TextField(
            value = title,
            onValueChange = {
                title = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Enter note title",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.35f
                ),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.35f
                ),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0f
                )
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Subject",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (subjects.isNotEmpty()) {
                    expanded = !expanded
                }
            }
        ) {
            TextField(
                value = subject,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                placeholder = {
                    Text(
                        text = "Select a subject",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Subject"
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.35f
                    ),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.35f
                    ),
                    focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0f
                    ),
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0f
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                subjects.forEach { subjectItem ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = subjectItem.name,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        onClick = {
                            subject = subjectItem.name
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Content",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        TextField(
            value = content,
            onValueChange = {
                content = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = {
                Text(
                    text = "Write your note here...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                lineHeight = 25.sp
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.35f
                ),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.35f
                ),
                focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0f
                ),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0f
                )
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )
    }
}