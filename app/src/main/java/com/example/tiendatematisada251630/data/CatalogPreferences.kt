package com.example.tiendatematisada251630.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Declarado una sola vez: toda la aplicación comparte la misma instancia.
private val Context.catalogDataStore by preferencesDataStore(name = "catalog_settings")

enum class CatalogOrder(val label: String) {
    ORIGINAL("Orden original"),
    PRICE("Precio: menor a mayor")
}

class CatalogPreferences(context: Context) {
    private val store = context.applicationContext.catalogDataStore
    private val orderKey = stringPreferencesKey("catalog_order")

    val order: Flow<CatalogOrder> = store.data.map { preferences ->
        CatalogOrder.entries.find { it.name == preferences[orderKey] } ?: CatalogOrder.ORIGINAL
    }

    suspend fun setOrder(value: CatalogOrder) {
        store.edit { it[orderKey] = value.name }
    }
}
