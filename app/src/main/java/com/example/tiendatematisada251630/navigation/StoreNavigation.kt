package com.example.tiendatematisada251630.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.tiendatematisada251630.model.StoreUiState
import com.example.tiendatematisada251630.data.CatalogOrder
import com.example.tiendatematisada251630.model.BillingType
import com.example.tiendatematisada251630.model.PaymentMethod
import com.example.tiendatematisada251630.model.orderLines
import com.example.tiendatematisada251630.model.orderTotalCents
import com.example.tiendatematisada251630.model.totalOrderUnits
import com.example.tiendatematisada251630.ui.CatalogScreen
import com.example.tiendatematisada251630.ui.CheckoutScreen
import com.example.tiendatematisada251630.ui.OrderScreen
import com.example.tiendatematisada251630.ui.OrderConfirmationScreen
import com.example.tiendatematisada251630.ui.ProducerProfileScreen
import com.example.tiendatematisada251630.ui.ProductDetailScreen
import kotlinx.coroutines.launch

@Composable
fun StoreNavigation(
    uiState: StoreUiState,
    onToggleFavorite: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onCatalogOrderChange: (CatalogOrder) -> Unit,
    onAddToOrder: (String, Int) -> Unit,
    onDecreaseOrder: (String) -> Unit,
    onRemoveFromOrder: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirmOrder: suspend () -> Boolean,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(StoreNavKey.Catalog)
    val catalogGridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    fun navigateBack() {
        if (backStack.lastOrNull() == StoreNavKey.Confirmation) {
            while (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        } else if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    fun navigateToOrder() {
        if (backStack.lastOrNull() != StoreNavKey.Order) {
            backStack.add(StoreNavKey.Order)
        }
    }

    fun navigateToCatalog() {
        while (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { navigateBack() },
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith
                fadeOut(animationSpec = tween(160))
        },
        popTransitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith
                fadeOut(animationSpec = tween(160))
        },
        entryProvider = entryProvider {
            entry<StoreNavKey.Catalog> {
                CatalogScreen(
                    products = uiState.products,
                    favoriteProductIds = uiState.favoriteProductIds,
                    query = uiState.query,
                    catalogOrder = uiState.catalogOrder,
                    totalOrderUnits = uiState.totalOrderUnits,
                    gridState = catalogGridState,
                    onQueryChange = onQueryChange,
                    onCatalogOrderChange = onCatalogOrderChange,
                    onProductClick = { productId ->
                        backStack.add(StoreNavKey.Detail(productId))
                    },
                    onFavoriteClick = onToggleFavorite,
                    onOrderClick = ::navigateToOrder
                )
            }

            entry<StoreNavKey.Detail> { key ->
                val product = uiState.products.find { it.id == key.productId }
                if (product == null) {
                    MissingContent(message = "Producto no encontrado")
                } else {
                    val producer = uiState.producers.find { it.id == product.producerId }
                    ProductDetailScreen(
                        product = product,
                        producerName = producer?.name ?: "Productor desconocido",
                        isFavorite = product.id in uiState.favoriteProductIds,
                        quantityInOrder = uiState.orderQuantities[product.id] ?: 0,
                        totalOrderUnits = uiState.totalOrderUnits,
                        message = uiState.message,
                        onFavoriteClick = { onToggleFavorite(product.id) },
                        onProducerClick = {
                            backStack.add(StoreNavKey.Profile(product.producerId))
                        },
                        onAddToOrder = { onAddToOrder(product.id, 1) },
                        onOrderClick = ::navigateToOrder,
                        onBack = ::navigateBack
                    )
                }
            }

            entry<StoreNavKey.Profile> { key ->
                val producer = uiState.producers.find { it.id == key.producerId }
                if (producer == null) {
                    MissingContent(message = "Productor no encontrado")
                } else {
                    ProducerProfileScreen(producer = producer, onBack = ::navigateBack)
                }
            }

            entry<StoreNavKey.Order> {
                OrderScreen(
                    orderLines = uiState.orderLines,
                    totalCents = uiState.orderTotalCents,
                    message = uiState.message,
                    onIncrease = { productId -> onAddToOrder(productId, 1) },
                    onDecrease = onDecreaseOrder,
                    onRemove = onRemoveFromOrder,
                    onCheckout = {
                        if (uiState.totalOrderUnits > 0 && backStack.lastOrNull() != StoreNavKey.Checkout) {
                            backStack.add(StoreNavKey.Checkout)
                        }
                    },
                    onCatalog = ::navigateToCatalog,
                    onBack = ::navigateBack
                )
            }

            entry<StoreNavKey.Checkout> {
                CheckoutScreen(
                    checkout = uiState.checkout,
                    totalOrderUnits = uiState.totalOrderUnits,
                    totalCents = uiState.orderTotalCents,
                    onFullNameChange = onFullNameChange,
                    onPhoneChange = onPhoneChange,
                    onBillingTypeChange = onBillingTypeChange,
                    onNitChange = onNitChange,
                    onBusinessNameChange = onBusinessNameChange,
                    onPaymentMethodChange = onPaymentMethodChange,
                    onConfirm = {
                        scope.launch {
                            if (onConfirmOrder()) {
                                backStack.add(StoreNavKey.Confirmation)
                            }
                        }
                    },
                    onBack = ::navigateBack
                )
            }

            entry<StoreNavKey.Confirmation> {
                val receipt = uiState.lastReceipt
                if (receipt == null) {
                    MissingContent(message = "No hay un recibo disponible")
                } else {
                    OrderConfirmationScreen(
                        receipt = receipt,
                        onCatalog = ::navigateToCatalog
                    )
                }
            }
        }
    )
}

@Composable
private fun MissingContent(message: String) {
    Text(message)
}
