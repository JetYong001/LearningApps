package com.example.project.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.project.navigation.Screen
import com.example.project.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val profile by viewModel.profile.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current

    var popupMessage by remember {
        mutableStateOf<String?>(null)
    }

    var imageVersion by remember {
        mutableLongStateOf(
            System.currentTimeMillis()
        )
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {

                imageVersion =
                    System.currentTimeMillis()

                viewModel.updateProfilePicture(
                    context,
                    uri
                )
            }
        }

    val primary =
        MaterialTheme.colorScheme.primary

    val background =
        MaterialTheme.colorScheme.background

    val surface =
        MaterialTheme.colorScheme.surface

    val surfaceVariant =
        MaterialTheme.colorScheme.surfaceVariant

    val onSurface =
        MaterialTheme.colorScheme.onSurface

    val onSurfaceVariant =
        MaterialTheme.colorScheme.onSurfaceVariant

    val onPrimary =
        MaterialTheme.colorScheme.onPrimary

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(background)
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.ArrowBack,
                        contentDescription =
                            "Back",
                        tint =
                            onSurface
                    )
                }

                Text(
                    text = "Profile",
                    style =
                        MaterialTheme.typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        onSurface
                )
            }

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            if (profile == null) {

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(500.dp),
                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color =
                            primary
                    )
                }

            } else {

                Column(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier =
                            Modifier.size(142.dp),
                        contentAlignment =
                            Alignment.BottomEnd
                    ) {

                        Box(
                            modifier =
                                Modifier
                                    .size(136.dp)
                                    .clip(CircleShape)
                                    .background(
                                        primary.copy(
                                            alpha =
                                                0.10f
                                        )
                                    )
                                    .clickable(
                                        enabled =
                                            !isUpdating
                                    ) {
                                        imagePickerLauncher
                                            .launch(
                                                "image/*"
                                            )
                                    },
                            contentAlignment =
                                Alignment.Center
                        ) {

                            val profilePicture =
                                profile!!
                                    .profile_picture

                            if (
                                !profilePicture
                                    .isNullOrBlank()
                            ) {

                                val imageUrl =
                                    if (
                                        profilePicture
                                            .contains("?")
                                    ) {
                                        "$profilePicture&v=$imageVersion"
                                    } else {
                                        "$profilePicture?v=$imageVersion"
                                    }

                                AsyncImage(
                                    model =
                                        imageUrl,
                                    contentDescription =
                                        "Profile Picture",
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .clip(
                                                CircleShape
                                            ),
                                    contentScale =
                                        ContentScale.Crop
                                )

                            } else {

                                Icon(
                                    imageVector =
                                        Icons.Default.Person,
                                    contentDescription =
                                        "Profile Picture",
                                    modifier =
                                        Modifier.size(
                                            64.dp
                                        ),
                                    tint =
                                        primary
                                )
                            }

                            if (isUpdating) {

                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .background(
                                                primary.copy(
                                                    alpha =
                                                        0.15f
                                                )
                                            ),
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    CircularProgressIndicator(
                                        modifier =
                                            Modifier.size(
                                                42.dp
                                            ),
                                        color =
                                            primary,
                                        strokeWidth =
                                            4.dp
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier =
                                Modifier
                                    .size(42.dp)
                                    .clickable(
                                        enabled =
                                            !isUpdating
                                    ) {
                                        imagePickerLauncher
                                            .launch(
                                                "image/*"
                                            )
                                    },
                            shape =
                                CircleShape,
                            color =
                                primary,
                            shadowElevation =
                                4.dp
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Edit,
                                    contentDescription =
                                        "Change Picture",
                                    modifier =
                                        Modifier.size(
                                            20.dp
                                        ),
                                    tint =
                                        onPrimary
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Text(
                        text =
                            profile!!.username,
                        style =
                            MaterialTheme.typography
                                .headlineSmall,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            onSurface
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "Tap your photo to change it",
                        style =
                            MaterialTheme.typography
                                .bodySmall,
                        color =
                            onSurfaceVariant
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(34.dp)
                )

                Text(
                    text =
                        "Profile Information",
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        onSurface
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(22.dp),
                    color =
                        surface,
                    tonalElevation =
                        2.dp
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 18.dp,
                                    vertical = 18.dp
                                ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier =
                                Modifier.size(44.dp),
                            shape =
                                RoundedCornerShape(
                                    14.dp
                                ),
                            color =
                                primary.copy(
                                    alpha = 0.10f
                                )
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Person,
                                    contentDescription =
                                        null,
                                    tint =
                                        primary
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.width(14.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "Username",
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelMedium,
                                color =
                                    onSurfaceVariant
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    profile!!.username,
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyLarge,
                                fontWeight =
                                    FontWeight.SemiBold,
                                color =
                                    onSurface
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(28.dp)
                )

                Text(
                    text = "Account",
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        onSurface
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {

                                navController.navigate(
                                    Screen.EditProfile.route
                                )
                            },
                    shape =
                        RoundedCornerShape(20.dp),
                    color =
                        surface
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 18.dp,
                                    vertical = 17.dp
                                ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier =
                                Modifier.size(44.dp),
                            shape =
                                RoundedCornerShape(
                                    14.dp
                                ),
                            color =
                                primary.copy(
                                    alpha = 0.10f
                                )
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Edit,
                                    contentDescription =
                                        null,
                                    tint =
                                        primary
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.width(14.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "Edit Username",
                                fontWeight =
                                    FontWeight.SemiBold,
                                color =
                                    onSurface,
                                fontSize =
                                    16.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(3.dp)
                            )

                            Text(
                                text =
                                    "Update your display name",
                                fontSize =
                                    12.sp,
                                color =
                                    onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector =
                                Icons.Default.ChevronRight,
                            contentDescription =
                                null,
                            tint =
                                onSurfaceVariant
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {

                                navController.navigate(
                                    Screen.ChangePassword.route
                                )
                            },
                    shape =
                        RoundedCornerShape(20.dp),
                    color =
                        surface
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 18.dp,
                                    vertical = 17.dp
                                ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier =
                                Modifier.size(44.dp),
                            shape =
                                RoundedCornerShape(
                                    14.dp
                                ),
                            color =
                                surfaceVariant
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Lock,
                                    contentDescription =
                                        null,
                                    tint =
                                        onSurface
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.width(14.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "Change Password",
                                fontWeight =
                                    FontWeight.SemiBold,
                                color =
                                    onSurface,
                                fontSize =
                                    16.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(3.dp)
                            )

                            Text(
                                text =
                                    "Keep your account secure",
                                fontSize =
                                    12.sp,
                                color =
                                    onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector =
                                Icons.Default.ChevronRight,
                            contentDescription =
                                null,
                            tint =
                                onSurfaceVariant
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )
            }
        }

        popupMessage?.let { message ->

            LaunchedEffect(message) {
                delay(2500)
                popupMessage = null
            }

            Surface(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 20.dp
                        ),
                shape =
                    RoundedCornerShape(18.dp),
                color =
                    primary,
                tonalElevation =
                    6.dp
            ) {

                Row(
                    modifier =
                        Modifier.padding(
                            horizontal = 18.dp,
                            vertical = 14.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "✓",
                        color =
                            onPrimary,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.width(10.dp)
                    )

                    Text(
                        text =
                            message,
                        color =
                            onPrimary,
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
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
                Text(
                    text = "Notice",
                    fontWeight =
                        FontWeight.Bold
                )
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