package com.example.project.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.project.navigation.Screen
import com.example.project.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current

    var popupMessage by remember {
        mutableStateOf<String?>(null)
    }

    var imageVersion by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                imageVersion = System.currentTimeMillis()

                viewModel.updateProfilePicture(
                    context,
                    uri
                )
            }
        }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("username_updated")
    ) {
        val updated =
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>("username_updated")

        if (updated == true) {
            popupMessage = "Username updated successfully!"

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Boolean>("username_updated")

            delay(2500)

            popupMessage = null
        }
    }

    LaunchedEffect(
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("password_changed")
    ) {
        val changed =
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>("password_changed")

        if (changed == true) {
            popupMessage = "Password changed successfully!"

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Boolean>("password_changed")

            delay(2500)

            popupMessage = null
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
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
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    color = onSurface
                )
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            if (profile == null) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = primary
                    )
                }

            } else {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(132.dp)
                            .clickable(
                                enabled = !isUpdating
                            ) {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.BottomEnd
                    ) {

                        Box(
                            modifier = Modifier
                                .size(132.dp)
                                .clip(CircleShape)
                                .background(
                                    primary.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            val profilePicture =
                                profile!!.profile_picture

                            if (!profilePicture.isNullOrBlank()) {

                                val imageUrl =
                                    if (profilePicture.contains("?")) {
                                        "$profilePicture&v=$imageVersion"
                                    } else {
                                        "$profilePicture?v=$imageVersion"
                                    }

                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                            } else {

                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(68.dp),
                                    tint = primary
                                )
                            }

                            if (isUpdating) {

                                CircularProgressIndicator(
                                    modifier = Modifier.size(42.dp),
                                    color = primary,
                                    strokeWidth = 4.dp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(primary),
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Picture",
                                modifier = Modifier.size(20.dp),
                                tint = onPrimary
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = profile!!.username,
                        style = MaterialTheme.typography.headlineSmall,
                        color = onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Tap your photo to change it",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Text(
                    text = "Profile Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurface
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
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
                            style = MaterialTheme.typography.labelMedium,
                            color = onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = profile!!.username,
                            style = MaterialTheme.typography.bodyLarge,
                            color = onSurface
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Button(
                    onClick = {
                        navController.navigate(
                            Screen.EditProfile.route
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text("Edit Username")
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedButton(
                    onClick = {
                        navController.navigate(
                            Screen.ChangePassword.route
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text("Change Password")
                }
            }
        }

        popupMessage?.let { message ->

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                color = primary,
                tonalElevation = 6.dp
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 14.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "✓",
                        color = onPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        text = message,
                        color = onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    errorMessage?.let { message ->

        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
            },
            title = {
                Text("Notice")
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