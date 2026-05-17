package com.gustiadhitya.sakuwise.core.model

data class Allocation(
    val id: String,
    val planId: String,
    val name: AllocationName,
    val percentageTarget: Int,
)
