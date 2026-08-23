package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.project.viewmodel.ChangePasswordViewModel

@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = viewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.loadCurrentEmail()
    }

    val background =
        MaterialTheme.colorScheme.background

    val surface =
        MaterialTheme.colorScheme.surface

    val primary =
        MaterialTheme.colorScheme.primary

    val onSurface =
        MaterialTheme.colorScheme.onSurface

    val onSurfaceVariant =
        MaterialTheme.colorScheme.onSurfaceVariant

    val onPrimary =
        MaterialTheme.colorScheme.onPrimary

    val errorMessage =
        viewModel.errorMessage

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
                text = "Change Password",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = onSurface
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Box(
            modifier = Modifier
                .size(68.dp)
                .background(
                    primary.copy(alpha = 0.12f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Secure your account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = onSurface
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Verify your email first, then create a new password.",
            fontSize = 14.sp,
            color = onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(28.dp)
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            if (viewModel.otpVerified)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.Email,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            text =
                                if (viewModel.otpVerified)
                                    "Email Verified"
                                else
                                    "Email Verification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = onSurface
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text = viewModel.email,
                            fontSize = 13.sp,
                            color = onSurfaceVariant
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                if (!viewModel.otpVerified) {

                    Button(
                        onClick = {
                            viewModel.sendOtp()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled =
                            !viewModel.isSendingOtp &&
                                    viewModel.otpCooldown == 0,
                        shape = RoundedCornerShape(16.dp)
                    ) {

                        if (viewModel.isSendingOtp) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(21.dp),
                                color = onPrimary,
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text(
                                if (viewModel.otpCooldown > 0)
                                    "Resend in ${viewModel.otpCooldown}s"
                                else
                                    "Send OTP"
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.otpCode,
                        onValueChange = {
                            if (it.length <= 6) {
                                viewModel.otpCode = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("OTP Code")
                        },
                        placeholder = {
                            Text("Enter 6-digit OTP")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.verifyOtp()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled =
                            !viewModel.isVerifyingOtp,
                        shape = RoundedCornerShape(16.dp)
                    ) {

                        if (viewModel.isVerifyingOtp) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(21.dp),
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text("Verify OTP")
                        }
                    }

                } else {

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = primary.copy(alpha = 0.10f)
                    ) {

                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = primary
                            )

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            Text(
                                text =
                                    "Your email has been verified.",
                                color = onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        if (viewModel.otpVerified) {

            Spacer(
                modifier = Modifier.height(20.dp)
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

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Lock,
                            contentDescription = null,
                            tint = primary
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Text(
                            text = "Create New Password",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.newPassword,
                        onValueChange = {
                            viewModel.newPassword = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("New Password")
                        },
                        placeholder = {
                            Text("At least 6 characters")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        visualTransformation =
                            PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = {
                            viewModel.confirmPassword = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Confirm Password")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        visualTransformation =
                            PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Button(
                        onClick = {

                            viewModel.changePassword {

                                navController
                                    .previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(
                                        "password_changed",
                                        true
                                    )

                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled =
                            !viewModel.isChangingPassword,
                        shape = RoundedCornerShape(18.dp)
                    ) {

                        if (viewModel.isChangingPassword) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = onPrimary,
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text(
                                text = "Update Password",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text =
                "A verification code will be sent to your registered email address.",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 12.sp,
            color = onSurfaceVariant
        )
    }

    errorMessage?.let { message ->

        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
            },
            title = {
                Text(
                    text = "Notice",
                    fontWeight = FontWeight.Bold
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