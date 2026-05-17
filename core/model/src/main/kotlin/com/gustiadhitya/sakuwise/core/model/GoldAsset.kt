package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

data class GoldAsset(
    val id: String,
    val purchaseDate: LocalDate,
    val weightGram: Double,
    val serial: String?,
    val purchasePrice: Long,
    val note: String?,
    val photoBlob: ByteArray?,
    val status: GoldAssetStatus,
    val soldDate: LocalDate?,
    val soldPrice: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GoldAsset) return false
        return id == other.id && purchaseDate == other.purchaseDate &&
            weightGram == other.weightGram && serial == other.serial &&
            purchasePrice == other.purchasePrice && note == other.note &&
            photoBlob.contentEquals(other.photoBlob) &&
            status == other.status && soldDate == other.soldDate && soldPrice == other.soldPrice
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + purchaseDate.hashCode()
        result = 31 * result + weightGram.hashCode()
        result = 31 * result + (serial?.hashCode() ?: 0)
        result = 31 * result + purchasePrice.hashCode()
        result = 31 * result + (note?.hashCode() ?: 0)
        result = 31 * result + (photoBlob?.contentHashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + (soldDate?.hashCode() ?: 0)
        result = 31 * result + (soldPrice?.hashCode() ?: 0)
        return result
    }
}
