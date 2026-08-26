package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    val errorMessage = viewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(
            modifier = Modifier.height(12.dp)
        )

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
                    text = "Security",
                    fontSize = 13.sp,
                    color = onSurfaceVariant
                )

                Text(
                    text = "Change Password",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )
            }
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.size(86.dp),
                shape = RoundedCornerShape(28.dp),
                color = primary.copy(alpha = 0.10f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = primary
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "Secure your account",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = onSurface
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Verify your email and create a new password.",
                fontSize = 13.sp,
                color = onSurfaceVariant
            )
        }

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicator(
                number = "1",
                active = !viewModel.otpVerified,
                completed = viewModel.otpVerified,
                primary = primary
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Verify your email",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )

                Text(
                    text = "Confirm your identity with an OTP code.",
                    fontSize = 12.sp,
                    color = onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier.height(14.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = primary.copy(alpha = 0.10f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = primary
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Registered email",
                            fontSize = 12.sp,
                            color = onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text = viewModel.email.ifBlank {
                                "Loading..."
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurface
                        )
                    }
                }

                if (!viewModel.otpVerified) {
                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

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
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary,
                            contentColor = onPrimary
                        )
                    ) {
                        if (viewModel.isSendingOtp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text(
                                text =
                                    if (viewModel.otpCooldown > 0) {
                                        "Resend in ${viewModel.otpCooldown}s"
                                    } else {
                                        "Send Verification Code"
                                    },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.otpCode,
                        onValueChange = {
                            if (
                                it.length <= 6 &&
                                it.all(Char::isDigit)
                            ) {
                                viewModel.otpCode = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Verification Code")
                        },
                        placeholder = {
                            Text("Enter 6-digit code")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            focusedLabelColor = primary,
                            cursorColor = primary
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.verifyOtp()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled =
                            !viewModel.isVerifyingOtp,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary,
                            contentColor = onPrimary
                        )
                    ) {
                        if (viewModel.isVerifyingOtp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Verify Code",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = primary.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = primary
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            Column {
                                Text(
                                    text = "Email verified",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurface
                                )

                                Text(
                                    text = "You can continue to the next step.",
                                    fontSize = 12.sp,
                                    color = onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicator(
                number = "2",
                active = viewModel.otpVerified,
                completed = false,
                primary = primary
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Create new password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )

                Text(
                    text = "Choose a secure password for your account.",
                    fontSize = 12.sp,
                    color = onSurfaceVariant
                )
            }
        }

        if (viewModel.otpVerified) {
            Spacer(
                modifier = Modifier.height(14.dp)
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
                    PasswordField(
                        value = viewModel.newPassword,
                        onValueChange = {
                            viewModel.newPassword = it
                        },
                        label = "New Password",
                        placeholder = "At least 6 characters",
                        primary = primary,
                        onSurface = onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    PasswordField(
                        value = viewModel.confirmPassword,
                        onValueChange = {
                            viewModel.confirmPassword = it
                        },
                        label = "Confirm Password",
                        placeholder = "Re-enter your password",
                        primary = primary,
                        onSurface = onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
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
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary,
                            contentColor = onPrimary
                        )
                    ) {
                        if (viewModel.isChangingPassword) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(21.dp),
                                color = onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text(
                                text = "Update Password",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = surfaceVariant.copy(alpha = 0.45f)
            ) {
                Text(
                    text =
                        "Complete email verification to unlock password settings.",
                    modifier = Modifier.padding(14.dp),
                    fontSize = 12.sp,
                    color = onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )
    }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
            },
            shape = RoundedCornerShape(24.dp),
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

@Composable
private fun StepIndicator(
    number: String,
    active: Boolean,
    completed: Boolean,
    primary: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.size(34.dp),
        shape = RoundedCornerShape(12.dp),
        color =
            when {
                completed -> primary
                active -> primary.copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(19.dp)
                )
            } else {
                Text(
                    text = number,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (active) {
                            primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                )
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    primary: androidx.compose.ui.graphics.Color,
    onSurface: androidx.compose.ui.graphics.Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(label)
        },
        placeholder = {
            Text(placeholder)
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
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primary,
            focusedLabelColor = primary,
            cursorColor = primary
        )
    )
}