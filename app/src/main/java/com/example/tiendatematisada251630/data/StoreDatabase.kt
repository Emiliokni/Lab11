package com.example.tiendatematisada251630.data

import android.content.Context
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import kotlinx.coroutines.flow.Flow

// Los IDs son los mismos del catálogo generado con la semilla fija.
@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val productId: String)

@Entity(tableName = "order_lines")
data class OrderLineEntity(
    @PrimaryKey val productId: String,
    val quantity: Int
)

@Dao
interface StoreDao {
    @Query("SELECT * FROM favorites ORDER BY productId")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM order_lines ORDER BY productId")
    fun observeOrderLines(): Flow<List<OrderLineEntity>>

    @Query("SELECT * FROM order_lines ORDER BY productId")
    suspend fun getOrderLines(): List<OrderLineEntity>

    @Query("SELECT * FROM favorites WHERE productId = :productId LIMIT 1")
    suspend fun favorite(productId: String): FavoriteEntity?

    @Query("SELECT * FROM order_lines WHERE productId = :productId LIMIT 1")
    suspend fun orderLine(productId: String): OrderLineEntity?

    @Insert
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert
    suspend fun insertOrderLine(line: OrderLineEntity)

    @Query("UPDATE order_lines SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateOrderLine(productId: String, quantity: Int)

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun deleteFavorite(productId: String)

    @Query("DELETE FROM order_lines WHERE productId = :productId")
    suspend fun deleteOrderLine(productId: String)

    @Query("DELETE FROM order_lines")
    suspend fun clearOrder()
}

@Database(entities = [FavoriteEntity::class, OrderLineEntity::class], version = 1, exportSchema = true)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile private var instance: StoreDatabase? = null

        fun get(context: Context): StoreDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder<StoreDatabase>(
                context.applicationContext, "cafe-de-origen.db"
            ).setDriver(AndroidSQLiteDriver()).build().also { instance = it }
        }
    }
}
