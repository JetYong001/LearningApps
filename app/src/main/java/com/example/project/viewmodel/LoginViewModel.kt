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
    var isOtpSent by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    var isSignUp by mutableStateOf(false)
    var isPasswordVisible by mutableStateOf(false)

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun updateEmail(value: String) {
        email = value
        isOtpSent = false
        otpCode = ""
        successMessage = null
        errorMessage = null
    }

    fun switchMode() {
        isSignUp = !isSignUp
        password = ""
        otpCode = ""
        isOtpSent = false
        errorMessage = null
        successMessage = null
        isPasswordVisible = false
    }

    fun switchLoginMethod() {
        otpCode = ""
        isOtpSent = false
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
        if (otpCooldown > 0 || isSendingOtp) {
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
            successMessage = null
            isOtpSent = false
            otpCode = ""

            try {
                supabase.auth.signInWith(OTP) {
                    email = currentEmail
                    createUser = false
                }

                isOtpSent = true

                successMessage =
                    "Verification code sent to your email."

                startCooldown()

            } catch (e: Exception) {

                isOtpSent = false
                otpCode = ""

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

    fun verifyOtp(
        onSuccess: () -> Unit
    ) {
        val currentEmail =
            email.trim().lowercase()

        val currentOtp =
            otpCode.trim()

        if (!isOtpSent) {
            errorMessage =
                "Please request a verification code first."
            return
        }

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

        if (isLoading) {
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

                successMessage =
                    "Email verified successfully."

                otpCode = ""
                isOtpSent = false

                onSuccess()

            } catch (e: Exception) {

                errorMessage =
                    getFriendlyError(
                        e,
                        "The verification code is incorrect."
                    )

            } finally {
                isLoading = false
            }
        }
    }

    fun authenticate(
        onSuccess: () -> Unit
    ) {
        val currentEmail =
            email.trim().lowercase()

        val currentPassword =
            password

        if (
            currentEmail.isEmpty() ||
            !currentEmail.contains("@")
        ) {
            errorMessage =
                "Please enter a valid email address."
            return
        }

        if (currentPassword.length < 6) {
            errorMessage =
                "Password must be at least 6 characters."
            return
        }

        if (isLoading) {
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

                    val newUserId =
                        supabase.auth
                            .currentUserOrNull()
                            ?.id

                    if (newUserId != null) {
                        createProfileIfNeeded(
                            userId = newUserId,
                            username =
                                currentEmail
                                    .substringBefore("@")
                        )
                    }

                    password = ""

                    successMessage =
                        "Account created successfully."

                    isSignUp = false

                } else {

                    supabase.auth.signInWith(Email) {
                        email = currentEmail
                        password = currentPassword
                    }

                    password = ""

                    onSuccess()
                }

            } catch (e: Exception) {

                errorMessage =
                    getFriendlyError(
                        e,
                        if (isSignUp) {
                            "Unable to create your account."
                        } else {
                            "Unable to sign in."
                        }
                    )

            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun createProfileIfNeeded(
        userId: String,
        username: String
    ) {
        try {
            val existingProfile =
                supabase
                    .from("profiles")
                    .select {
                        filter {
                            eq(
                                "id",
                                userId
                            )
                        }
                    }
                    .decodeList<Profile>()

            if (existingProfile.isEmpty()) {
                supabase
                    .from("profiles")
                    .insert(
                        Profile(
                            id = userId,
                            username = username,
                            profile_picture = null
                        )
                    )
            }
        } catch (_: Exception) {
        }
    }

    fun clearError() {
        errorMessage = null
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

            "signups not allowed for otp" in message ||
                    "otp_disabled" in message ->
                "This email is not registered. Please sign up first."

            "otp is disabled" in message ||
                    "sign in with otp is disabled" in message ->
                "OTP login is currently unavailable."

            "user_not_found" in message ||
                    "user not found" in message ||
                    "email not found" in message ||
                    "no user found" in message ->
                "This email is not registered. Please sign up first."

            "invalid login credentials" in message ||
                    "invalid_credentials" in message ||
                    "invalid_grant" in message ||
                    "wrong password" in message ->
                "Incorrect email or password."

            "email not confirmed" in message ||
                    "email_not_confirmed" in message ->
                "Please verify your email before logging in."

            "user already registered" in message ||
                    "already registered" in message ||
                    "already_exists" in message ->
                "This email is already registered."

            "expired" in message &&
                    (
                            "otp" in message ||
                                    "token" in message ||
                                    "code" in message
                            ) ->
                "This verification code has expired. Please request a new one."

            "invalid" in message &&
                    (
                            "otp" in message ||
                                    "token" in message ||
                                    "code" in message
                            ) ->
                "The verification code is incorrect."

            "too many" in message ||
                    "rate limit" in message ||
                    "rate_limit" in message ->
                "Too many attempts. Please try again later."

            "password" in message &&
                    (
                            "too short" in message ||
                                    "at least" in message ||
                                    "characters" in message
                            ) ->
                "Password must be at least 6 characters."

            "invalid email" in message ||
                    "email_format" in message ->
                "Please enter a valid email address."

            "network" in message ||
                    "timeout" in message ||
                    "connection" in message ->
                "Network error. Please check your internet connection."

            "disabled" in message ->
                "This authentication method is currently unavailable."

            else ->
                defaultMessage
        }
    }
}