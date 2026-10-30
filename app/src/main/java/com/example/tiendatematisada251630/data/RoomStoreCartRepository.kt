package com.example.tiendatematisada251630.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomStoreCartRepository(
    private val dao: StoreDao
) : StoreCartRepository {

    override fun observeFavorites(): Flow<Set<String>> {
        return dao.observeFavorites().map { list ->
            list.mapTo(hashSetOf()) { it.productId }
        }
    }

    override fun observeOrderQuantities(): Flow<Map<String, Int>> {
        return dao.observeOrderLines().map { list ->
            list.filter { it.quantity > 0 }.associate { it.productId to it.quantity }
        }
    }

    override suspend fun toggleFavorite(productId: String): Result<Unit> {
        return runCatching {
            val existing = dao.favorite(productId)
            if (existing == null) {
                dao.insertFavorite(FavoriteEntity(productId))
            } else {
                dao.deleteFavorite(productId)
            }
        }
    }

    override suspend fun setOrderQuantity(productId: String, quantity: Int): Result<Unit> {
        return runCatching {
            val existing = dao.orderLine(productId)
            if (quantity <= 0) {
                if (existing != null) {
                    dao.deleteOrderLine(productId)
                }
            } else {
                if (existing == null) {
                    dao.insertOrderLine(OrderLineEntity(productId, quantity))
                } else {
                    dao.updateOrderLine(productId, quantity)
                }
            }
        }
    }

    override suspend fun clearOrder(): Result<Unit> {
        return runCatching {
            dao.clearOrderLines()
        }
    }
}
