package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

data class LandAsset(
    val id: String,
    val name: String,
    val location: String?,
    val sertifikatId: String?,
    val areaSqm: Double?,
    val purchasePrice: Long,
    val currentValue: Long?,
    val note: String?,
    val photoBlob: ByteArray?,
    val status: LandAssetStatus,
    val soldDate: LocalDate?,
    val soldPrice: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LandAsset) return false
        return id == other.id && name == other.name && location == other.location &&
            sertifikatId == other.sertifikatId && areaSqm == other.areaSqm &&
            purchasePrice == other.purchasePrice && currentValue == other.currentValue &&
            note == other.note && photoBlob.contentEquals(other.photoBlob) &&
            status == other.status && soldDate == other.soldDate && soldPrice == other.soldPrice
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + (sertifikatId?.hashCode() ?: 0)
        result = 31 * result + (areaSqm?.hashCode() ?: 0)
        result = 31 * result + purchasePrice.hashCode()
        result = 31 * result + (currentValue?.hashCode() ?: 0)
        result = 31 * result + (note?.hashCode() ?: 0)
        result = 31 * result + (photoBlob?.contentHashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + (soldDate?.hashCode() ?: 0)
        result = 31 * result + (soldPrice?.hashCode() ?: 0)
        return result
    }
}
