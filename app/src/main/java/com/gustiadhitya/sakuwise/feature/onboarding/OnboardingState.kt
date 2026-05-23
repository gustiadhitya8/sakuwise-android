package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable

data class OnboardingUi(
    val lang: String = "id",
    val nickname: String = "Gusti",
    val pin: String = "",
    val biometric: Boolean = true,
    val accountName: String = "Tunai",
    val accountType: String = "Tunai",
    val accountBalance: Long = 0L,
)

@Composable
fun rememberOnboardingState(): MutableState<OnboardingUi> =
    remember { mutableStateOf(OnboardingUi()) }
