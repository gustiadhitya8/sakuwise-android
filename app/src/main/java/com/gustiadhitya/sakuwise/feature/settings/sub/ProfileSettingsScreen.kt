package com.gustiadhitya.sakuwise.feature.settings.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    main: MainViewModel = hiltViewModel(),
    mutator: PrefMutatorViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val prefs by main.prefs.collectAsState()
    var name by remember { mutableStateOf("") }
    LaunchedEffect(prefs.userNickname) {
        if (name.isEmpty()) name = prefs.userNickname
    }

    SimpleSettingsScreen(title = stringResource(R.string.profile_title), onBack = onBack) {
        Text(
            stringResource(R.string.profile_intro),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))
        SwField(value = name, onValueChange = { name = it },
            label = stringResource(R.string.profile_nickname_label),
            placeholder = stringResource(R.string.profile_nickname_placeholder))
        Spacer(Modifier.height(20.dp))
        SwButton(
            text = stringResource(R.string.profile_save),
            onClick = { mutator.setNickname(name.trim()); onBack() },
            enabled = name.isNotBlank() && name != prefs.userNickname,
        )
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(sw.primaryContainer)
                .padding(16.dp),
        ) {
            Text(
                stringResource(R.string.profile_privacy_note),
                color = sw.onPrimaryContainer,
                style = SwType.Body.copy(fontSize = 12.sp),
            )
        }
    }
}
