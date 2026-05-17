package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gustiadhitya.sakuwise.core.model.GoldAssetStatus
import java.time.LocalDate

@Entity(tableName = "gold_assets")
data class GoldAssetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "purchase_date") val purchaseDate: LocalDate,
    @ColumnInfo(name = "weight_gram") val weightGram: Double,
    @ColumnInfo(name = "serial") val serial: String?,
    @ColumnInfo(name = "purchase_price") val purchasePrice: Long,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "photo_blob") val photoBlob: ByteArray?,
    @ColumnInfo(name = "status") val status: GoldAssetStatus,
    @ColumnInfo(name = "sold_date") val soldDate: LocalDate?,
    @ColumnInfo(name = "sold_price") val soldPrice: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GoldAssetEntity) return false
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
