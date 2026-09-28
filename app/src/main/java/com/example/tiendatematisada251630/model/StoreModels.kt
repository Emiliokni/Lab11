package com.example.tiendatematisada251630.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val priceCents: Int,
    val producerId: String,
    val technicalDetails: String,
    val stock: Int,
    val imageUrl: String
)

data class Producer(
    val id: String,
    val name: String,
    val role: String,
    val location: String,
    val description: String
)

data class StoreMessage(
    val id: Long,
    val text: String,
    val isError: Boolean
)

data class StoreUiState(
    val products: List<Product> = emptyList(),
    val producers: List<Producer> = emptyList(),
    val favoriteProductIds: Set<String> = emptySet(),
    val query: String = "",
    val orderQuantities: Map<String, Int> = emptyMap(),
    val message: StoreMessage? = null,
    val checkout: CheckoutUiState = CheckoutUiState(),
    val lastReceipt: OrderReceipt? = null
)

data class OrderLine(
    val product: Product,
    val quantity: Int
) {
    val subtotalCents: Int
        get() = product.priceCents * quantity
}

val StoreUiState.orderLines: List<OrderLine>
    get() = orderQuantities.mapNotNull { (productId, quantity) ->
        products.find { it.id == productId }?.let { product ->
            OrderLine(product = product, quantity = quantity)
        }
    }

val StoreUiState.totalOrderUnits: Int
    get() = orderQuantities.values.sum()

val StoreUiState.orderTotalCents: Int
    get() = orderLines.sumOf(OrderLine::subtotalCents)

fun formatQuetzales(priceCents: Int): String =
    "Q%d.%02d".format(priceCents / 100, priceCents % 100)
