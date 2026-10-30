package com.example.tiendatematisada251630.data

import kotlinx.coroutines.flow.Flow

class DataStoreSettingsRepository(
    private val preferences: CatalogPreferences
) : SettingsRepository {

    override val catalogOrder: Flow<CatalogOrder> = preferences.order

    override suspend fun setCatalogOrder(order: CatalogOrder): Result<Unit> {
        return runCatching {
            preferences.setOrder(order)
        }
    }
}
