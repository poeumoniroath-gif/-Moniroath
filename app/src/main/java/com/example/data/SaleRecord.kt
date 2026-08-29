package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_records")
data class SaleRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: String,
    val productName: String,
    val unitPrice: Int,
    val quantity: Int,
    val totalPrice: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // e.g. "2026-08-25"
    val paymentMethod: String = "CASH" // "CASH" or "ABA"
)
