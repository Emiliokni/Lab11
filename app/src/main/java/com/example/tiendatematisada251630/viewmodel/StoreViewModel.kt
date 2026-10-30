package com.example.tiendatematisada251630.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendatematisada251630.data.CatalogOrder
import com.example.tiendatematisada251630.data.CatalogPreferences
import com.example.tiendatematisada251630.data.FavoriteEntity
import com.example.tiendatematisada251630.data.OrderLineEntity
import com.example.tiendatematisada251630.data.StoreDatabase
import com.example.tiendatematisada251630.model.OrderMutationResult
import com.example.tiendatematisada251630.model.BillingType
import com.example.tiendatematisada251630.model.CheckoutUiState
import com.example.tiendatematisada251630.model.OrderReceipt
import com.example.tiendatematisada251630.model.PaymentMethod
import com.example.tiendatematisada251630.model.Producer
import com.example.tiendatematisada251630.model.Product
import com.example.tiendatematisada251630.model.StoreMessage
import com.example.tiendatematisada251630.model.StoreUiState
import com.example.tiendatematisada251630.model.addToOrder as applyAddToOrder
import com.example.tiendatematisada251630.model.orderTotalCents
import com.example.tiendatematisada251630.model.totalOrderUnits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private var nextMessageId = 0L
    private var nextOrderNumber = 1
    private val dao = StoreDatabase.get(application).storeDao()
    private val preferences = CatalogPreferences(application)
    private val mutationMutex = Mutex()

    private val _uiState = MutableStateFlow(
        StoreUiState(products = generateCatalog(), producers = producers)
    )
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(dao.observeFavorites(), dao.observeOrderLines(), preferences.order) {
                favorites, lines, order -> Triple(favorites, lines, order)
            }.collect { (favorites, lines, order) ->
                val knownIds = _uiState.value.products.mapTo(hashSetOf()) { it.id }
                _uiState.update { current ->
                    current.copy(
                        favoriteProductIds = favorites.mapTo(hashSetOf()) { it.productId }.intersect(knownIds),
                        orderQuantities = lines.filter { it.productId in knownIds && it.quantity > 0 }
                            .associate { it.productId to it.quantity },
                        catalogOrder = order,
                        isLoaded = true
                    )
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onCatalogOrderChange(order: CatalogOrder) {
        viewModelScope.launch { preferences.setOrder(order) }
    }

    fun toggleFavorite(productId: String) {
        if (!isKnownProduct(productId)) return
        viewModelScope.launch {
            mutationMutex.withLock {
                if (dao.favorite(productId) == null) {
                    dao.insertFavorite(FavoriteEntity(productId))
                } else {
                    dao.deleteFavorite(productId)
                }
            }
        }
    }

    fun addToOrder(productId: String, increment: Int = 1) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val existing = dao.orderLine(productId)
                when (val result = applyAddToOrder(
                    products = _uiState.value.products,
                    currentOrder = existing?.let { mapOf(productId to it.quantity) } ?: emptyMap(),
                    productId = productId,
                    increment = increment
                )) {
                    is OrderMutationResult.Success -> {
                        val quantity = result.orderQuantities.getValue(productId)
                        if (existing == null) dao.insertOrderLine(OrderLineEntity(productId, quantity))
                        else dao.updateOrderLine(productId, quantity)
                        _uiState.update { it.copy(message = newMessage(result.message, false)) }
                    }
                    is OrderMutationResult.Rejected ->
                        _uiState.update { it.copy(message = newMessage(result.reason, true)) }
                }
            }
        }
    }

    fun decreaseOrder(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val existing = dao.orderLine(productId) ?: return@withLock
                if (existing.quantity <= 1) dao.deleteOrderLine(productId)
                else dao.updateOrderLine(productId, existing.quantity - 1)
                _uiState.update { it.copy(message = null) }
            }
        }
    }

    fun removeFromOrder(productId: String) {
        viewModelScope.launch {
            mutationMutex.withLock {
                dao.deleteOrderLine(productId)
                _uiState.update { it.copy(message = null) }
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

    // La navegación espera a que el DELETE termine para que el pedido vacío sobreviva al cierre.
    suspend fun confirmOrder(): Boolean = mutationMutex.withLock {
        val currentState = _uiState.value
        val checkout = currentState.checkout
        val persistedOrder = dao.getOrderLines().associate { it.productId to it.quantity }
        val orderState = currentState.copy(orderQuantities = persistedOrder)
        if (!checkout.isFormValid || orderState.totalOrderUnits <= 0) {
            _uiState.value = currentState.copy(
                checkout = checkout.copy(
                    isFullNameTouched = true,
                    isPhoneTouched = true,
                    isNitTouched = checkout.billingType == BillingType.NIT,
                    isBusinessNameTouched = checkout.billingType == BillingType.NIT
                ),
                message = newMessage("No se pudo confirmar: revisa los datos y el pedido.", true)
            )
            return@withLock false
        }

        val receipt = OrderReceipt(
            folio = "#ORD-${nextOrderNumber.toString().padStart(5, '0')}",
            clientName = checkout.fullName.trim(),
            billingDescription = if (checkout.billingType == BillingType.CF) {
                "CF (Consumidor Final)"
            } else {
                "NIT ${checkout.nit.trim()} · ${checkout.businessName.trim()}"
            },
            paymentDescription = checkout.paymentMethod.label,
            totalCents = orderState.orderTotalCents
        )
        dao.clearOrder()
        nextOrderNumber++
        _uiState.update {
            it.copy(orderQuantities = emptyMap(), checkout = CheckoutUiState(),
                lastReceipt = receipt, message = null)
        }
        true
    }

    private fun isKnownProduct(productId: String): Boolean =
        _uiState.value.products.any { it.id == productId }

    private fun updateCheckout(transform: CheckoutUiState.() -> CheckoutUiState) {
        _uiState.update { current -> current.copy(checkout = current.checkout.transform()) }
    }

    private fun newMessage(text: String, isError: Boolean): StoreMessage =
        StoreMessage(id = ++nextMessageId, text = text, isError = isError)

    private companion object {
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

        fun generateCatalog(): List<Product> {
            val originals = listOf(
                Product(
                    id = "coffee-1",
                    name = "Geisha de Acatenango",
                    description = "Café floral y cítrico cultivado a gran altura.",
                    priceCents = 14500,
                    producerId = "producer-1",
                    technicalDetails = "Proceso lavado · Altitud: 1,750 m · Tueste medio",
                    stock = 4,
                    imageUrl = imageUrl("coffee-1")
                ),
                Product(
                    id = "coffee-2",
                    name = "Borbón de Huehuetenango",
                    description = "Café dulce con notas de chocolate y almendra.",
                    priceCents = 11000,
                    producerId = "producer-2",
                    technicalDetails = "Proceso natural · Altitud: 1,650 m · Tueste medio",
                    stock = 3,
                    imageUrl = imageUrl("coffee-2")
                ),
                Product(
                    id = "coffee-3",
                    name = "Caturra de Cobán",
                    description = "Café equilibrado con notas de miel y frutos rojos.",
                    priceCents = 9800,
                    producerId = "producer-1",
                    technicalDetails = "Proceso honey · Altitud: 1,450 m · Tueste medio claro",
                    stock = 0,
                    imageUrl = imageUrl("coffee-3")
                )
            )

            val random = Random(251630)
            val varieties = listOf("Borbón", "Caturra", "Catuaí", "Pacamara", "Typica", "Geisha")
            val origins = listOf("Antigua", "Atitlán", "Cobán", "Fraijanes", "Huehuetenango", "San Marcos")
            val processes = listOf("lavado", "natural", "honey")
            val roasts = listOf("claro", "medio claro", "medio", "medio oscuro")

            val generated = (4..500).map { number ->
                val id = "coffee-lab10-${number.toString().padStart(3, '0')}"
                val variety = varieties[random.nextInt(varieties.size)]
                val origin = origins[random.nextInt(origins.size)]
                val process = processes[random.nextInt(processes.size)]
                val roast = roasts[random.nextInt(roasts.size)]
                val altitude = random.nextInt(1200, 2001)
                val stock = when {
                    number % 17 == 0 -> 0
                    number % 13 == 0 -> 3
                    else -> random.nextInt(1, 13)
                }
                Product(
                    id = id,
                    name = "$variety de $origin #$number",
                    description = "Café de $origin con proceso $process y tueste $roast.",
                    priceCents = random.nextInt(4500, 18001),
                    producerId = if (random.nextBoolean()) "producer-1" else "producer-2",
                    technicalDetails = "Proceso $process · Altitud: $altitude m · Tueste $roast",
                    stock = stock,
                    imageUrl = imageUrl(id)
                )
            }
            return originals + generated
        }

        fun imageUrl(id: String): String = "https://picsum.photos/seed/$id/600/420"
    }
}
