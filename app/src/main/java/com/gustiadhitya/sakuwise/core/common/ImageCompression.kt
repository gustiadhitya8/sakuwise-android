package com.gustiadhitya.sakuwise.core.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Receipt / asset photos are persisted in the DB as JPEG BLOBs (PRD §7.11,
 * §7.4, §7.7, §7.8). To keep DB size bounded — Drive backup payload + on-disk
 * footprint — we cap the longest edge at 1600 px and re-compress to JPEG q=70.
 * Empirically that yields ~150-250 KB per receipt; PRD targets ~200 KB.
 */
object ImageCompression {
    private const val MAX_EDGE_PX = 1600
    private const val JPEG_QUALITY = 70

    /**
     * Down-scale [src] so its longest edge is ≤ [MAX_EDGE_PX], then encode to
     * JPEG bytes. Returns null if the bitmap couldn't be encoded (very rare).
     */
    fun toCompressedJpeg(src: Bitmap): ByteArray? {
        val scaled = downscale(src)
        return ByteArrayOutputStream().use { out ->
            if (scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                out.toByteArray()
            } else null
        }
    }

    /**
     * Decode [stream] into a Bitmap with `inSampleSize` tuned so the output's
     * longest edge is roughly [MAX_EDGE_PX]. Returns null on decode failure.
     * Used by the gallery picker — we never load the full-res image into RAM.
     */
    fun decodeBoundedFromStream(stream: InputStream): Bitmap? {
        val bytes = stream.use { it.readBytes() }
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOpts)
        val (w, h) = boundsOpts.outWidth to boundsOpts.outHeight
        if (w <= 0 || h <= 0) return null
        var sample = 1
        var maxEdge = maxOf(w, h)
        while (maxEdge / sample > MAX_EDGE_PX * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }

    private fun downscale(src: Bitmap): Bitmap {
        val maxEdge = maxOf(src.width, src.height)
        if (maxEdge <= MAX_EDGE_PX) return src
        val scale = MAX_EDGE_PX.toFloat() / maxEdge
        val matrix = Matrix().apply { postScale(scale, scale) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }
}
