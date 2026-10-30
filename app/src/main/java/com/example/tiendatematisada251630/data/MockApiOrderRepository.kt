package com.example.tiendatematisada251630.data

import com.example.tiendatematisada251630.model.OrderReceipt

class MockApiOrderRepository(
    private val endpointUrl: String = "https://mockapi.io/api/v1/orders"
) : RemoteOrderRepository {

    private val inMemoryOrders = mutableListOf<OrderReceipt>()

    override suspend fun submitOrder(receipt: OrderReceipt): Result<OrderReceipt> {
        return runCatching {
            inMemoryOrders.add(receipt)
            receipt
        }
    }

    override suspend fun getOrders(): Result<List<OrderReceipt>> {
        return runCatching {
            inMemoryOrders.toList()
        }
    }
}
