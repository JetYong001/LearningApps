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
                "Please enter a valid email address."
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
                    getFriendlyError(
                        e,
                        "Unable to send verification code."
                    )

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
                "Please enter a valid email address."
            return
        }

        if (currentOtp.isEmpty()) {
            errorMessage =
                "Please enter the verification code."
            return
        }

        if (currentOtp.length != 6) {
            errorMessage =
                "The verification code must contain 6 digits."
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
                otpCode = ""

            } catch (e: Exception) {

                errorMessage =
                    getFriendlyError(
                        e,
                        "Invalid verification code."
                    )

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
                "Please verify your email first."
            return
        }

        if (newPassword.isBlank()) {
            errorMessage =
                "Please enter a new password."
            return
        }

        if (newPassword.length < 6) {
            errorMessage =
                "Password must be at least 6 characters."
            return
        }

        if (confirmPassword.isBlank()) {
            errorMessage =
                "Please confirm your new password."
            return
        }

        if (newPassword != confirmPassword) {
            errorMessage =
                "Passwords do not match."
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
                    getFriendlyError(
                        e,
                        "Unable to update your password."
                    )

            } finally {

                isChangingPassword = false
            }
        }
    }

    private fun getFriendlyError(
        exception: Exception,
        defaultMessage: String
    ): String {

        val message =
            exception.message
                ?.lowercase()
                .orEmpty()

        return when {

            "expired" in message &&
                    ("otp" in message ||
                            "token" in message ||
                            "code" in message) ->
                "This verification code has expired. Please request a new one."

            "invalid" in message &&
                    ("otp" in message ||
                            "token" in message ||
                            "code" in message) ->
                "The verification code is incorrect."

            "otp" in message &&
                    ("too many" in message ||
                            "rate" in message ||
                            "limit" in message) ->
                "Too many attempts. Please try again later."

            "email" in message &&
                    ("not found" in message ||
                            "not exist" in message) ->
                "This email address could not be found."

            "password" in message &&
                    ("weak" in message ||
                            "short" in message ||
                            "length" in message) ->
                "Your password is too weak. Please use a stronger password."

            "password" in message &&
                    ("same" in message ||
                            "current" in message) ->
                "Please choose a different password."

            "network" in message ||
                    "timeout" in message ||
                    "connection" in message ->
                "Network error. Please check your internet connection."

            "rate limit" in message ||
                    "too many requests" in message ->
                "Too many requests. Please try again later."

            "already registered" in message ->
                "This account is already registered."

            "not authorized" in message ||
                    "unauthorized" in message ||
                    "forbidden" in message ->
                "You are not authorized to perform this action."

            else ->
                defaultMessage
        }
    }
}