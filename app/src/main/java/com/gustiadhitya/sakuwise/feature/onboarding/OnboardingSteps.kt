package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.toRupiah
import com.gustiadhitya.sakuwise.core.designsystem.components.PinInput
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.icons.LogoDaun
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType as Type

// ─── Step 1: Language ──────────────────────────────────────────
@Composable
fun OnbLanguageScreen(
    state: OnboardingUi,
    onChange: (OnboardingUi) -> Unit,
    onNext: () -> Unit,
) {
    val sw = SwTheme.colors
    val langId = stringResource(R.string.onb_lang_id)
    val langIdSub = stringResource(R.string.onb_lang_id_sub)
    val langEn = stringResource(R.string.onb_lang_en)
    val langEnSub = stringResource(R.string.onb_lang_en_sub)
    OnboardingShell(
        step = 1, total = 4,
        hero = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    // Hero squircle reduced from 180dp → 140dp so all 4 onboarding
                    // steps fit a single viewport without scroll on standard phones.
                    .size(140.dp)
                    .clip(RoundedCornerShape(46.dp))
                    .background(sw.primaryContainer),
            ) {
                LogoDaun(sizeDp = 84)
            }
        },
        title = stringResource(R.string.onb_step1_title),
        subtitle = stringResource(R.string.onb_step1_subtitle),
        primaryLabel = stringResource(R.string.onb_step1_primary),
        onPrimary = onNext,
    ) {
        Text(
            stringResource(R.string.onb_step1_section),
            color = sw.inkSubtle,
            style = Type.SectionLabel,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Source of truth for the radio is the ACTIVE locale (what's
            // visibly painted), not the DataStore pref. On a fresh install
            // DataStore defaults to "id" but the system locale (e.g. "en"
            // on a device with English UI) is what's actually rendering —
            // the previous version showed the wrong radio in that case.
            val configLocale = androidx.compose.ui.platform.LocalConfiguration.current
                .locales.get(0)?.language?.lowercase()
            val activeLang = when {
                configLocale == "en" -> "en"
                configLocale == "id" -> "id"
                else -> state.lang
            }
            listOf("id" to (langId to langIdSub), "en" to (langEn to langEnSub))
                .forEach { (id, pair) ->
                    val active = activeLang == id
                    LangRow(active, pair.first, pair.second) { onChange(state.copy(lang = id)) }
                }
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
            Text(label, color = sw.ink, style = SwType.LabelStrong.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
            Text(sub, color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (active) sw.primary else Color.Transparent)
                .border(2.dp, if (active) sw.primary else sw.borderStrong, CircleShape),
        ) {
            if (active) {
                Icon(Icons.Outlined.Check, null, tint = sw.onPrimary, modifier = Modifier.size(12.dp))
            }
        }
    }
}

// ─── Step 2: Identity ──────────────────────────────────────────
@Composable
fun OnbIdentityScreen(
    state: OnboardingUi,
    onChange: (OnboardingUi) -> Unit,
    onNext: () -> Unit,
) {
    val sw = SwTheme.colors
    OnboardingShell(
        step = 2, total = 4,
        hero = {
            Box(modifier = Modifier.size(140.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(60.dp))
                        .background(sw.primaryContainer),
                ) {
                    Icon(Icons.Outlined.Shield, null, tint = sw.primary, modifier = Modifier.size(90.dp))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(sw.surface)
                        .border(2.dp, sw.bg, RoundedCornerShape(18.dp)),
                ) {
                    Icon(Icons.Outlined.Person, null, tint = sw.primary, modifier = Modifier.size(28.dp))
                }
            }
        },
        title = stringResource(R.string.onb_step2_title),
        subtitle = stringResource(R.string.onb_step2_subtitle),
        primaryLabel = stringResource(R.string.action_continue),
        onPrimary = onNext,
        primaryEnabled = state.nickname.isNotBlank() && state.pin.length == 6,
    ) {
        SwField(
            value = state.nickname,
            onValueChange = { onChange(state.copy(nickname = it)) },
            label = stringResource(R.string.onb_step2_nickname_label),
            placeholder = stringResource(R.string.onb_step2_nickname_placeholder),
            hint = stringResource(R.string.onb_step2_nickname_hint),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.onb_step2_pin_label),
            color = sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        PinInput(
            value = state.pin,
            onChange = { onChange(state.copy(pin = it.take(6))) },
        )
        Text(
            stringResource(R.string.onb_step2_pin_hint),
            color = sw.inkSubtle,
            style = SwType.LabelSmall.copy(fontSize = 11.sp),
            modifier = Modifier.padding(top = 6.dp, start = 4.dp),
        )
        Spacer(Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(sw.surface)
                .border(1.dp, sw.border, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sw.primaryContainer),
            ) { Icon(Icons.Outlined.Shield, null, tint = sw.onPrimaryContainer, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.onb_step2_biometric_title), color = sw.ink, style = SwType.LabelStrong.copy(fontSize = 13.sp))
                Text(stringResource(R.string.onb_step2_biometric_sub),
                    color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
            }
            Switch(
                checked = state.biometric,
                onCheckedChange = { onChange(state.copy(biometric = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = sw.onPrimary,
                    checkedTrackColor = sw.primary,
                ),
            )
        }
    }
}

// ─── Step 3: Privacy ───────────────────────────────────────────
@Composable
fun OnbPrivacyScreen(onNext: () -> Unit) {
    val sw = SwTheme.colors
    OnboardingShell(
        step = 3, total = 4,
        hero = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    // Hero squircle reduced from 180dp → 140dp so all 4 onboarding
                    // steps fit a single viewport without scroll on standard phones.
                    .size(140.dp)
                    .clip(RoundedCornerShape(46.dp))
                    .background(sw.primaryContainer),
            ) {
                Icon(Icons.Outlined.Shield, null, tint = sw.primary, modifier = Modifier.size(100.dp))
                Icon(Icons.Outlined.Check, null, tint = sw.primary, modifier = Modifier.size(40.dp))
            }
        },
        title = stringResource(R.string.onb_step3_title),
        subtitle = stringResource(R.string.onb_step3_subtitle),
        primaryLabel = stringResource(R.string.onb_step3_primary),
        onPrimary = onNext,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrivacyPoint(Icons.Outlined.Shield,
                stringResource(R.string.onb_step3_p1_title),
                stringResource(R.string.onb_step3_p1_sub))
            PrivacyPoint(Icons.Outlined.Person,
                stringResource(R.string.onb_step3_p2_title),
                stringResource(R.string.onb_step3_p2_sub))
            PrivacyPoint(Icons.Outlined.ContentCopy,
                stringResource(R.string.onb_step3_p3_title),
                stringResource(R.string.onb_step3_p3_sub))
        }
    }
}

@Composable
private fun PrivacyPoint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sub: String,
) {
    val sw = SwTheme.colors
    Row(verticalAlignment = Alignment.Top) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(sw.primaryContainer),
        ) { Icon(icon, null, tint = sw.onPrimaryContainer, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = sw.ink, style = SwType.LabelStrong.copy(fontSize = 14.sp))
            Text(sub, color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 12.sp))
        }
    }
}

// ─── Step 4: First account ─────────────────────────────────────
@Composable
fun OnbFirstAccountScreen(
    state: OnboardingUi,
    onChange: (OnboardingUi) -> Unit,
    onDone: () -> Unit,
) {
    val sw = SwTheme.colors
    val tCash = stringResource(R.string.onb_type_cash)
    val tBank = stringResource(R.string.onb_type_bank)
    val tWallet = stringResource(R.string.onb_type_ewallet)
    val types = listOf(
        Triple(tCash, "cash", Icons.Outlined.Payments),
        Triple(tBank, "bank", Icons.Outlined.AccountBalance),
        Triple(tWallet, "ewallet", Icons.Outlined.AccountBalanceWallet),
    )
    val defaultName = stringResource(R.string.onb_step4_account_default_name)
    OnboardingShell(
        step = 4, total = 4,
        hero = {
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(sw.primary)
                    .padding(18.dp),
            ) {
                // Decorative leaf in corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 20.dp),
                ) {
                    // Watermark — swap squircle/leaf so the cream squircle reads on dark green
                    LogoDaun(
                        sizeDp = 108,
                        bg = sw.onPrimary.copy(alpha = 0.18f),
                        leaf = sw.primary.copy(alpha = 0.18f),
                        vein = sw.onPrimary.copy(alpha = 0.18f),
                    )
                }
                Column {
                    Text(
                        stringResource(R.string.onb_step4_account_prefix, state.accountType).uppercase(),
                        color = sw.onPrimary.copy(alpha = 0.8f),
                        style = SwType.SectionLabel.copy(fontSize = 10.sp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        state.accountName.ifEmpty { defaultName },
                        color = sw.onPrimary,
                        style = SwType.H2.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(10.dp))
                    // Proto renders SW_FORMAT.rp inline — plain text, equal-size "Rp 0",
                    // not the dimmed/shrunk prefix that RupiahText emits.
                    Text(
                        text = "Rp " + java.text.NumberFormat
                            .getInstance(java.util.Locale("id", "ID"))
                            .format(state.accountBalance),
                        color = sw.onPrimary,
                        style = SwType.AmountL.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
                        ),
                    )
                }
            }
        },
        title = stringResource(R.string.onb_step4_title),
        subtitle = stringResource(R.string.onb_step4_subtitle),
        primaryLabel = stringResource(R.string.onb_step4_primary),
        onPrimary = onDone,
        secondaryLabel = stringResource(R.string.onb_step4_secondary),
        onSecondary = onDone,
    ) {
        SwField(
            value = state.accountName,
            onValueChange = { onChange(state.copy(accountName = it)) },
            label = stringResource(R.string.onb_step4_name_label),
            placeholder = stringResource(R.string.onb_step4_name_placeholder),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.onb_step4_type_label),
            color = sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            types.forEach { (label, _, icon) ->
                val active = state.accountType == label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) sw.primaryContainer else sw.surface)
                        .border(
                            1.5.dp,
                            if (active) sw.primary else sw.border,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable { onChange(state.copy(accountType = label)) }
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                ) {
                    Icon(icon, null, tint = sw.ink, modifier = Modifier.size(22.dp))
                    Text(label, color = sw.ink, style = SwType.LabelStrong.copy(fontSize = 12.sp))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        SwField(
            value = if (state.accountBalance == 0L) "" else state.accountBalance.toRupiah(prefix = ""),
            onValueChange = {
                val parsed = it.filter { ch -> ch.isDigit() }.toLongOrNull() ?: 0L
                onChange(state.copy(accountBalance = parsed))
            },
            label = stringResource(R.string.onb_step4_balance_label),
            prefix = "Rp", rupiah = true,
            placeholder = "0",
            hint = stringResource(R.string.onb_step4_balance_hint),
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
        )
    }
}
