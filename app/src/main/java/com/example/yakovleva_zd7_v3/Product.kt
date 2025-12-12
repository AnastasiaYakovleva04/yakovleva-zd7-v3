package com.example.yakovleva_zd7_v3

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("image_path") val imagePath: String = ""
)