package com.gustiadhitya.sakuwise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gustiadhitya.sakuwise.core.designsystem.component.FieldButton
import com.gustiadhitya.sakuwise.core.designsystem.component.PinInput
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButtonSize
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.component.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.component.SwField
import com.gustiadhitya.sakuwise.core.designsystem.component.SwTopBar
import com.gustiadhitya.sakuwise.core.designsystem.theme.CaptionStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing

@Composable
fun M5aGalleryScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            SwTopBar(title = "M5a Components", subtitle = "Design System Gallery")

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                Section("SwButton — Variants")
                SwButtonVariant.entries.forEach { variant ->
                    SwButton(text = variant.name, onClick = {}, variant = variant)
                }

                Section("SwButton — Sizes & States")
                SwButtonSize.entries.forEach { size ->
                    SwButton(text = "Size ${size.name}", onClick = {}, size = size)
                }
                SwButton(text = "Disabled", onClick = {}, enabled = false)
                SwButton(text = "Loading", onClick = {}, loading = true)
                SwButton(text = "No fill width", onClick = {}, fillWidth = false)

                Section("SwField")
                var fieldValue by remember { mutableStateOf("") }
                SwField(
                    value = fieldValue,
                    onValueChange = { fieldValue = it },
                    label = "Nama",
                    placeholder = "Masukkan nama",
                )
                SwField(
                    value = "",
                    onValueChange = {},
                    label = "Jumlah *",
                    placeholder = "0",
                    prefix = "Rp",
                    isError = true,
                    hint = "Jumlah tidak boleh kosong",
                )

                Section("FieldButton")
                FieldButton(
                    text = "GoPay",
                    label = "Akun *",
                    subText = "Saldo: Rp 280.000",
                    leadingIcon = Icons.Default.AccountBalance,
                    onClick = {},
                )
                FieldButton(
                    text = "15 Mei 2026",
                    label = "Tanggal",
                    subText = "2 hari lalu",
                    leadingIcon = Icons.Default.CalendarToday,
                    onClick = {},
                )

                Section("PinInput")
                var pin by remember { mutableStateOf("") }
                PinInput(value = pin, onValueChange = { pin = it })

                Section("SwCard")
                SwCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Static card content", style = MaterialTheme.typography.bodyMedium)
                }
                SwCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                    Text("Clickable card with press feedback", style = MaterialTheme.typography.bodyMedium)
                }

                Section("SwTopBar variants")
                SwCard(modifier = Modifier.fillMaxWidth(), noBorder = true, contentPadding = SakuwiseSpacing.xs) {
                    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs)) {
                        SwTopBar(title = "Beranda")
                        HorizontalDivider()
                        SwTopBar(title = "Beranda", subtitle = "Plan Mei · sisa 16 hari")
                        HorizontalDivider()
                        SwTopBar(
                            title = "Pengeluaran",
                            onBack = {},
                            rightAction = {
                                SwButton(text = "Simpan", onClick = {}, size = SwButtonSize.Sm, fillWidth = false)
                            },
                        )
                    }
                }

                // bottom padding
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(bottom = SakuwiseSpacing.xxxl),
                )
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(
        text = title,
        style = CaptionStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        modifier = Modifier.padding(top = SakuwiseSpacing.m),
    )
}
