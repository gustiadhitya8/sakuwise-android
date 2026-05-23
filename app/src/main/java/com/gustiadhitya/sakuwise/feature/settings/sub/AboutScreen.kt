package com.gustiadhitya.sakuwise.feature.settings.sub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.icons.LogoDaun
import com.gustiadhitya.sakuwise.core.designsystem.icons.Wordmark
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToLicenses: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
) {
    val sw = SwTheme.colors
    SimpleSettingsScreen(title = stringResource(R.string.about_title), onBack = onBack) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        ) {
            LogoDaun(sizeDp = 80)
            Spacer(Modifier.height(12.dp))
            Wordmark(sizeSp = 22)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.about_version), color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 13.sp))
        }
        SwCard {
            Text(
                stringResource(R.string.about_blurb),
                color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
            )
        }
        Spacer(Modifier.height(16.dp))
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                AboutRow(stringResource(R.string.about_privacy), onClick = onNavigateToPrivacy)
                AboutRow(stringResource(R.string.about_licenses), onClick = onNavigateToLicenses)
                AboutRow(stringResource(R.string.about_contact), onClick = onNavigateToContact)
            }
        }
    }
}

@Composable
private fun AboutRow(label: String, onClick: () -> Unit) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp) // A11Y-004 tap target
            .clickable(onClick = onClick) // BUG FIX: row was missing onClick — links were dead
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(label, color = sw.ink,
            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ChevronRight, null, tint = sw.inkSubtle, modifier = Modifier.size(18.dp))
    }
}
