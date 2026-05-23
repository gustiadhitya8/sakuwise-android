package com.gustiadhitya.sakuwise.feature.settings.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.domain.model.AllocationId
import com.gustiadhitya.sakuwise.feature.plan.displayName
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun AllocationEditorScreen(
    onBack: () -> Unit,
    main: MainViewModel = hiltViewModel(),
    mutator: PrefMutatorViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val prefs by main.prefs.collectAsState()
    var needs by remember { mutableIntStateOf(prefs.needsPct) }
    var wants by remember { mutableIntStateOf(prefs.wantsPct) }
    var invest by remember { mutableIntStateOf(prefs.investPct) }
    val total = needs + wants + invest
    val valid = total == 100

    LaunchedEffect(prefs) {
        if (needs == 0 && wants == 0 && invest == 0) {
            needs = prefs.needsPct; wants = prefs.wantsPct; invest = prefs.investPct
        }
    }

    SimpleSettingsScreen(title = stringResource(R.string.settings_alloc_title), onBack = onBack) {
        Text(
            stringResource(R.string.settings_alloc_intro),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(20.dp))
        AllocSlider(AllocationId.Needs.displayName(), sw.primary, needs) { needs = it }
        AllocSlider(AllocationId.Wants.displayName(), sw.accent, wants) { wants = it }
        AllocSlider(AllocationId.Invest.displayName(), sw.info, invest) { invest = it }
        Spacer(Modifier.height(12.dp))
        SwCard {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_alloc_total_label), color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f))
                Text("$total%",
                    color = if (valid) sw.success else sw.danger,
                    style = SwType.AmountL.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"))
            }
            if (!valid) {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.alloc_invalid_total),
                    color = sw.danger, style = SwType.LabelSmall.copy(fontSize = 12.sp))
            }
        }
        Spacer(Modifier.height(20.dp))
        SwButton(
            text = stringResource(R.string.alloc_save),
            onClick = {
                mutator.setAllocations(needs, wants, invest)
                onBack()
            },
            enabled = valid,
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun AllocSlider(label: String, tint: Color, value: Int, onChange: (Int) -> Unit) {
    val sw = SwTheme.colors
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(tint))
            Spacer(Modifier.size(width = 8.dp, height = 1.dp))
            Text(label, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f))
            Text("$value%", color = sw.ink,
                style = SwType.AmountL.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"))
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 99,
            colors = SliderDefaults.colors(
                thumbColor = tint, activeTrackColor = tint, inactiveTrackColor = sw.border,
            ),
        )
    }
}
