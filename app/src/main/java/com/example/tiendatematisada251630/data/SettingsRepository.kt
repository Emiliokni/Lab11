package com.example.tiendatematisada251630.data

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val catalogOrder: Flow<CatalogOrder>
    suspend fun setCatalogOrder(order: CatalogOrder): Result<Unit>
}
