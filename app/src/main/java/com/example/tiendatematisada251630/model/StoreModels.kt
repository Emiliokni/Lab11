package com.example.tiendatematisada251630.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val producerId: String,
    val technicalDetails: String
)

data class Producer(
    val id: String,
    val name: String,
    val role: String,
    val location: String,
    val description: String
)

data class StoreUiState(
    val products: List<Product> = emptyList(),
    val producers: List<Producer> = emptyList(),
    val favoriteProductIds: Set<String> = emptySet()
)