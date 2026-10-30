package com.example.tiendatematisada251630.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tiendatematisada251630.StoreApplication
import com.example.tiendatematisada251630.data.AuthRepository
import com.example.tiendatematisada251630.data.CatalogOrder
import com.example.tiendatematisada251630.data.ProductRepository
import com.example.tiendatematisada251630.data.RemoteOrderRepository
import com.example.tiendatematisada251630.data.SettingsRepository
import com.example.tiendatematisada251630.data.StoreCartRepository
import com.example.tiendatematisada251630.model.BillingType
import com.example.tiendatematisada251630.model.CheckoutUiState
import com.example.tiendatematisada251630.model.OrderReceipt
import com.example.tiendatematisada251630.model.PaymentMethod
import com.example.tiendatematisada251630.model.Producer
import com.example.tiendatematisada251630.model.StoreMessage
import com.example.tiendatematisada251630.model.StoreUiState
import com.example.tiendatematisada251630.model.formatQuetzales
import com.example.tiendatematisada251630.model.orderTotalCents
import com.example.tiendatematisada251630.model.totalOrderUnits
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StoreViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: StoreCartRepository,
    private val settingsRepository: SettingsRepository,
    private val remoteOrderRepository: RemoteOrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var nextMessageId = 0L
    private var nextOrderNumber = 1
    private val mutationMutex = Mutex()

    private val _uiState = MutableStateFlow(
        StoreUiState(producers = producers)
    )
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        initSession()
        observeData()
    }

    private fun initSession() {
        viewModelScope.launch {
            authRepository.signInAnonymously()
        }
    }

    fun retryLoadProducts() {
        _uiState.update { it.copy(isError = false, errorMessage = null, isLoaded = false) }
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                productRepository.observeProducts().catch { e ->
                    _uiState.update {
                        it.copy(
                            isError = true,
                            errorMessage = e.localizedMessage ?: "Error al cargar catálogo de Firestore",
                            isLoaded = true
                        )
                    }
                    emit(emptyList())
                },
                cartRepository.observeFavorites(),
                cartRepository.observeOrderQuantities(),
                settingsRepository.catalogOrder
            ) { products, favorites, orderQuantities, order ->
                Quadruple(products, favorites, orderQuantities, order)
            }.collect { (products, favorites, orderQuantities, order) ->
                if (products.isNotEmpty() || !_uiState.value.isError) {
                    _uiState.update { current ->
                        val knownIds = products.mapTo(hashSetOf()) { it.id }
                        current.copy(
                            products = products,
                            favoriteProductIds = favorites.intersect(knownIds),
                            orderQuantities = orderQuantities.filterKeys { it in knownIds },
                            catalogOrder = order,
                            isLoaded = true,
                            isError = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onCatalogOrderChange(order: CatalogOrder) {
        viewModelScope.launch { settingsRepository.setCatalogOrder(order) }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            cartRepository.toggleFavorite(productId)
        }
    }

    fun addToOrder(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val product = _uiState.value.products.find { it.id == productId }
                if (product == null) {
                    _uiState.update { it.copy(message = newMessage("El producto solicitado no existe.", true)) }
                    return@withLock
                }

                if (product.stock <= 0) {
                    _uiState.update { it.copy(message = newMessage("Producto agotado. No se puede agregar al pedido.", true)) }
                    return@withLock
                }

                val reserveResult = productRepository.reserveUnit(productId)
                if (reserveResult.isSuccess) {
                    val currentQty = _uiState.value.orderQuantities[productId] ?: 0
                    cartRepository.setOrderQuantity(productId, currentQty + 1)
                    _uiState.update { it.copy(message = newMessage("Se reservó 1 unidad de ${product.name}.", false)) }
                } else {
                    _uiState.update {
                        it.copy(
                            message = newMessage(
                                "No se pudo reservar la unidad: ${reserveResult.exceptionOrNull()?.message ?: "Sin existencias"}",
                                true
                            )
                        )
                    }
                }
            }
        }
    }

    fun decreaseOrderLine(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val currentQty = _uiState.value.orderQuantities[productId] ?: 0
                if (currentQty <= 0) return@withLock

                val releaseResult = productRepository.releaseUnits(productId, 1)
                if (releaseResult.isSuccess) {
                    cartRepository.setOrderQuantity(productId, currentQty - 1)
                    _uiState.update { it.copy(message = null) }
                } else {
                    _uiState.update {
                        it.copy(message = newMessage("Error al devolver unidad al catálogo.", true))
                    }
                }
            }
        }
    }

    fun removeFromOrder(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val currentQty = _uiState.value.orderQuantities[productId] ?: 0
                if (currentQty <= 0) return@withLock

                val releaseResult = productRepository.releaseUnits(productId, currentQty)
                if (releaseResult.isSuccess) {
                    cartRepository.setOrderQuantity(productId, 0)
                    _uiState.update { it.copy(message = null) }
                } else {
                    _uiState.update {
                        it.copy(message = newMessage("Error al liberar unidades reservadas.", true))
                    }
                }
            }
        }
    }

    fun onConcurrencyTest(productId: String, attempts: Int = 20) {
        viewModelScope.launch {
            val results = List(attempts) {
                async { productRepository.reserveUnit(productId) }
            }.awaitAll()

            val successes = results.count { it.isSuccess }
            val failures = attempts - successes
            _uiState.update {
                it.copy(concurrencyTestResult = "Éxitos: $successes · Rechazos: $failures")
            }
        }
    }

    fun onFullNameChange(value: String) {
        updateCheckout { copy(fullName = value, isFullNameTouched = true) }
    }

    fun onPhoneChange(value: String) {
        updateCheckout { copy(phone = value, isPhoneTouched = true) }
    }

    fun onNitChange(value: String) {
        updateCheckout { copy(nit = value, isNitTouched = true) }
    }

    fun onBusinessNameChange(value: String) {
        updateCheckout { copy(businessName = value, isBusinessNameTouched = true) }
    }

    fun onBillingTypeChange(type: BillingType) {
        updateCheckout {
            if (type == BillingType.CF) {
                copy(billingType = type, isNitTouched = false, isBusinessNameTouched = false)
            } else copy(billingType = type)
        }
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        updateCheckout { copy(paymentMethod = method) }
    }

    suspend fun confirmOrder(): Boolean = mutationMutex.withLock {
        val currentState = _uiState.value
        val checkout = currentState.checkout
        if (!checkout.isFormValid || currentState.totalOrderUnits <= 0) {
            _uiState.value = currentState.copy(
                checkout = checkout.copy(
                    isFullNameTouched = true,
                    isPhoneTouched = true,
                    isNitTouched = checkout.billingType == BillingType.NIT,
                    isBusinessNameTouched = checkout.billingType == BillingType.NIT
                ),
                message = newMessage("Revise los datos de facturación antes de confirmar.", true)
            )
            return false
        }

        val folio = "ORD-%04d".format(nextOrderNumber++)
        val billingDescription = if (checkout.billingType == BillingType.CF) {
            "Consumidor Final (CF)"
        } else {
            "NIT ${checkout.nit.trim()} · ${checkout.businessName.trim()}"
        }
        val receipt = OrderReceipt(
            folio = folio,
            clientName = checkout.fullName.trim(),
            billingDescription = billingDescription,
            paymentDescription = checkout.paymentMethod.label,
            totalCents = currentState.orderTotalCents
        )

        remoteOrderRepository.submitOrder(receipt)
        cartRepository.clearOrder()

        _uiState.value = currentState.copy(
            orderQuantities = emptyMap(),
            checkout = CheckoutUiState(),
            lastReceipt = receipt,
            message = newMessage("Pedido $folio confirmado exitosamente.", false)
        )
        return true
    }

    private fun updateCheckout(transform: CheckoutUiState.() -> CheckoutUiState) {
        _uiState.update { current ->
            current.copy(checkout = current.checkout.transform(), message = null)
        }
    }

    private fun newMessage(text: String, isError: Boolean): StoreMessage =
        StoreMessage(id = ++nextMessageId, text = text, isError = isError)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StoreApplication)
                val container = application.container
                StoreViewModel(
                    productRepository = container.productRepository,
                    cartRepository = container.cartRepository,
                    settingsRepository = container.settingsRepository,
                    remoteOrderRepository = container.orderRepository,
                    authRepository = container.authRepository
                )
            }
        }

        val producers = listOf(
            Producer(
                id = "producer-1",
                name = "Finca El Moriche",
                role = "Productor de café",
                location = "Acatenango, Guatemala",
                description = "Finca familiar especializada en café de altura y procesos sostenibles."
            ),
            Producer(
                id = "producer-2",
                name = "Cooperativa La Montaña",
                role = "Cooperativa productora",
                location = "Huehuetenango, Guatemala",
                description = "Cooperativa formada por pequeños productores de café de especialidad."
            )
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
