package com.sizllle.warehouseinventory.data

import android.net.wifi.aware.Characteristics
import com.sizllle.warehouseinventory.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.tasks.await

enum class SearchField(val label: String) {
    ALL("Все поля"),
    BOX_CODE("Код коробки"),
    DETAIL_CODE("Код детали"),
    MANUFACTURER("Производитель"),
    CAR_BRAND("Марка машины")
}

class WarehouseRepository(
    firestore: FirebaseFirestore? = null
) {
    private val firestore: FirebaseFirestore? = firestore ?: try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }

    private val productsCollection
        get() = firestore?.collection("products")

    private fun buildKeywords(
        boxCode: String,
        detailCode: String,
        manufacturer: String,
        carBrand: String
    ): List<String> {
        return listOf(boxCode, detailCode, manufacturer, carBrand)
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() && it != "не указано" }
            .distinct()
    }

    suspend fun addProduct(
        boxCode: String,
        detailCode: String,
        manufacturer: String,
        carBrand: String,
        characteristics: String,
        rack: Int,
        shelf: Int
    ): Result<Unit> {
        return try {
            val cleanBoxCode = boxCode.trim()
            if (cleanBoxCode.isEmpty()) {
                return Result.failure(IllegalArgumentException("Код коробки обязателен"))
            }

            val cleanCarBrand = if (carBrand == "Не указано") "" else carBrand.trim()
            val collection = productsCollection ?: return Result.failure(IllegalStateException("Firestore не доступен"))
            val docRef = collection.document()

            val data = hashMapOf(
                "boxCode" to cleanBoxCode,
                "boxCodeLower" to cleanBoxCode.lowercase(),
                "detailCode" to detailCode.trim(),
                "detailCodeLower" to detailCode.trim().lowercase(),
                "manufacturer" to manufacturer.trim(),
                "manufacturerLower" to manufacturer.trim().lowercase(),
                "carBrand" to cleanCarBrand,
                "carBrandLower" to cleanCarBrand.lowercase(),
                "characteristics" to characteristics.trim(),
                "rack" to rack,
                "shelf" to shelf,
                "status" to Product.STATUS_IN_STOCK,
                "searchKeywords" to buildKeywords(cleanBoxCode, detailCode, manufacturer, cleanCarBrand)
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInStockProducts(
        query: String = "",
        searchField: SearchField = SearchField.ALL
    ): Result<List<Product>> {
        return try {
            val collection = productsCollection ?: return Result.success(emptyList())
            val snapshot = collection
                .whereEqualTo("status", Product.STATUS_IN_STOCK)
                .get()
                .await()

            val allInStock = snapshot.documents.map { doc ->
                Product(
                    id = doc.id,
                    boxCode = doc.getString("boxCode").orEmpty(),
                    detailCode = doc.getString("detailCode").orEmpty(),
                    manufacturer = doc.getString("manufacturer").orEmpty(),
                    carBrand = doc.getString("carBrand").orEmpty(),
                    characteristics = doc.getString("characteristics").orEmpty(),
                    rack = (doc.getLong("rack") ?: 1L).toInt(),
                    shelf = (doc.getLong("shelf") ?: 1L).toInt(),
                    status = doc.getString("status") ?: Product.STATUS_IN_STOCK
                )
            }

            val q = query.trim().lowercase()
            if (q.isEmpty()) {
                return Result.success(allInStock)
            }

            val filtered = allInStock.filter { item ->
                when (searchField) {
                    SearchField.ALL -> listOf(
                        item.boxCode,
                        item.detailCode,
                        item.manufacturer,
                        item.carBrand
                    ).any { it.lowercase().contains(q) }

                    SearchField.BOX_CODE -> item.boxCode.lowercase().contains(q)
                    SearchField.DETAIL_CODE -> item.detailCode.lowercase().contains(q)
                    SearchField.MANUFACTURER -> item.manufacturer.lowercase().contains(q)
                    SearchField.CAR_BRAND -> item.carBrand.lowercase().contains(q)
                }
            }

            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsUsed(productId: String): Result<Unit> {
        return try {
            val collection = productsCollection ?: return Result.failure(IllegalStateException("Firestore не доступен"))
            collection.document(productId)
                .update("status", Product.STATUS_TO_ORDER)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getToOrderProducts(): Result<List<Product>> {
        return try {
            val collection = productsCollection ?: return Result.success(emptyList())
            val snapshot = collection
                .whereEqualTo("status", Product.STATUS_TO_ORDER)
                .get()
                .await()

            val items = snapshot.documents.map { doc ->
                Product(
                    id = doc.id,
                    boxCode = doc.getString("boxCode").orEmpty(),
                    detailCode = doc.getString("detailCode").orEmpty(),
                    manufacturer = doc.getString("manufacturer").orEmpty(),
                    carBrand = doc.getString("carBrand").orEmpty(),
                    characteristics = doc.getString("characteristics").orEmpty(),
                    rack = (doc.getLong("rack") ?: 1L).toInt(),
                    shelf = (doc.getLong("shelf") ?: 1L).toInt(),
                    status = doc.getString("status") ?: Product.STATUS_TO_ORDER
                )
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsOrderedByBoxCode(boxCode: String): Result<Unit> {
        return try {
            val collection = productsCollection ?: return Result.failure(IllegalStateException("Firestore не доступен"))
            val db = firestore ?: return Result.failure(IllegalStateException("Firestore не доступен"))

            val snapshot = collection
                .whereEqualTo("status", Product.STATUS_TO_ORDER)
                .whereEqualTo("boxCode", boxCode)
                .get()
                .await()

            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}