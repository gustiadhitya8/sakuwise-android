package com.gustiadhitya.sakuwise.core.model

import java.time.LocalDate

data class LandTaxPayment(
    val id: String,
    val assetLandId: String,
    val paymentDate: LocalDate,
    val amount: Long,
    val accountId: String?,
    val photoBlob: ByteArray?,
    val note: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LandTaxPayment) return false
        return id == other.id && assetLandId == other.assetLandId &&
            paymentDate == other.paymentDate && amount == other.amount &&
            accountId == other.accountId && photoBlob.contentEquals(other.photoBlob) &&
            note == other.note
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + assetLandId.hashCode()
        result = 31 * result + paymentDate.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (accountId?.hashCode() ?: 0)
        result = 31 * result + (photoBlob?.contentHashCode() ?: 0)
        result = 31 * result + (note?.hashCode() ?: 0)
        return result
    }
}
