package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asset_gold")
data class AssetGoldEntity(
    @PrimaryKey val id: String,
    val purchaseEpochDay: Long,
    /** Weight in milligrams (gram × 1000). Long so 0.5g = 500, 10g = 10_000.
     *  Stored as integer to avoid floating-point rounding when summing
     *  totals across many pieces. Convert to grams with weightMilliGram / 1000.0
     *  only at display time. */
    val weightMilliGram: Long,
    val serial: String?,
    val buyPrice: Long,
    val note: String?,
    val photoBlob: ByteArray?,
    val status: String,         // "held" | "sold"
    val soldEpochDay: Long?,
    val soldPrice: Long?,
) {
    override fun equals(other: Any?): Boolean = (other as? AssetGoldEntity)?.id == id
    override fun hashCode(): Int = id.hashCode()
}

@Entity(tableName = "asset_land")
data class AssetLandEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
    val sertifikatId: String,
    val sizeM2: Int,
    val buyPrice: Long,
    val currentValue: Long?,
    val note: String?,
    val photoBlob: ByteArray?,
    val status: String,         // "held" | "sold"
    val soldEpochDay: Long?,
    val soldPrice: Long?,
    // Defaults to 0 in the DB schema (epoch day 0 = 1970-01-01) so pre-
    // migration rows have a stable value. The mapper coerces 0 → today on
    // read so the UI doesn't render "1970" for legacy entries.
    val purchaseEpochDay: Long = 0L,
) {
    override fun equals(other: Any?): Boolean = (other as? AssetLandEntity)?.id == id
    override fun hashCode(): Int = id.hashCode()
}

@Entity(tableName = "land_tax_payments")
data class LandTaxPaymentEntity(
    @PrimaryKey val id: String,
    val assetLandId: String,
    val payEpochDay: Long,
    val amount: Long,
    val accountId: String?,
    val photoBlob: ByteArray?,
    val note: String?,
) {
    override fun equals(other: Any?): Boolean = (other as? LandTaxPaymentEntity)?.id == id
    override fun hashCode(): Int = id.hashCode()
}

@Entity(tableName = "asset_deposit")
data class AssetDepositEntity(
    @PrimaryKey val id: String,
    val name: String,
    val typeLabel: String,       // "DPLK" | "BPJSTK" | "Deposito" | "Other"
    val institutionInfo: String?,
    val note: String?,
    val status: String,          // "active" | "closed"
)

@Entity(tableName = "asset_deposit_snapshots")
data class AssetDepositSnapshotEntity(
    @PrimaryKey val id: String,
    val assetDepositId: String,
    val snapshotEpochDay: Long,
    val balance: Long,
    val note: String?,
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String,
    val counterparty: String,
    val direction: String,        // "i_owe" | "owed_to_me"
    val principal: Long,
    val startEpochDay: Long,
    val dueEpochDay: Long?,
    val status: String,           // "open" | "closed"
    val note: String?,
)

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey val id: String,
    val debtId: String,
    val payEpochDay: Long,
    val amount: Long,
    val accountId: String?,
    val transactionId: String?,
    val note: String?,
)
