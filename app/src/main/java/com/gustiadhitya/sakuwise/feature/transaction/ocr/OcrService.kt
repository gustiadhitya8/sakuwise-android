package com.gustiadhitya.sakuwise.feature.transaction.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * OcrService — runs ML Kit Text Recognition on a Bitmap and parses the
 * extracted text via [IndonesianReceiptParser].
 *
 * All processing is on-device (Tech Solution §11.1). No image leaves the
 * phone.
 */
@Singleton
class OcrService @Inject constructor(
    private val parser: IndonesianReceiptParser,
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractReceipt(bitmap: Bitmap, rotation: Int = 0): ReceiptDraft {
        val raw = runRecognizer(bitmap, rotation)
        return parser.parse(raw)
    }

    private suspend fun runRecognizer(bitmap: Bitmap, rotation: Int): String =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, rotation)
            recognizer.process(image)
                .addOnSuccessListener { result -> cont.resume(result.text) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
}
