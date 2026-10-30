package com.example.tiendatematisada251630.data

import com.example.tiendatematisada251630.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProductRepository {

    private val productsCollection = firestore.collection("products")

    override fun observeProducts(): Flow<List<Product>> {
        return productsCollection.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                val description = doc.getString("description") ?: ""
                val priceCents = (doc.getLong("priceCents") ?: 0L).toInt()
                val producerId = doc.getString("producerId") ?: "producer-1"
                val technicalDetails = doc.getString("technicalDetails") ?: ""
                val stock = (doc.getLong("stock") ?: 0L).toInt()
                val imageUrl = doc.getString("imageUrl") ?: "https://picsum.photos/seed/$id/600/420"

                Product(
                    id = id,
                    name = name,
                    description = description,
                    priceCents = priceCents,
                    producerId = producerId,
                    technicalDetails = technicalDetails,
                    stock = stock,
                    imageUrl = imageUrl
                )
            }
        }
    }

    override suspend fun getProduct(id: String): Product? {
        val doc = productsCollection.document(id).get().await()
        if (!doc.exists()) return null
        val name = doc.getString("name") ?: return null
        val description = doc.getString("description") ?: ""
        val priceCents = (doc.getLong("priceCents") ?: 0L).toInt()
        val producerId = doc.getString("producerId") ?: "producer-1"
        val technicalDetails = doc.getString("technicalDetails") ?: ""
        val stock = (doc.getLong("stock") ?: 0L).toInt()
        val imageUrl = doc.getString("imageUrl") ?: "https://picsum.photos/seed/$id/600/420"

        return Product(
            id = id,
            name = name,
            description = description,
            priceCents = priceCents,
            producerId = producerId,
            technicalDetails = technicalDetails,
            stock = stock,
            imageUrl = imageUrl
        )
    }

    override suspend fun reserveUnit(productId: String): Result<Unit> {
        return runCatching {
            val docRef = productsCollection.document(productId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentStock = (snapshot.getLong("stock") ?: 0L).toInt()
                if (currentStock <= 0) {
                    throw IllegalStateException("Producto agotado en el servidor.")
                }
                transaction.update(docRef, "stock", currentStock - 1)
            }.await()
        }
    }

    override suspend fun releaseUnits(productId: String, quantity: Int): Result<Unit> {
        if (quantity <= 0) return Result.success(Unit)
        return runCatching {
            val docRef = productsCollection.document(productId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentStock = (snapshot.getLong("stock") ?: 0L).toInt()
                transaction.update(docRef, "stock", currentStock + quantity)
            }.await()
        }
    }
}
