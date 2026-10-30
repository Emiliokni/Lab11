package com.example.tiendatematisada251630.data

import com.example.tiendatematisada251630.model.OrderReceipt

interface RemoteOrderRepository {
    suspend fun submitOrder(receipt: OrderReceipt): Result<OrderReceipt>
    suspend fun getOrders(): Result<List<OrderReceipt>>
}
