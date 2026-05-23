package com.gustiadhitya.sakuwise.feature.settings.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

/* ──────────────────────────────────────────────────────────────────────────
 *  Privacy Policy
 * ──────────────────────────────────────────────────────────────────────── */

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    SimpleSettingsScreen(title = stringResource(R.string.about_privacy), onBack = onBack) {
        LegalSection(
            heading = stringResource(R.string.privacy_section_principle),
            body = stringResource(R.string.privacy_principle_body),
        )
        LegalSection(
            heading = stringResource(R.string.privacy_section_collect),
            body = stringResource(R.string.privacy_collect_body),
        )
        LegalSection(
            heading = stringResource(R.string.privacy_section_share),
            body = stringResource(R.string.privacy_share_body),
        )
        LegalSection(
            heading = stringResource(R.string.privacy_section_backup),
            body = stringResource(R.string.privacy_backup_body),
        )
        LegalSection(
            heading = stringResource(R.string.privacy_section_reset),
            body = stringResource(R.string.privacy_reset_body),
        )
        LegalSection(
            heading = stringResource(R.string.privacy_section_camera),
            body = stringResource(R.string.privacy_camera_body),
        )
        LegalSection(
            heading = stringResource(R.string.privacy_section_contact_q),
            body = stringResource(R.string.privacy_contact_body),
        )
        Spacer(Modifier.height(24.dp))
    }
}

/* ──────────────────────────────────────────────────────────────────────────
 *  Open Source Licenses
 * ──────────────────────────────────────────────────────────────────────── */

@Composable
fun OpenSourceLicensesScreen(onBack: () -> Unit) {
    val apache = stringResource(R.string.licenses_apache)
    val bsd = stringResource(R.string.licenses_bsd)
    val libs = listOf(
        R.string.licenses_lib_kotlin to apache,
        R.string.licenses_lib_compose to apache,
        R.string.licenses_lib_hilt to apache,
        R.string.licenses_lib_room to apache,
        R.string.licenses_lib_sqlcipher to bsd,
        R.string.licenses_lib_argon2 to apache,
        R.string.licenses_lib_mlkit to apache,
        R.string.licenses_lib_workmanager to apache,
        R.string.licenses_lib_security to apache,
        R.string.licenses_lib_appcompat to apache,
    )

    SimpleSettingsScreen(title = stringResource(R.string.about_licenses), onBack = onBack) {
        val sw = SwTheme.colors
        Text(
            stringResource(R.string.licenses_intro),
            color = sw.inkMuted,
            style = SwType.Body.copy(fontSize = 13.sp),
            modifier = Modifier.padding(bottom = 14.dp),
        )
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                libs.forEachIndexed { i, (libRes, license) ->
                    LicenseRow(
                        name = stringResource(libRes),
                        license = license,
                    )
                    if (i < libs.lastIndex) {
                        androidx.compose.foundation.layout.Box(
                            Modifier.fillMaxWidth().height(1.dp).background(sw.border),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.licenses_footer),
            color = sw.inkSubtle,
            style = SwType.LabelSmall.copy(fontSize = 12.sp),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LicenseRow(name: String, license: String) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            Text(license, color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 11.sp))
        }
    }
}

/* ──────────────────────────────────────────────────────────────────────────
 *  Contact Developer
 * ──────────────────────────────────────────────────────────────────────── */

@Composable
fun ContactDeveloperScreen(onBack: () -> Unit) {
    val sw = SwTheme.colors
    SimpleSettingsScreen(title = stringResource(R.string.about_contact), onBack = onBack) {
        Text(
            stringResource(R.string.contact_intro),
            color = sw.inkMuted,
            style = SwType.Body.copy(fontSize = 13.sp),
            modifier = Modifier.padding(bottom = 16.dp),
        )
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                ContactRow(
                    label = stringResource(R.string.contact_email_label),
                    value = stringResource(R.string.contact_email_value),
                )
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxWidth().height(1.dp).background(sw.border),
                )
                ContactRow(
                    label = stringResource(R.string.contact_github_label),
                    value = stringResource(R.string.contact_github_value),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        LegalSection(
            heading = stringResource(R.string.contact_bug_label),
            body = stringResource(R.string.contact_bug_body),
        )
        LegalSection(
            heading = stringResource(R.string.contact_feature_label),
            body = stringResource(R.string.contact_feature_body),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ContactRow(label: String, value: String) {
    val sw = SwTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 11.sp))
        Text(value, color = sw.ink,
            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
    }
}

/* ──────────────────────────────────────────────────────────────────────────
 *  Shared section block
 * ──────────────────────────────────────────────────────────────────────── */

@Composable
private fun LegalSection(heading: String, body: String) {
    val sw = SwTheme.colors
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(heading, color = sw.ink,
            style = SwType.H3.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(4.dp))
        SwCard {
            Text(body, color = sw.inkMuted,
                style = SwType.Body.copy(fontSize = 13.sp))
        }
    }
}
