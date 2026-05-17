package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gustiadhitya.sakuwise.core.model.DebtDirection
import com.gustiadhitya.sakuwise.core.model.DebtStatus
import java.time.LocalDate

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "counterparty") val counterparty: String,
    @ColumnInfo(name = "direction") val direction: DebtDirection,
    @ColumnInfo(name = "principal") val principal: Long,
    @ColumnInfo(name = "date_opened") val dateOpened: LocalDate,
    @ColumnInfo(name = "expected_close_date") val expectedCloseDate: LocalDate?,
    @ColumnInfo(name = "status") val status: DebtStatus,
    @ColumnInfo(name = "note") val note: String?,
)
