package com.gustiadhitya.sakuwise.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.common.toRupiah
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

/**
 * Shared form shell for Expense / Income / Transfer.
 * - Top bar with Back + title + Save (primary) button
 * - Hero card (color per type, big amount input)
 * - Scrollable field stack passed via [content]
 * - Sticky bottom CTA
 */
@Composable
fun TxnFormShell(
    title: String,
    heroBg: Color,
    heroFg: Color,
    heroLabel: String,
    amount: Long,
    onAmountChange: (Long) -> Unit,
    heroSubtitle: String? = null,
    onCancel: () -> Unit,
    saveLabel: String,
    saveEnabled: Boolean,
    onSave: () -> Unit,
    content: @Composable () -> Unit,
) {
    val sw = SwTheme.colors
    Column(modifier = Modifier.fillMaxSize().background(sw.bg)) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onCancel),
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Batal", tint = sw.ink)
            }
            Text(
                title,
                color = sw.ink,
                style = SwType.H2.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (saveEnabled) sw.primary else sw.primary.copy(alpha = 0.4f))
                    .clickable(enabled = saveEnabled, onClick = onSave)
                    .padding(horizontal = 14.dp),
            ) {
                Text(
                    saveLabel,
                    color = sw.onPrimary,
                    style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // Hero amount card — per screens-addtxn.jsx:77-111. r22, big
            // bold amount (44sp 800) with a smaller "Rp" prefix (22sp 600).
            // Letter spacing tightened slightly so the number reads as a
            // single visual unit on the alloc-colored field.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(heroBg)
                    .padding(horizontal = 22.dp, vertical = 24.dp),
            ) {
                Column {
                    Text(
                        heroLabel.uppercase(),
                        color = heroFg.copy(alpha = 0.78f),
                        style = SwType.SectionLabel.copy(fontSize = 11.sp,
                            fontWeight = FontWeight.Bold),
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "Rp ",
                            color = heroFg.copy(alpha = 0.85f),
                            fontSize = 22.sp, lineHeight = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        BasicTextField(
                            value = if (amount == 0L) "" else java.text.NumberFormat
                                .getInstance(java.util.Locale("id", "ID")).format(amount),
                            onValueChange = { raw ->
                                val digits = raw.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                onAmountChange(digits)
                            },
                            singleLine = true,
                            cursorBrush = SolidColor(heroFg),
                            textStyle = TextStyle(
                                color = heroFg,
                                fontSize = 44.sp,
                                lineHeight = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFeatureSettings = "tnum",
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decorationBox = { inner ->
                                if (amount == 0L) {
                                    Text(
                                        "0",
                                        style = TextStyle(
                                            color = heroFg.copy(alpha = 0.45f),
                                            fontSize = 44.sp,
                                            lineHeight = 48.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFeatureSettings = "tnum",
                                        ),
                                    )
                                }
                                inner()
                            },
                        )
                    }
                    if (heroSubtitle != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            heroSubtitle,
                            color = heroFg.copy(alpha = 0.78f),
                            style = SwType.Body.copy(fontSize = 12.sp,
                                fontWeight = FontWeight.Medium),
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            content()
            Spacer(Modifier.height(80.dp))
        }
    }
}

/**
 * "FieldButton" — a read-only row that looks like an input but opens a picker.
 *
 * The proto shows two visual variants:
 *  1. Plain: label only (single line).
 *  2. Rich: chip/dot + primary text + a muted subtitle (e.g. "Makan di Luar
 *     · Wants" for plan items, "Saldo: Rp 280.000" for accounts).
 *
 * `subtitle` and `leadingContent` cover variant 2; if both are null we fall
 * back to the plain row (matches old API for callers we haven't migrated).
 */
@Composable
fun FieldButton(
    label: String,
    value: String,
    placeholder: String = "Pilih…",
    required: Boolean = false,
    leadingIcon: ImageVector? = null,
    subtitle: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    val isEmpty = value.isBlank()
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
        Text(
            buildString {
                append(label)
                if (required) append(" *")
            },
            color = if (required && isEmpty) sw.danger else sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        val rich = subtitle != null || leadingContent != null
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .let { if (rich) it else it.height(52.dp) }
                .clip(RoundedCornerShape(12.dp))
                .background(sw.surface)
                .border(
                    1.5.dp,
                    if (required && isEmpty) sw.danger else sw.border,
                    RoundedCornerShape(12.dp),
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = if (rich) 10.dp else 0.dp),
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(Modifier.size(width = 10.dp, height = 1.dp))
            } else if (leadingIcon != null) {
                Icon(leadingIcon, null, tint = sw.inkMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    value.ifBlank { placeholder },
                    color = if (isEmpty) sw.inkSubtle else sw.ink,
                    style = if (rich)
                        SwType.LabelStrong.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    else
                        SwType.BodyL.copy(fontSize = 15.sp),
                )
                if (subtitle != null && !isEmpty) {
                    Text(
                        subtitle,
                        color = sw.inkMuted,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp,
                            fontFeatureSettings = "tnum"),
                    )
                }
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = sw.inkSubtle, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * Round chip that goes in [FieldButton.leadingContent]. Used for category
 * dots (single letter) and account icons (currency wallet). Defaults to the
 * primaryContainer / onPrimaryContainer pair so chips read on both light
 * and dark themes without per-call tinting.
 */
@Composable
fun FieldChip(
    size: Int = 32,
    bg: Color = SwTheme.colors.primaryContainer,
    fg: Color = SwTheme.colors.onPrimaryContainer,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3).dp))
            .background(bg),
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides fg,
        ) { content() }
    }
}
