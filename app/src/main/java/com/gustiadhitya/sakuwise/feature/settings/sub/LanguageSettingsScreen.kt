package com.gustiadhitya.sakuwise.feature.settings.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import android.app.Activity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun LanguageSettingsScreen(
    onBack: () -> Unit,
    main: MainViewModel = hiltViewModel(),
    mutator: PrefMutatorViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val prefs by main.prefs.collectAsState()
    val ctx = LocalContext.current

    SimpleSettingsScreen(title = stringResource(R.string.language_title), onBack = onBack) {
        Text(
            stringResource(R.string.language_intro),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))
        listOf(
            "id" to (stringResource(R.string.onb_lang_id) to stringResource(R.string.onb_lang_id_sub)),
            "en" to (stringResource(R.string.onb_lang_en) to stringResource(R.string.onb_lang_en_sub)),
        ).forEach { (code, pair) ->
            LangRow(
                active = prefs.language == code,
                label = pair.first,
                sub = pair.second,
                onClick = {
                    // mutator.setLanguage calls AppCompatDelegate.setApplicationLocales
                    // which triggers a config-change recreate on AppCompatActivity
                    // — no manual recreate needed (and the manual recreate raced the
                    // async DataStore write, reading the OLD pref on rebuild).
                    mutator.setLanguage(code)
                },
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LangRow(active: Boolean, label: String, sub: String, onClick: () -> Unit) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) sw.primaryContainer else sw.surface)
            .border(1.5.dp, if (active) sw.primary else sw.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
            Text(sub, color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 11.sp))
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (active) sw.primary else Color.Transparent)
                .border(2.dp, if (active) sw.primary else sw.borderStrong, CircleShape),
        ) {
            if (active) Icon(Icons.Outlined.Check, null,
                tint = sw.onPrimary, modifier = Modifier.size(12.dp))
        }
    }
}
