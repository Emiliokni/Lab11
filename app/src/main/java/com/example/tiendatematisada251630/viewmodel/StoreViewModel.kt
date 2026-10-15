package com.example.tiendatematisada251630.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tiendatematisada251630.model.Producer
import com.example.tiendatematisada251630.model.Product
import com.example.tiendatematisada251630.model.StoreUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        StoreUiState(
            products = listOf(
                Product(
                    id = "coffee-1",
                    name = "Geisha de Acatenango",
                    description = "Café floral y cítrico cultivado a gran altura.",
                    price = 145.00,
                    producerId = "producer-1",
                    technicalDetails =
                        "Proceso lavado · Altitud: 1,750 m · Tueste medio"
                ),
                Product(
                    id = "coffee-2",
                    name = "Borbón de Huehuetenango",
                    description = "Café dulce con notas de chocolate y almendra.",
                    price = 110.00,
                    producerId = "producer-2",
                    technicalDetails =
                        "Proceso natural · Altitud: 1,650 m · Tueste medio"
                ),
                Product(
                    id = "coffee-3",
                    name = "Caturra de Cobán",
                    description = "Café equilibrado con notas de miel y frutos rojos.",
                    price = 98.00,
                    producerId = "producer-1",
                    technicalDetails =
                        "Proceso honey · Altitud: 1,450 m · Tueste medio claro"
                )
            ),
            producers = listOf(
                Producer(
                    id = "producer-1",
                    name = "Finca El Moriche",
                    role = "Productor de café",
                    location = "Acatenango, Guatemala",
                    description =
                        "Finca familiar especializada en café de altura y procesos sostenibles."
                ),
                Producer(
                    id = "producer-2",
                    name = "Cooperativa La Montaña",
                    role = "Cooperativa productora",
                    location = "Huehuetenango, Guatemala",
                    description =
                        "Cooperativa formada por pequeños productores de café de especialidad."
                )
            )
        )
    )

    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun toggleFavorite(productId: String) {
        _uiState.update { currentState ->
            val updatedFavorites =
                if (productId in currentState.favoriteProductIds) {
                    currentState.favoriteProductIds - productId
                } else {
                    currentState.favoriteProductIds + productId
                }

            currentState.copy(
                favoriteProductIds = updatedFavorites
            )
        }
    }
}