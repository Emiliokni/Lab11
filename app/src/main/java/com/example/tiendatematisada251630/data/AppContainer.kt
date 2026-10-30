package com.example.tiendatematisada251630.data

import android.content.Context

interface AppContainer {
    val productRepository: ProductRepository
    val cartRepository: StoreCartRepository
    val settingsRepository: SettingsRepository
    val orderRepository: RemoteOrderRepository
    val authRepository: AuthRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: StoreDatabase by lazy {
        StoreDatabase.get(context)
    }

    private val preferences: CatalogPreferences by lazy {
        CatalogPreferences(context)
    }

    override val productRepository: ProductRepository by lazy {
        FirestoreProductRepository()
    }

    override val cartRepository: StoreCartRepository by lazy {
        RoomStoreCartRepository(database.storeDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(preferences)
    }

    override val orderRepository: RemoteOrderRepository by lazy {
        MockApiOrderRepository()
    }

    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }
}
