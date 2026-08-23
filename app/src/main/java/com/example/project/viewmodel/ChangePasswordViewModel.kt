package com.example.project.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {

    var email by mutableStateOf("")
    var otpCode by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var isSendingOtp by mutableStateOf(false)
    var isVerifyingOtp by mutableStateOf(false)
    var isChangingPassword by mutableStateOf(false)

    var otpCooldown by mutableStateOf(0)

    var otpVerified by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)

    fun loadCurrentEmail() {

        email =
            supabase.auth
                .currentUserOrNull()
                ?.email
                ?: ""
    }

    fun clearError() {
        errorMessage = null
    }

    fun sendOtp() {

        if (otpCooldown > 0) {
            return
        }

        val currentEmail =
            email.trim().lowercase()

        if (
            currentEmail.isEmpty() ||
            !currentEmail.contains("@")
        ) {

            errorMessage =
                "Unable to find a valid email address"

            return
        }

        viewModelScope.launch {

            isSendingOtp = true
            errorMessage = null

            try {

                supabase.auth.signInWith(OTP) {

                    email = currentEmail
                }

                startCooldown()

            } catch (e: Exception) {

                errorMessage =
                    e.message
                        ?: "Failed to send OTP"

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

    fun verifyOtp() {

        val currentEmail =
            email.trim().lowercase()

        val currentOtp =
            otpCode.trim()

        if (
            currentEmail.isEmpty() ||
            !currentEmail.contains("@")
        ) {

            errorMessage =
                "Invalid email address"

            return
        }

        if (currentOtp.isEmpty()) {

            errorMessage =
                "Please enter the OTP code"

            return
        }

        viewModelScope.launch {

            isVerifyingOtp = true
            errorMessage = null

            try {

                supabase.auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL,
                    email = currentEmail,
                    token = currentOtp
                )

                otpVerified = true

            } catch (e: Exception) {

                errorMessage =
                    e.message
                        ?: "Invalid OTP code"

            } finally {

                isVerifyingOtp = false
            }
        }
    }

    fun changePassword(
        onSuccess: () -> Unit
    ) {

        if (!otpVerified) {

            errorMessage =
                "Please verify the OTP first"

            return
        }

        if (newPassword.length < 6) {

            errorMessage =
                "Password must be at least 6 characters"

            return
        }

        if (newPassword != confirmPassword) {

            errorMessage =
                "Passwords do not match"

            return
        }

        viewModelScope.launch {

            isChangingPassword = true
            errorMessage = null

            try {

                supabase.auth.updateUser {

                    password = newPassword
                }

                otpVerified = false
                otpCode = ""
                newPassword = ""
                confirmPassword = ""

                onSuccess()

            } catch (e: Exception) {

                errorMessage =
                    e.message
                        ?: "Failed to change password"

            } finally {

                isChangingPassword = false
            }
        }
    }
}