// sync/FirebaseSyncManager.kt
package com.example.foodcare.data.sync

import com.example.foodcare.data.dao.ProductDao
import com.example.foodcare.data.model.Product
import com.example.foodcare.auth.UserManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncManager @Inject constructor(
    private val productDao: ProductDao,
    private val userManager: UserManager // Добавляем UserManager
) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val COLLECTION_PRODUCTS = "user_products"
    }

    private fun getCurrentUserId(): String {
        // Используем Firebase UID если пользователь авторизован, иначе локальный ID
        return auth.currentUser?.uid ?: userManager.getCurrentUserId()
    }

    private fun getUserProductsCollection(): String {
        val userId = getCurrentUserId()
        return "$COLLECTION_PRODUCTS/$userId/products"
    }

    suspend fun syncAllData() {
        try {
            val userId = getCurrentUserId()
            Timber.d("Starting sync for user: $userId")

            // 1. Отправляем локальные изменения на Firebase
            uploadLocalChanges()

            // 2. Загружаем данные из Firebase
            downloadFirebaseChanges()

            // 3. Очищаем удаленные продукты
            cleanupDeletedProducts()

            Timber.d("Sync completed successfully for user: $userId")
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
        }
    }

    private suspend fun uploadLocalChanges() {
        try {
            // Получаем несинхронизированные продукты
            val unsyncedProducts = productDao.getUnsyncedProducts()
            Timber.d("Found ${unsyncedProducts.size} unsynced products")

            for (product in unsyncedProducts) {
                try {
                    if (product.firebaseId == null) {
                        // Новый продукт - создаем в Firebase
                        val productData = product.toFirebaseMap()
                        val documentRef = firestore.collection(getUserProductsCollection()).document()
                        documentRef.set(productData).await()

                        // Обновляем локальную запись
                        productDao.updateFirebaseId(product.id, documentRef.id)
                        productDao.markAsSynced(product.id, System.currentTimeMillis())

                        Timber.d("New product uploaded: ${product.name}")
                    } else {
                        // Обновляем существующий продукт в Firebase
                        val productData = product.toFirebaseMap()
                        firestore.collection(getUserProductsCollection())
                            .document(product.firebaseId)
                            .set(productData, SetOptions.merge())
                            .await()

                        productDao.markAsSynced(product.id, System.currentTimeMillis())
                        Timber.d("Product updated: ${product.name}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync product: ${product.name}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload local changes")
        }
    }

    private suspend fun downloadFirebaseChanges() {
        try {
            val snapshot = firestore.collection(getUserProductsCollection())
                .get()
                .await()

            val firebaseProducts = snapshot.documents.mapNotNull { document ->
                try {
                    document.toProduct(document.id)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse product from Firebase")
                    null
                }
            }

            Timber.d("Downloaded ${firebaseProducts.size} products from Firebase")

            // Сохраняем/обновляем в локальной базе
            for (firebaseProduct in firebaseProducts) {
                try {
                    val existingProduct = productDao.getProductByFirebaseId(firebaseProduct.firebaseId!!)

                    if (existingProduct == null) {
                        // Новый продукт из Firebase
                        productDao.insertProduct(firebaseProduct)
                        Timber.d("New product from Firebase: ${firebaseProduct.name}")
                    } else {
                        // Сравниваем временные метки с обработкой nullable
                        val firebaseSyncTime = firebaseProduct.lastSynced ?: 0L
                        val existingSyncTime = existingProduct.lastSynced ?: 0L

                        if (firebaseSyncTime > existingSyncTime) {
                            // Продукт из Firebase новее - обновляем локально
                            val updatedProduct = firebaseProduct.copy(id = existingProduct.id)
                            productDao.updateProduct(updatedProduct)
                            Timber.d("Product updated from Firebase: ${firebaseProduct.name}")
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to process Firebase product: ${firebaseProduct.name}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to download from Firebase")
        }
    }

    private suspend fun cleanupDeletedProducts() {
        try {
            val deletedProducts = productDao.getDeletedProducts()
            Timber.d("Found ${deletedProducts.size} deleted products to cleanup")

            for (product in deletedProducts) {
                try {
                    // Удаляем из Firebase
                    product.firebaseId?.let { firebaseId ->
                        firestore.collection(getUserProductsCollection())
                            .document(firebaseId)
                            .delete()
                            .await()
                    }

                    // Удаляем локально
                    productDao.deleteProduct(product)
                    Timber.d("Deleted product cleaned up: ${product.name}")

                } catch (e: Exception) {
                    Timber.e(e, "Failed to cleanup deleted product: ${product.name}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup deleted products")
        }
    }

    suspend fun syncIfNeeded() {
        syncAllData()
    }

    suspend fun uploadSingleProduct(product: Product): String? {
        return try {
            val productData = product.toFirebaseMap()
            val documentRef = firestore.collection(getUserProductsCollection()).document()
            documentRef.set(productData).await()

            val firebaseId = documentRef.id
            productDao.updateFirebaseId(product.id, firebaseId)
            productDao.markAsSynced(product.id, System.currentTimeMillis())

            Timber.d("Single product uploaded: ${product.name}")
            firebaseId
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload single product")
            null
        }
    }

    suspend fun deleteProductFromFirebase(product: Product) {
        try {
            product.firebaseId?.let { firebaseId ->
                firestore.collection(getUserProductsCollection())
                    .document(firebaseId)
                    .delete()
                    .await()
                Timber.d("Product deleted from Firebase: ${product.name}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete product from Firebase")
        }
    }

    // НОВЫЙ МЕТОД: Синхронизация только продуктов текущего пользователя
    suspend fun syncUserProducts() {
        val userId = getCurrentUserId()
        Timber.d("Syncing products for user: $userId")
        syncAllData()
    }
}

// ОБНОВЛЕННЫЕ Extension functions с поддержкой userId
private fun Product.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "name" to name,
        "category" to category,
        "expirationDate" to expirationDate,
        "quantity" to quantity,
        "unit" to unit,
        "barcode" to barcode,
        "imageUrl" to imageUrl,
        "createdAt" to createdAt,
        "lastSynced" to System.currentTimeMillis(),
        "isDeleted" to isDeleted,
        "isDirty" to isDirty,
        "isMyProduct" to isMyProduct, // Добавляем новое поле
        "userId" to userId // Добавляем userId для разделения данных
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toProduct(firebaseId: String): Product {
    return Product(
        id = "", // Будет сгенерирован в Room
        name = getString("name") ?: "",
        category = getString("category") ?: "",
        expirationDate = getString("expirationDate") ?: "",
        quantity = getDouble("quantity") ?: 0.0,
        unit = getString("unit") ?: "",
        barcode = getString("barcode") ?: "",
        imageUrl = getString("imageUrl") ?: "",
        createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
        isDirty = getBoolean("isDirty") ?: false,
        firebaseId = firebaseId,
        lastSynced = getLong("lastSynced"),
        isDeleted = getBoolean("isDeleted") ?: false,
        isMyProduct = getBoolean("isMyProduct") ?: true, // По умолчанию true
        userId = getString("userId") ?: "" // Получаем userId из Firebase
    )
}