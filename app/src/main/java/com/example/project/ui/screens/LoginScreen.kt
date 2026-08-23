package com.example.project.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onAuthSuccess: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var useOtpMode by remember {
        mutableStateOf(false)
    }

    var isOtpSent by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.25f
                        ),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(
                    horizontal = 28.dp,
                    vertical = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = when {
                        useOtpMode ->
                            "OTP Verification"

                        viewModel.isSignUp ->
                            "Create Account"

                        else ->
                            "Welcome"
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = when {
                        useOtpMode ->
                            "Enter your registered email to receive a 6-digit verification code."

                        viewModel.isSignUp ->
                            "Sign up with your email and password to create your account."

                        else ->
                            "Sign in to continue to your workspace."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = {
                        viewModel.email = it
                    },
                    label = {
                        Text("Email Address")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = if (
                            useOtpMode
                        ) {
                            ImeAction.Done
                        } else {
                            ImeAction.Next
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                if (!useOtpMode) {

                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = {
                            viewModel.password = it
                        },
                        label = {
                            Text("Password")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {

                            IconButton(
                                onClick = {
                                    viewModel.isPasswordVisible =
                                        !viewModel.isPasswordVisible
                                }
                            ) {

                                Icon(
                                    imageVector =
                                        if (
                                            viewModel.isPasswordVisible
                                        ) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation =
                            if (
                                viewModel.isPasswordVisible
                            ) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.authenticate(
                                    onAuthSuccess
                                )
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                } else {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        OutlinedTextField(
                            value = viewModel.otpCode,
                            onValueChange = { value ->

                                if (
                                    value.length <= 6 &&
                                    value.all {
                                        it.isDigit()
                                    }
                                ) {
                                    viewModel.otpCode = value
                                }
                            },
                            label = {
                                Text("6-Digit OTP")
                            },
                            placeholder = {
                                Text("Enter 6-digit code")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Pin,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.verifyOtp(
                                        onAuthSuccess
                                    )
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.sendOtp()
                                isOtpSent = true
                            },
                            enabled =
                                !viewModel.isSendingOtp &&
                                        viewModel.email.isNotBlank() &&
                                        viewModel.otpCooldown == 0,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(56.dp)
                        ) {

                            if (
                                viewModel.isSendingOtp
                            ) {

                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )

                            } else {

                                Text(
                                    text = when {

                                        viewModel.otpCooldown > 0 ->
                                            "Resend (${viewModel.otpCooldown}s)"

                                        isOtpSent ->
                                            "Resend OTP"

                                        else ->
                                            "Send OTP"
                                    },
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible =
                        viewModel.successMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {

                    viewModel.successMessage?.let { message ->

                        Column {

                            Spacer(
                                modifier = Modifier.height(20.dp)
                            )

                            Surface(
                                color = MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                                    .copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    text = message,
                                    color = MaterialTheme
                                        .colorScheme
                                        .onPrimaryContainer,
                                    style = MaterialTheme
                                        .typography
                                        .bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 14.dp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(36.dp)
                )

                Button(
                    onClick = {

                        focusManager.clearFocus()

                        if (useOtpMode) {

                            viewModel.verifyOtp(
                                onAuthSuccess
                            )

                        } else {

                            viewModel.authenticate(
                                onAuthSuccess
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !viewModel.isLoading,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    if (viewModel.isLoading) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )

                    } else {

                        Text(
                            text = when {

                                useOtpMode ->
                                    "Verify & Log In"

                                viewModel.isSignUp ->
                                    "Sign Up"

                                else ->
                                    "Log In"
                            },
                            style = MaterialTheme.typography
                                .titleMedium
                                .copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                if (!viewModel.isSignUp) {

                    TextButton(
                        onClick = {

                            useOtpMode =
                                !useOtpMode

                            isOtpSent = false

                            viewModel.errorMessage = null
                            viewModel.successMessage = null
                            viewModel.otpCode = ""
                        }
                    ) {

                        Text(
                            text =
                                if (useOtpMode) {
                                    "Use Password Instead"
                                } else {
                                    "Log In with OTP Code"
                                },
                            style = MaterialTheme.typography
                                .bodyMedium
                                .copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp,
                        bottom = 12.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (!useOtpMode) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text =
                                if (
                                    viewModel.isSignUp
                                ) {
                                    "Already have an account?"
                                } else {
                                    "Don't have an account?"
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        )

                        TextButton(
                            onClick = {

                                viewModel.isSignUp =
                                    !viewModel.isSignUp

                                useOtpMode = false
                                isOtpSent = false

                                viewModel.errorMessage = null
                                viewModel.successMessage = null
                                viewModel.password = ""
                                viewModel.otpCode = ""
                            }
                        ) {

                            Text(
                                text =
                                    if (
                                        viewModel.isSignUp
                                    ) {
                                        "Log In"
                                    } else {
                                        "Sign Up"
                                    },
                                style = MaterialTheme.typography
                                    .bodyMedium
                                    .copy(
                                        fontWeight = FontWeight.Bold
                                    )
                            )
                        }
                    }
                }
            }
        }

        if (
            viewModel.errorMessage != null
        ) {

            AlertDialog(
                onDismissRequest = {
                    viewModel.errorMessage = null
                },
                title = {

                    Text(
                        text = "Authentication Failed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {

                    Text(
                        text = formatFriendlyError(
                            viewModel.errorMessage
                        ),
                        fontSize = 15.sp,
                        color = MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    )
                },
                confirmButton = {

                    TextButton(
                        onClick = {
                            viewModel.errorMessage = null
                        }
                    ) {

                        Text(
                            text = "OK",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme
                    .colorScheme
                    .surface,
                tonalElevation = 6.dp
            )
        }
    }
}

private fun formatFriendlyError(
    rawError: String?
): String {

    if (rawError.isNullOrBlank()) {
        return "An error occurred. Please try again."
    }

    val error =
        rawError.lowercase()

    return when {

        error.contains(
            "invalid login credentials"
        ) ||
                error.contains(
                    "invalid_credentials"
                ) ->
            "Incorrect email or password."

        error.contains(
            "invalid token"
        ) ||
                error.contains(
                    "otp_expired"
                ) ||
                error.contains(
                    "token has expired"
                ) ->
            "Invalid or expired OTP code."

        error.contains(
            "email not confirmed"
        ) ->
            "Please verify your email address before logging in."

        error.contains(
            "user already registered"
        ) ||
                error.contains(
                    "already_exists"
                ) ->
            "This email address is already registered."

        error.contains(
            "rate limit"
        ) ||
                error.contains(
                    "rate_limit"
                ) ->
            "Too many requests. Please try again later."

        error.contains(
            "disabled"
        ) ->
            "Email provider or OTP is disabled in Supabase."

        error.contains(
            "password should be"
        ) ||
                error.contains(
                    "password_length"
                ) ->
            "Password must be at least 6 characters long."

        error.contains(
            "invalid email"
        ) ||
                error.contains(
                    "email_format"
                ) ->
            "Please enter a valid email address."

        error.contains(
            "unexpected failure error sending"
        ) ||
                error.contains(
                    "error sending confirmation email"
                ) ->
            "Failed to send confirmation email. Please check your Supabase email settings."

        error.contains(
            "network"
        ) ||
                error.contains(
                    "connect"
                ) ->
            "Network error. Please check your internet connection."

        else ->
            rawError
    }
}