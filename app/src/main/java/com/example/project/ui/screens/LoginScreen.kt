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
    val focusManager =
        LocalFocusManager.current

    val scrollState =
        rememberScrollState()

    var useOtpMode by remember {
        mutableStateOf(false)
    }

    val primary =
        MaterialTheme.colorScheme.primary

    val background =
        MaterialTheme.colorScheme.background

    val onBackground =
        MaterialTheme.colorScheme.onBackground

    val onSurfaceVariant =
        MaterialTheme.colorScheme.onSurfaceVariant

    val onPrimary =
        MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
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
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier =
                    Modifier.height(36.dp)
            )

            Text(
                text =
                    when {
                        useOtpMode ->
                            "OTP Verification"

                        viewModel.isSignUp ->
                            "Create Account"

                        else ->
                            "Welcome"
                    },
                style =
                    MaterialTheme.typography
                        .headlineLarge
                        .copy(
                            fontWeight =
                                FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                textAlign =
                    TextAlign.Center,
                color =
                    onBackground
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(
                text =
                    when {
                        useOtpMode ->
                            "Enter your email and use the verification code sent to you."

                        viewModel.isSignUp ->
                            "Create your account with your email and password."

                        else ->
                            "Sign in to continue to your workspace."
                    },
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                textAlign =
                    TextAlign.Center,
                color =
                    onSurfaceVariant,
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(38.dp)
            )

            OutlinedTextField(
                value =
                    viewModel.email,
                onValueChange = {
                    viewModel.updateEmail(it)
                },
                label = {
                    Text("Email Address")
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Default.Email,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape =
                    RoundedCornerShape(16.dp),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Email,
                        imeAction =
                            if (useOtpMode) {
                                ImeAction.Done
                            } else {
                                ImeAction.Next
                            }
                    ),
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            if (!useOtpMode) {

                OutlinedTextField(
                    value =
                        viewModel.password,
                    onValueChange = {
                        viewModel.password = it
                    },
                    label = {
                        Text("Password")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Default.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel
                                    .isPasswordVisible =
                                    !viewModel
                                        .isPasswordVisible
                            }
                        ) {
                            Icon(
                                imageVector =
                                    if (
                                        viewModel
                                            .isPasswordVisible
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
                    shape =
                        RoundedCornerShape(16.dp),
                    visualTransformation =
                        if (
                            viewModel
                                .isPasswordVisible
                        ) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Password,
                            imeAction =
                                ImeAction.Done
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()

                                viewModel.authenticate(
                                    onAuthSuccess
                                )
                            }
                        ),
                    modifier =
                        Modifier.fillMaxWidth()
                )

            } else {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    OutlinedTextField(
                        value =
                            viewModel.otpCode,
                        onValueChange = { value ->
                            if (
                                value.length <= 6 &&
                                value.all(
                                    Char::isDigit
                                )
                            ) {
                                viewModel.otpCode =
                                    value
                            }
                        },
                        label = {
                            Text("6-Digit OTP")
                        },
                        placeholder = {
                            Text("Enter code")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Pin,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape =
                            RoundedCornerShape(16.dp),
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number,
                                imeAction =
                                    ImeAction.Done
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()

                                    if (
                                        viewModel.isOtpSent &&
                                        viewModel.otpCode.length == 6
                                    ) {
                                        viewModel.verifyOtp(
                                            onAuthSuccess
                                        )
                                    }
                                }
                            ),
                        modifier =
                            Modifier.weight(1f)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(10.dp)
                    )

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.sendOtp()
                        },
                        enabled =
                            !viewModel.isSendingOtp &&
                                    viewModel.otpCooldown == 0 &&
                                    viewModel.email.isNotBlank(),
                        shape =
                            RoundedCornerShape(16.dp),
                        modifier =
                            Modifier.height(56.dp)
                    ) {

                        if (
                            viewModel.isSendingOtp
                        ) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier.size(20.dp),
                                color =
                                    onPrimary,
                                strokeWidth =
                                    2.dp
                            )
                        } else {
                            Text(
                                text =
                                    if (
                                        viewModel.otpCooldown > 0
                                    ) {
                                        "Resend\n${viewModel.otpCooldown}s"
                                    } else if (
                                        viewModel.isOtpSent
                                    ) {
                                        "Resend"
                                    } else {
                                        "Send OTP"
                                    },
                                fontSize =
                                    12.sp,
                                textAlign =
                                    TextAlign.Center
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible =
                    viewModel.successMessage != null,
                enter =
                    fadeIn(),
                exit =
                    fadeOut()
            ) {
                viewModel.successMessage?.let {
                        message ->

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    Surface(
                        color =
                            MaterialTheme
                                .colorScheme
                                .primaryContainer
                                .copy(alpha = 0.65f),
                        shape =
                            RoundedCornerShape(12.dp),
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text =
                                message,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimaryContainer,
                            textAlign =
                                TextAlign.Center,
                            modifier =
                                Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 14.dp
                                )
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(28.dp)
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                shape =
                    RoundedCornerShape(16.dp),
                enabled =
                    if (useOtpMode) {
                        !viewModel.isLoading &&
                                viewModel.isOtpSent &&
                                viewModel.otpCode.length == 6
                    } else {
                        !viewModel.isLoading
                    }
            ) {

                if (viewModel.isLoading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(23.dp),
                        color =
                            onPrimary,
                        strokeWidth =
                            2.5.dp
                    )

                } else {

                    Text(
                        text =
                            when {
                                useOtpMode ->
                                    "Verify & Log In"

                                viewModel.isSignUp ->
                                    "Sign Up"

                                else ->
                                    "Log In"
                            },
                        fontWeight =
                            FontWeight.Bold,
                        fontSize =
                            16.sp
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            if (!viewModel.isSignUp) {

                TextButton(
                    onClick = {

                        useOtpMode =
                            !useOtpMode

                        viewModel.switchLoginMethod()
                    }
                ) {
                    Text(
                        text =
                            if (useOtpMode) {
                                "Use Password Instead"
                            } else {
                                "Log In with OTP Code"
                            },
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            if (!useOtpMode) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.Center
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
                        color =
                            onSurfaceVariant
                    )

                    TextButton(
                        onClick = {
                            viewModel.switchMode()
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
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )
        }

        viewModel.errorMessage?.let { message ->

            AlertDialog(
                onDismissRequest = {
                    viewModel.clearError()
                },
                title = {
                    Text(
                        text =
                            "Authentication Failed",
                        fontWeight =
                            FontWeight.Bold,
                        fontSize =
                            18.sp
                    )
                },
                text = {
                    Text(
                        text = message,
                        fontSize = 15.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearError()
                        }
                    ) {
                        Text(
                            text = "OK",
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                },
                shape =
                    RoundedCornerShape(20.dp),
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            )
        }
    }
}