package com.example.tiendatematisada251630.data

import kotlinx.coroutines.flow.Flow

interface StoreCartRepository {
    fun observeFavorites(): Flow<Set<String>>
    fun observeOrderQuantities(): Flow<Map<String, Int>>
    suspend fun toggleFavorite(productId: String): Result<Unit>
    suspend fun setOrderQuantity(productId: String, quantity: Int): Result<Unit>
    suspend fun clearOrder(): Result<Unit>
}
