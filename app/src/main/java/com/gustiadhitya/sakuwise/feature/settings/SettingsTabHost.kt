package com.gustiadhitya.sakuwise.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gustiadhitya.sakuwise.feature.settings.sub.AboutScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.AllocationEditorScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.AutoLockSettingsScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.GoldPriceSettingsScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.LanguageSettingsScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.PeriodStartSettingsScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.PinSettingsScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.PrefMutatorViewModel
import com.gustiadhitya.sakuwise.feature.settings.sub.ContactDeveloperScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.OpenSourceLicensesScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.PrivacyPolicyScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.ProfileSettingsScreen
import com.gustiadhitya.sakuwise.feature.settings.sub.ResetAppScreen
import androidx.hilt.navigation.compose.hiltViewModel

internal sealed interface SettingsRoute {
    data object Hub : SettingsRoute
    data object Profile : SettingsRoute
    data object Language : SettingsRoute
    data object AutoLock : SettingsRoute
    data object PeriodStart : SettingsRoute
    data object Allocation : SettingsRoute
    data object Pin : SettingsRoute
    data object GoldPrice : SettingsRoute
    data object About : SettingsRoute
    data object Backup : SettingsRoute
    data object Restore : SettingsRoute
    data object Donate : SettingsRoute
    data object Reset : SettingsRoute
    data object ExportPdf : SettingsRoute
    data object PrivacyPolicy : SettingsRoute
    data object Licenses : SettingsRoute
    data object Contact : SettingsRoute
}

@Composable
fun SettingsTabHost() {
    var route by remember { mutableStateOf<SettingsRoute>(SettingsRoute.Hub) }
    val pop = { route = SettingsRoute.Hub }
    val prefMutator: PrefMutatorViewModel = hiltViewModel()

    // Intra-tab back: sub-routes go to Hub, Restore→Backup, Legal pages→About, then MainShell takes over on Hub.
    androidx.activity.compose.BackHandler(enabled = route !is SettingsRoute.Hub) {
        route = when (route) {
            SettingsRoute.Restore -> SettingsRoute.Backup
            SettingsRoute.PrivacyPolicy,
            SettingsRoute.Licenses,
            SettingsRoute.Contact -> SettingsRoute.About
            else -> SettingsRoute.Hub
        }
    }

    when (route) {
        SettingsRoute.Hub -> SettingsScreen(
            onNavigateToProfile = { route = SettingsRoute.Profile },
            onNavigateToLanguage = { route = SettingsRoute.Language },
            onNavigateToAutoLock = { route = SettingsRoute.AutoLock },
            onNavigateToPeriodStart = { route = SettingsRoute.PeriodStart },
            onNavigateToAllocation = { route = SettingsRoute.Allocation },
            onNavigateToPin = { route = SettingsRoute.Pin },
            onNavigateToGoldPrice = { route = SettingsRoute.GoldPrice },
            onNavigateToAbout = { route = SettingsRoute.About },
            onNavigateToBackup = { route = SettingsRoute.Backup },
            onNavigateToDonate = { route = SettingsRoute.Donate },
            onNavigateToReset = { route = SettingsRoute.Reset },
            onNavigateToExport = { route = SettingsRoute.ExportPdf },
            onReplayOnboarding = { prefMutator.replayOnboarding() },
        )
        SettingsRoute.Profile -> ProfileSettingsScreen(onBack = pop)
        SettingsRoute.Language -> LanguageSettingsScreen(onBack = pop)
        SettingsRoute.AutoLock -> AutoLockSettingsScreen(onBack = pop)
        SettingsRoute.PeriodStart -> PeriodStartSettingsScreen(onBack = pop)
        SettingsRoute.Allocation -> AllocationEditorScreen(onBack = pop)
        SettingsRoute.Pin -> PinSettingsScreen(onBack = pop)
        SettingsRoute.GoldPrice -> GoldPriceSettingsScreen(onBack = pop)
        SettingsRoute.About -> AboutScreen(
            onBack = pop,
            onNavigateToPrivacy = { route = SettingsRoute.PrivacyPolicy },
            onNavigateToLicenses = { route = SettingsRoute.Licenses },
            onNavigateToContact = { route = SettingsRoute.Contact },
        )
        SettingsRoute.PrivacyPolicy -> PrivacyPolicyScreen(onBack = { route = SettingsRoute.About })
        SettingsRoute.Licenses -> OpenSourceLicensesScreen(onBack = { route = SettingsRoute.About })
        SettingsRoute.Contact -> ContactDeveloperScreen(onBack = { route = SettingsRoute.About })
        SettingsRoute.Backup -> com.gustiadhitya.sakuwise.feature.settings.backup.BackupSettingsScreen(
            onBack = pop,
            onOpenRestore = { route = SettingsRoute.Restore },
        )
        SettingsRoute.Restore -> com.gustiadhitya.sakuwise.feature.settings.backup.RestoreFlowScreen(
            onBack = { route = SettingsRoute.Backup },
            onRestored = { route = SettingsRoute.Hub },
        )
        SettingsRoute.Donate -> com.gustiadhitya.sakuwise.feature.settings.DonateScreen(onBack = pop)
        SettingsRoute.Reset -> ResetAppScreen(
            onBack = pop,
            // After wipe, prefs reset → SakuwiseApp recomposes back to onboarding overlay.
            onDone = pop,
        )
        SettingsRoute.ExportPdf ->
            com.gustiadhitya.sakuwise.feature.settings.export.ExportPdfScreen(onBack = pop)
    }
}
