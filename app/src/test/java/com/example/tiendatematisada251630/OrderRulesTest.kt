package com.example.tiendatematisada251630

import com.example.tiendatematisada251630.model.OrderMutationResult
import com.example.tiendatematisada251630.model.Product
import com.example.tiendatematisada251630.model.addToOrder
import com.example.tiendatematisada251630.model.decreaseOrderLine
import com.example.tiendatematisada251630.model.filterProducts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderRulesTest {
    private val product = Product(
        id = "coffee-test",
        name = "Café de prueba",
        description = "Descripción",
        priceCents = 2250,
        producerId = "producer-1",
        technicalDetails = "Proceso lavado",
        stock = 3,
        imageUrl = "https://picsum.photos/seed/coffee-test/600/420"
    )

    @Test
    fun addingSameProductAccumulatesInOneLine() {
        val first = addToOrder(listOf(product), emptyMap(), product.id, 1)
            as OrderMutationResult.Success
        val second = addToOrder(listOf(product), first.orderQuantities, product.id, 1)
            as OrderMutationResult.Success

        assertEquals(mapOf(product.id to 2), second.orderQuantities)
    }

    @Test
    fun quantityAboveStockIsRejectedWithoutChangingOrder() {
        val original = mapOf(product.id to 3)
        val result = addToOrder(listOf(product), original, product.id, 1)

        assertTrue(result is OrderMutationResult.Rejected)
        assertEquals(original, mapOf(product.id to 3))
    }

    @Test
    fun nonexistentProductAndNonPositiveIncrementAreRejected() {
        assertTrue(addToOrder(listOf(product), emptyMap(), "missing", 1) is OrderMutationResult.Rejected)
        assertTrue(addToOrder(listOf(product), emptyMap(), product.id, 0) is OrderMutationResult.Rejected)
    }

    @Test
    fun decreasingOneUnitRemovesTheLine() {
        assertEquals(emptyMap<String, Int>(), decreaseOrderLine(mapOf(product.id to 1), product.id))
    }

    @Test
    fun searchIgnoresCaseAndOuterSpaces() {
        val result = filterProducts(listOf(product), "  CAFÉ DE PRUEBA  ")

        assertEquals(listOf(product), result)
    }
}
