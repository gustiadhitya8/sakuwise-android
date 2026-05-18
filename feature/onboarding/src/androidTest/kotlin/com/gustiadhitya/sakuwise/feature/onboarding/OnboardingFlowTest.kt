package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit4.runners.AndroidJUnit4
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.model.AccountType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun identityScreen_emptyNickname_lanjutButtonDisabled() {
        composeTestRule.setContent {
            SakuwiseTheme {
                Onb_IdentityContent(
                    nickname = "",
                    pin = "123456",
                    biometricEnabled = false,
                    biometricAvailable = false,
                    onNicknameChange = {},
                    onPinChange = {},
                    onBiometricToggle = {},
                    onNext = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Lanjut").assertIsNotEnabled()
    }

    @Test
    fun identityScreen_emptyPin_lanjutButtonDisabled() {
        composeTestRule.setContent {
            SakuwiseTheme {
                Onb_IdentityContent(
                    nickname = "Gusti",
                    pin = "",
                    biometricEnabled = false,
                    biometricAvailable = false,
                    onNicknameChange = {},
                    onPinChange = {},
                    onBiometricToggle = {},
                    onNext = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Lanjut").assertIsNotEnabled()
    }

    @Test
    fun identityScreen_nicknameAndPin_lanjutButtonEnabled() {
        composeTestRule.setContent {
            SakuwiseTheme {
                Onb_IdentityContent(
                    nickname = "Gusti",
                    pin = "123456",
                    biometricEnabled = false,
                    biometricAvailable = false,
                    onNicknameChange = {},
                    onPinChange = {},
                    onBiometricToggle = {},
                    onNext = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Lanjut").assertIsEnabled()
    }

    @Test
    fun languageScreen_defaultSelection_isBahasaIndonesia() {
        composeTestRule.setContent {
            SakuwiseTheme {
                Onb_LanguageContent(
                    selectedCode = "id",
                    onSelectLanguage = {},
                    onNext = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Bahasa Indonesia").assertExists()
        composeTestRule.onNodeWithText("English").assertExists()
    }

    @Test
    fun firstAccountScreen_showsExpectedContent() {
        composeTestRule.setContent {
            SakuwiseTheme {
                Onb_FirstAccountContent(
                    accountName = "Tunai",
                    accountType = AccountType.CASH,
                    initialBalance = 0L,
                    onAccountNameChange = {},
                    onAccountTypeChange = {},
                    onBalanceChange = {},
                    onDone = {},
                    onSkip = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Akun pertamamu").assertExists()
        composeTestRule.onNodeWithText("Selesai · Masuk Beranda").assertExists()
        composeTestRule.onNodeWithText("Tambah akun lain nanti").assertExists()
    }

    @Test
    fun languageScreen_tapEnglish_callsSelectLanguage() {
        var selectedCode = "id"
        composeTestRule.setContent {
            SakuwiseTheme {
                Onb_LanguageContent(
                    selectedCode = selectedCode,
                    onSelectLanguage = { selectedCode = it },
                    onNext = {},
                )
            }
        }

        composeTestRule.onNodeWithText("English").performClick()
        assert(selectedCode == "en")
    }
}
