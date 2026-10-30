package com.example.tiendatematisada251630.data

import com.example.tiendatematisada251630.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>
    suspend fun getProduct(id: String): Product?
    suspend fun reserveUnit(productId: String): Result<Unit>
    suspend fun releaseUnits(productId: String, quantity: Int): Result<Unit>
}
