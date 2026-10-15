package com.example.tiendatematisada251630.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface StoreNavKey : NavKey {

    @Serializable
    data object Catalog : StoreNavKey

    @Serializable
    data class Detail(
        val productId: String
    ) : StoreNavKey

    @Serializable
    data class Profile(
        val producerId: String
    ) : StoreNavKey
}