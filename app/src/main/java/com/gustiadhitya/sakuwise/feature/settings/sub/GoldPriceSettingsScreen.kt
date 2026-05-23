package com.gustiadhitya.sakuwise.feature.settings.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonSize
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GoldPriceSettingsScreen(
    onBack: () -> Unit,
    main: MainViewModel = hiltViewModel(),
    mutator: PrefMutatorViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val prefs by main.prefs.collectAsState()
    var raw by remember { mutableStateOf(prefs.goldPriceGlobal.toString()) }

    LaunchedEffect(prefs.goldPriceGlobal) {
        if (raw == "0" || raw.isBlank()) raw = prefs.goldPriceGlobal.toString()
    }

    SimpleSettingsScreen(title = stringResource(R.string.gold_price_settings_title), onBack = onBack) {
        // Intro
        Text(
            stringResource(R.string.gold_price_settings_intro),
            color = sw.inkMuted,
            style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))
        SwField(
            value = if (raw == "0") "" else raw,
            onValueChange = { v -> raw = v.filter { it.isDigit() } },
            label = stringResource(R.string.gold_price_field_label),
            prefix = "Rp", rupiah = true,
            suffix = "/ gram",
            keyboardType = KeyboardType.Number,
        )
        Spacer(Modifier.height(4.dp))
        // Tip box (warningSoft bg + warning border — exact prototype match)
        val tipPrefix = stringResource(R.string.gold_price_tip_prefix)
        val tipBody = stringResource(R.string.gold_price_tip_body)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(sw.warningSoft)
                .border(1.dp, sw.warning.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = sw.ink, fontWeight = FontWeight.Bold)) {
                        append(tipPrefix)
                    }
                    append(tipBody)
                },
                color = sw.inkMuted,
                style = SwType.Body.copy(fontSize = 12.sp),
            )
        }
        Spacer(Modifier.height(20.dp))
        SwButton(
            text = stringResource(R.string.gold_price_save),
            onClick = {
                mutator.setGoldPrice(raw.toLongOrNull() ?: 0L)
                onBack()
            },
            size = SwButtonSize.Lg,
            enabled = raw.toLongOrNull() != null && raw.toLongOrNull()!! > 0,
            leading = { Icon(Icons.Outlined.Check, null, tint = sw.onPrimary, modifier = Modifier.size(18.dp)) },
        )
        Spacer(Modifier.height(20.dp))
    }
}
