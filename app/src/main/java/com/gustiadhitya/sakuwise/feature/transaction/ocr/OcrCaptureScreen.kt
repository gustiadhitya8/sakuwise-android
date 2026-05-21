package com.gustiadhitya.sakuwise.feature.transaction.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OcrStage {
    data object Idle : OcrStage
    data object Processing : OcrStage
    data class Ready(val draft: ReceiptDraft) : OcrStage
    data class Failure(val message: String) : OcrStage
}

@HiltViewModel
class OcrCaptureViewModel @Inject constructor(
    private val ocr: OcrService,
) : ViewModel() {
    private val _stage = MutableStateFlow<OcrStage>(OcrStage.Idle)
    val stage: StateFlow<OcrStage> = _stage

    fun process(bitmap: Bitmap) {
        _stage.value = OcrStage.Processing
        viewModelScope.launch {
            runCatching {
                // Persist the photo BLOB alongside the parsed text (PRD §7.11).
                // We encode the SAME bitmap the OCR engine sees — the rescale
                // happens inside the helper if needed.
                val jpeg = com.gustiadhitya.sakuwise.core.common.ImageCompression
                    .toCompressedJpeg(bitmap)
                ocr.extractReceipt(bitmap).copy(photoBlob = jpeg)
            }
                .onSuccess { _stage.value = OcrStage.Ready(it) }
                .onFailure {
                    _stage.value = OcrStage.Failure(it.message ?: "OCR gagal — coba foto ulang.")
                }
        }
    }

    fun reset() { _stage.value = OcrStage.Idle }
}

/**
 * OcrCaptureScreen — uses the system camera (TakePicturePreview) to grab a
 * Bitmap, runs ML Kit text recognition + Indonesian receipt parser, then
 * hands the resulting draft back via [onComplete] for prefill into the
 * Expense form.
 *
 * No CameraX dependency required — the system intent already handles preview,
 * focus, and capture. Result is an in-memory thumbnail Bitmap (~1MP) which is
 * adequate for receipt OCR.
 */
@Composable
fun OcrCaptureScreen(
    onClose: () -> Unit,
    onComplete: (ReceiptDraft) -> Unit,
    viewModel: OcrCaptureViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val ctx = LocalContext.current
    val stage by viewModel.stage.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap -> if (bitmap != null) viewModel.process(bitmap) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) cameraLauncher.launch(null) }

    LaunchedEffect(Unit) {
        if (stage is OcrStage.Idle) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) cameraLauncher.launch(null)
            else permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(sw.bg)) {
        // Top bar — per proto OCR review screen: back + title + primary
        // Lanjut pill on the right (only visible when a draft is Ready).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = SwSpace.pageH, top = 6.dp, bottom = 12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClose),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack, "Kembali",
                    tint = sw.ink,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                if (stage is OcrStage.Ready) "Review Struk" else "Scan Struk",
                color = sw.ink,
                style = SwType.H1.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp).weight(1f),
            )
            if (stage is OcrStage.Ready) {
                val s = stage as OcrStage.Ready
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(sw.primary)
                        .clickable { onComplete(s.draft) }
                        .padding(horizontal = 16.dp),
                ) {
                    Text("Lanjut", color = sw.onPrimary,
                        style = SwType.LabelStrong.copy(fontSize = 13.sp,
                            fontWeight = FontWeight.Bold))
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SwSpace.pageH),
        ) {
            when (val s = stage) {
                is OcrStage.Idle -> IdleHero(sw.primaryContainer, sw.onPrimaryContainer)
                is OcrStage.Processing -> ProcessingHero()
                is OcrStage.Ready -> ReadyBody(draft = s.draft)
                is OcrStage.Failure -> FailureBody(
                    message = s.message,
                    onRetake = {
                        viewModel.reset()
                        cameraLauncher.launch(null)
                    },
                )
            }
        }
    }
}

@Composable
private fun IdleHero(bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CameraAlt, null, tint = fg, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Menyiapkan kamera…", color = fg, style = SwType.Body.copy(fontSize = 14.sp))
        }
    }
}

@Composable
private fun ProcessingHero() {
    val sw = SwTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(sw.surface),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = sw.primary)
            Spacer(Modifier.height(12.dp))
            Text("Membaca struk…", color = sw.inkMuted,
                style = SwType.Body.copy(fontSize = 14.sp))
        }
    }
}

/**
 * Review-Struk panel per design/sakuwise-screens/light/47-ocr-review.png.
 * Renders a success banner, then a card-per-field with the parsed value
 * and a confidence chip (tinggi/sedang/rendah), and an info banner with the
 * next-step copy. The header's "Lanjut" pill is the primary CTA — no in-body
 * buttons (proto has none, only the top-right pill drives navigation).
 */
@Composable
private fun ReadyBody(draft: ReceiptDraft) {
    val sw = SwTheme.colors
    val detected = listOf(draft.merchant != null,
        draft.totalAmount != null, draft.date != null).count { it }
    // Success banner — green chip + "Berhasil dibaca" + "N field terdeteksi".
    SwCard(padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(sw.successSoft),
            ) {
                Icon(Icons.Outlined.Check, null, tint = sw.success,
                    modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
            Column {
                Text("Berhasil dibaca", color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 15.sp,
                        fontWeight = FontWeight.Bold))
                Text("$detected field terdeteksi · cek sebelum simpan",
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp))
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    val perFieldHigh = draft.confidence == OcrConfidence.High
    val perFieldMed = draft.confidence == OcrConfidence.Medium
    OcrDraftFieldCard(
        label = "Merchant",
        value = draft.merchant ?: "—",
        present = draft.merchant != null,
        confidence = if (perFieldHigh) "tinggi" else if (perFieldMed) "sedang" else "rendah",
    )
    Spacer(Modifier.height(10.dp))
    OcrDraftFieldCard(
        label = "Nominal",
        value = draft.totalAmount?.let { "Rp ${it.toRupiahShort(prefix = "")}" } ?: "—",
        present = draft.totalAmount != null,
        confidence = if (perFieldHigh) "tinggi" else if (perFieldMed) "sedang" else "rendah",
    )
    Spacer(Modifier.height(10.dp))
    OcrDraftFieldCard(
        label = "Tanggal",
        value = draft.date?.toString() ?: "—",
        present = draft.date != null,
        // Date is the weakest signal in IndonesianReceiptParser — bias one
        // notch down from the overall confidence per practical experience.
        confidence = if (perFieldHigh) "sedang" else if (perFieldMed) "sedang" else "rendah",
    )
    Spacer(Modifier.height(16.dp))
    // Info banner — infoSoft tinted card with the next-step hint.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(sw.infoSoft)
            .padding(14.dp),
    ) {
        Text(
            "Tap Lanjut untuk buka form pengeluaran. Field yang terdeteksi sudah di-prefill. Foto struk otomatis di-attach (terkompresi ~200 KB).",
            color = sw.ink,
            style = SwType.Body.copy(fontSize = 13.sp, lineHeight = 18.sp),
        )
    }
}

@Composable
private fun OcrDraftFieldCard(
    label: String, value: String, present: Boolean, confidence: String,
) {
    val sw = SwTheme.colors
    val (chipBg, chipFg) = when (confidence) {
        "tinggi" -> sw.successSoft to sw.success
        "sedang" -> sw.warningSoft to sw.warning
        else -> sw.dangerSoft to sw.danger
    }
    SwCard(padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label.uppercase(), color = sw.inkSubtle,
                    style = SwType.SectionLabel.copy(fontSize = 11.sp,
                        fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBg)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(confidence, color = chipFg,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp,
                            fontWeight = FontWeight.Bold))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                value, color = if (present) sw.ink else sw.inkMuted,
                style = SwType.LabelStrong.copy(fontSize = 17.sp,
                    fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
            )
        }
    }
}

@Composable
private fun FailureBody(message: String, onRetake: () -> Unit) {
    val sw = SwTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(sw.dangerSoft)
            .padding(20.dp),
    ) {
        Column {
            Icon(Icons.Outlined.WarningAmber, null, tint = sw.danger,
                modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text("Gagal membaca struk", color = sw.danger,
                style = SwType.H3.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
            Text(message, color = sw.ink,
                style = SwType.LabelSmall.copy(fontSize = 12.sp))
        }
    }
    Spacer(Modifier.height(16.dp))
    SwButton(text = "Foto Ulang", onClick = onRetake)
}

