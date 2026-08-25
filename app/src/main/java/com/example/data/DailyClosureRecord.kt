package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_closures")
data class DailyClosureRecord(
    @PrimaryKey
    val dateString: String, // e.g. "2026-08-25"
    val closedAtTimestamp: Long = System.currentTimeMillis(),
    val totalRevenue: Long,
    val totalItems: Int,
    val totalTransactions: Int,
    val notes: String = ""
)
