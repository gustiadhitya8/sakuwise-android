package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "land_tax_payments",
    foreignKeys = [
        ForeignKey(
            entity = LandAssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["asset_land_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("asset_land_id")],
)
data class LandTaxPaymentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "asset_land_id") val assetLandId: String,
    @ColumnInfo(name = "payment_date") val paymentDate: LocalDate,
    @ColumnInfo(name = "amount") val amount: Long,
    @ColumnInfo(name = "account_id") val accountId: String?,
    @ColumnInfo(name = "photo_blob") val photoBlob: ByteArray?,
    @ColumnInfo(name = "note") val note: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LandTaxPaymentEntity) return false
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
