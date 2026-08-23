package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.project.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {

    val profile by viewModel.profile.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var username by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(profile) {

        profile?.let {

            if (username.isEmpty()) {
                username = it.username
            }
        }
    }

    val primary =
        MaterialTheme.colorScheme.primary

    val background =
        MaterialTheme.colorScheme.background

    val surface =
        MaterialTheme.colorScheme.surface

    val onSurface =
        MaterialTheme.colorScheme.onSurface

    val onSurfaceVariant =
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = onSurface
                )
            }

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Text(
                text = "Edit Username",
                style = MaterialTheme.typography.titleLarge,
                color = onSurface
            )
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    primary.copy(alpha = 0.12f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Change your username",
            style = MaterialTheme.typography.headlineSmall,
            color = onSurface
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Your username will be displayed across the app.",
            style = MaterialTheme.typography.bodyMedium,
            color = onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {

                Text(
                    text = "Username",
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurface
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Username")
                    },
                    placeholder = {
                        Text("Enter username")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = {

                val cleanUsername =
                    username.trim()

                when {

                    cleanUsername.isEmpty() -> {

                        viewModel.clearError()

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "edit_error",
                                "Username cannot be empty"
                            )
                    }

                    cleanUsername.length < 3 -> {

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "edit_error",
                                "Username must be at least 3 characters"
                            )
                    }

                    cleanUsername == profile?.username -> {

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "edit_error",
                                "Please enter a different username"
                            )
                    }

                    else -> {

                        viewModel.updateUsername(
                            username = cleanUsername,
                            onSuccess = {

                                navController
                                    .previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(
                                        "username_updated",
                                        true
                                    )

                                navController.popBackStack()
                            }
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isUpdating,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary
            )
        ) {

            if (isUpdating) {

                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )

            } else {

                Text("Save Changes")
            }
        }
    }

    errorMessage?.let { message ->

        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
            },
            title = {
                Text("Update Failed")
            },
            text = {
                Text(message)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearError()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}