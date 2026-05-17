package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gustiadhitya.sakuwise.core.model.LandAssetStatus
import java.time.LocalDate

@Entity(tableName = "land_assets")
data class LandAssetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "location") val location: String?,
    @ColumnInfo(name = "sertifikat_id") val sertifikatId: String?,
    @ColumnInfo(name = "area_sqm") val areaSqm: Double?,
    @ColumnInfo(name = "purchase_price") val purchasePrice: Long,
    @ColumnInfo(name = "current_value") val currentValue: Long?,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "photo_blob") val photoBlob: ByteArray?,
    @ColumnInfo(name = "status") val status: LandAssetStatus,
    @ColumnInfo(name = "sold_date") val soldDate: LocalDate?,
    @ColumnInfo(name = "sold_price") val soldPrice: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LandAssetEntity) return false
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
