package com.gustiadhitya.sakuwise.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen

@Composable
fun DonateScreen(onBack: () -> Unit) {
    val sw = SwTheme.colors
    SimpleSettingsScreen(title = stringResource(R.string.donate_title), onBack = onBack) {
        // Hero
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(sw.primary)
                .padding(20.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
            ) {
                Icon(Icons.Outlined.AutoAwesome, null, tint = sw.accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(width = 14.dp, height = 1.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.donate_hero_title), color = sw.onPrimary,
                    style = SwType.H2.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold))
                Text(
                    stringResource(R.string.donate_hero_body),
                    color = sw.onPrimary.copy(alpha = 0.85f),
                    style = SwType.Body.copy(fontSize = 13.sp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Text(stringResource(R.string.donate_section_platform), color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                DonateRow("S", Color(0xFFFFC107),
                    stringResource(R.string.donate_platform_saweria),
                    stringResource(R.string.donate_platform_saweria_url), onClick = {})
                Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
                DonateRow("T", Color(0xFF9333EA),
                    stringResource(R.string.donate_platform_trakteer),
                    stringResource(R.string.donate_platform_trakteer_url), onClick = {})
            }
        }
        Spacer(Modifier.height(16.dp))

        Text(stringResource(R.string.donate_section_qris), color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        SwCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                ) {
                    // QRIS placeholder pattern
                    Box(
                        modifier = Modifier
                            .size(176.dp)
                            .background(Color(0xFF000000)),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.donate_qris_sub),
                    color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 12.sp))
                Text(stringResource(R.string.donate_nmid), color = sw.inkSubtle,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp, fontFeatureSettings = "tnum"))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.donate_footnote),
            color = sw.inkSubtle, style = SwType.LabelSmall.copy(fontSize = 11.sp),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun DonateRow(initial: String, accent: Color, name: String, url: String, onClick: () -> Unit) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(accent),
        ) {
            Text(initial, color = Color.White,
                fontSize = 17.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(width = 12.dp, height = 1.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            Text(url, color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 11.sp))
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = sw.inkSubtle, modifier = Modifier.size(18.dp))
    }
}
