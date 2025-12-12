package com.example.yakovleva_zd7_v3

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val clientId: Int,
    var status: String = "Новый",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val assignedWorkerId: Int? = null,
    val totalAmount: Double = 0.0
)