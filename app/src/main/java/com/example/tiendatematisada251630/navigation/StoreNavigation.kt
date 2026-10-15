package com.example.tiendatematisada251630.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.tiendatematisada251630.model.StoreUiState
import com.example.tiendatematisada251630.ui.CatalogScreen
import com.example.tiendatematisada251630.ui.ProducerProfileScreen
import com.example.tiendatematisada251630.ui.ProductDetailScreen

@Composable
fun StoreNavigation(
    uiState: StoreUiState,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(
        StoreNavKey.Catalog
    )

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            navigateBack()
        },
        entryProvider = entryProvider {
            entry<StoreNavKey.Catalog> {
                CatalogScreen(
                    products = uiState.products,
                    favoriteProductIds =
                        uiState.favoriteProductIds,
                    onProductClick = { productId ->
                        backStack.add(
                            StoreNavKey.Detail(productId)
                        )
                    },
                    onFavoriteClick = onToggleFavorite
                )
            }

            entry<StoreNavKey.Detail> { key ->
                val product = uiState.products.find {
                    it.id == key.productId
                }

                if (product == null) {
                    MissingContent(
                        message = "Producto no encontrado"
                    )
                } else {
                    val producer = uiState.producers.find {
                        it.id == product.producerId
                    }

                    ProductDetailScreen(
                        product = product,
                        producerName =
                            producer?.name ?: "Productor desconocido",
                        isFavorite =
                            product.id in uiState.favoriteProductIds,
                        onFavoriteClick = {
                            onToggleFavorite(product.id)
                        },
                        onProducerClick = {
                            backStack.add(
                                StoreNavKey.Profile(
                                    producerId = product.producerId
                                )
                            )
                        },
                        onBack = {
                            navigateBack()
                        }
                    )
                }
            }

            entry<StoreNavKey.Profile> { key ->
                val producer = uiState.producers.find {
                    it.id == key.producerId
                }

                if (producer == null) {
                    MissingContent(
                        message = "Productor no encontrado"
                    )
                } else {
                    ProducerProfileScreen(
                        producer = producer,
                        onBack = {
                            navigateBack()
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun MissingContent(
    message: String
) {
    Text(message)
}