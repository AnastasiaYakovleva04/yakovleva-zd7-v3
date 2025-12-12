package com.example.yakovleva_zd7_v3

import androidx.room.Entity

@Entity(tableName = "order_part", primaryKeys = ["orderId", "partId"])
data class OrderPart(
    val orderId: Long,
    val partId: Int,
    val quantity: Int = 1,
    val used: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)