package com.example.project.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.Profile
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var otpCode by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var isSendingOtp by mutableStateOf(false)

    var otpCooldown by mutableStateOf(0)

    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    var isSignUp by mutableStateOf(false)
    var isPasswordVisible by mutableStateOf(false)

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun checkExistingSession(
        onAlreadyLoggedIn: () -> Unit
    ) {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    onAlreadyLoggedIn()
                }
            }
        }
    }

    fun sendOtp() {
        if (otpCooldown > 0) return

        val currentEmail = email.trim().lowercase()

        if (currentEmail.isEmpty() || !currentEmail.contains("@")) {
            errorMessage = "Invalid email address"
            return
        }

        viewModelScope.launch {
            isSendingOtp = true
            errorMessage = null
            successMessage = null

            try {
                // 设置 createUser = false，未注册账号禁止发送 OTP
                supabase.auth.signInWith(OTP) {
                    email = currentEmail
                    createUser = false
                }

                successMessage = "OTP has been sent to your email."
                startCooldown()

            } catch (e: Exception) {
                val msg = e.message ?: ""
                errorMessage = if (msg.contains("user_not_found", ignoreCase = true) ||
                    msg.contains("invalid", ignoreCase = true)
                ) {
                    "This email is not registered yet. Please sign up with password first."
                } else {
                    e.message ?: "Failed to send OTP"
                }
            } finally {
                isSendingOtp = false
            }
        }
    }

    private fun startCooldown() {
        viewModelScope.launch {
            otpCooldown = 60
            while (otpCooldown > 0) {
                delay(1000)
                otpCooldown--
            }
        }
    }

    fun verifyOtp(
        onSuccess: () -> Unit
    ) {
        val currentEmail = email.trim().lowercase()
        val currentOtp = otpCode.trim()

        if (currentEmail.isEmpty() || !currentEmail.contains("@")) {
            errorMessage = "Invalid email address"
            return
        }

        if (currentOtp.isEmpty()) {
            errorMessage = "Please enter the OTP code"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            try {
                supabase.auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL,
                    email = currentEmail,
                    token = currentOtp
                )

                successMessage = "Email verified successfully."
                onSuccess()

            } catch (e: Exception) {
                errorMessage = e.message ?: "Invalid OTP code"
            } finally {
                isLoading = false
            }
        }
    }

    fun authenticate(
        onSuccess: () -> Unit
    ) {
        val currentEmail = email.trim().lowercase()
        val currentPassword = password.trim()

        if (currentEmail.isEmpty() || !currentEmail.contains("@")) {
            errorMessage = "Invalid email address"
            return
        }

        if (currentPassword.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            try {
                if (isSignUp) {
                    supabase.auth.signUpWith(Email) {
                        email = currentEmail
                        password = currentPassword
                    }

                    val newUserId = supabase.auth.currentUserOrNull()?.id

                    if (newUserId != null) {
                        val defaultUsername = currentEmail.substringBefore("@")

                        val initialProfile = Profile(
                            id = newUserId,
                            username = defaultUsername,
                            profile_picture = null
                        )

                        supabase.from("profiles").insert(initialProfile)
                    }

                    successMessage = "Account created successfully."
                    isSignUp = false
                    password = ""

                } else {
                    supabase.auth.signInWith(Email) {
                        email = currentEmail
                        password = currentPassword
                    }

                    onSuccess()
                }

            } catch (e: Exception) {
                errorMessage = e.message ?: "Authentication failed"
            } finally {
                isLoading = false
            }
        }
    }
}