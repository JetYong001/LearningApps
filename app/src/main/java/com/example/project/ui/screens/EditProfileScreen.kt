package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val profile by viewModel.profile.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var validationError by remember {
        mutableStateOf<String?>(null)
    }

    var username by remember {
        mutableStateOf("")
    }

    LaunchedEffect(profile?.username) {
        profile?.username?.let {
            username = it
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

    val onPrimary =
        MaterialTheme.colorScheme.onPrimary

    val outline =
        MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(
                horizontal = 20.dp,
                vertical = 14.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = surface
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
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column {

                Text(
                    text = "Edit Profile",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )

                Text(
                    text = "Update your personal information",
                    fontSize = 12.sp,
                    color = onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Surface(
            modifier = Modifier
                .size(82.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(24.dp),
            color = primary.copy(alpha = 0.12f)
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        Text(
            text = "Change your username",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = onSurface
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Text(
            text = "Your username is shown throughout the app.",
            fontSize = 14.sp,
            color = onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = surface,
            tonalElevation = 2.dp
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Username",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurface
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
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
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            focusedLabelColor = primary,
                            cursorColor = primary,
                            unfocusedBorderColor = outline
                        )
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "${username.length} characters",
                    fontSize = 11.sp,
                    color = onSurfaceVariant,
                    modifier = Modifier.align(
                        Alignment.End
                    )
                )
            }
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = primary.copy(alpha = 0.08f)
        ) {

            Text(
                text =
                    "Use at least 3 characters for your username.",
                modifier = Modifier.padding(14.dp),
                fontSize = 12.sp,
                color = onSurfaceVariant
            )
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = {

                val cleanUsername =
                    username.trim()

                when {

                    cleanUsername.isEmpty() -> {
                        validationError =
                            "Username cannot be empty"
                    }

                    cleanUsername.length < 3 -> {
                        validationError =
                            "Username must be at least 3 characters"
                    }

                    cleanUsername == profile?.username -> {
                        validationError =
                            "Please enter a different username"
                    }

                    else -> {
                        validationError = null

                        viewModel.updateUsername(
                            username = cleanUsername,
                            onSuccess = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            enabled = !isUpdating,
            shape = RoundedCornerShape(18.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = onPrimary
                )
        ) {

            if (isUpdating) {

                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = onPrimary,
                    strokeWidth = 2.5.dp
                )

            } else {

                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Save Changes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )
    }

    validationError?.let { message ->

        AlertDialog(
            onDismissRequest = {
                validationError = null
            },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Invalid Username",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(message)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        validationError = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}