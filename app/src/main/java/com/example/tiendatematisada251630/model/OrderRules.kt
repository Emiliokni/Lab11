package com.example.tiendatematisada251630.model

import com.example.tiendatematisada251630.data.CatalogOrder

sealed interface OrderMutationResult {
    data class Success(
        val orderQuantities: Map<String, Int>,
        val message: String
    ) : OrderMutationResult

    data class Rejected(val reason: String) : OrderMutationResult
}

fun addToOrder(
    products: List<Product>,
    currentOrder: Map<String, Int>,
    productId: String,
    increment: Int
): OrderMutationResult {
    if (increment <= 0) {
        return OrderMutationResult.Rejected("La cantidad debe ser mayor que cero.")
    }

    val product = products.find { it.id == productId }
        ?: return OrderMutationResult.Rejected("El producto solicitado no existe.")
    val currentQuantity = currentOrder[productId] ?: 0
    val requestedQuantity = currentQuantity + increment

    if (requestedQuantity > product.stock) {
        return OrderMutationResult.Rejected(
            "Solo hay ${product.stock} unidades disponibles. El pedido no cambió."
        )
    }

    return OrderMutationResult.Success(
        orderQuantities = currentOrder + (productId to requestedQuantity),
        message = "Se agregó $increment ${if (increment == 1) "unidad" else "unidades"} al pedido."
    )
}

fun decreaseOrderLine(
    currentOrder: Map<String, Int>,
    productId: String
): Map<String, Int> {
    val currentQuantity = currentOrder[productId] ?: return currentOrder
    return if (currentQuantity <= 1) {
        currentOrder - productId
    } else {
        currentOrder + (productId to currentQuantity - 1)
    }
}

fun removeOrderLine(
    currentOrder: Map<String, Int>,
    productId: String
): Map<String, Int> = currentOrder - productId

fun filterProducts(products: List<Product>, query: String): List<Product> {
    val normalizedQuery = query.trim()
    return if (normalizedQuery.isEmpty()) {
        products
    } else {
        products.filter { product ->
            product.name.contains(normalizedQuery, ignoreCase = true)
        }
    }
}

fun orderCatalog(products: List<Product>, order: CatalogOrder): List<Product> =
    when (order) {
        CatalogOrder.ORIGINAL -> products
        CatalogOrder.PRICE -> products.sortedWith(compareBy(Product::priceCents, Product::id))
    }
