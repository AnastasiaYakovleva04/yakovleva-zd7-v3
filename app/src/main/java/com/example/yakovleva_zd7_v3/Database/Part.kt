package com.example.yakovleva_zd7_v3

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parts")
data class Part(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val characteristics: String,
    val supplierId: Int,
    val price: Double
)