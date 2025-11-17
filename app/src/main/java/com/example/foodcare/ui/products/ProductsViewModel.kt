package com.example.foodcare.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllProducts().collect { productsList ->
                    _products.value = productsList
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- УБРАНО: Вызов addSampleProducts ---
    // fun addSampleProducts() {
    //     viewModelScope.launch {
    //         repository.addSampleProducts()
    //     }
    // }
    // --- КОНЕЦ УБРАНО ---

    fun updateProductQuantity(product: Product, newQuantity: String) {
        viewModelScope.launch {
            try {
                // Парсим строку в Double
                val quantityValue = newQuantity.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0

                val updatedProduct = product.copy(
                    quantity = quantityValue,
                    isDirty = true
                )
                repository.updateProduct(updatedProduct)
            } catch (e: Exception) {
                // Обработка ошибки парсинга
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun addNewProduct(name: String, category: String, expirationDate: String, quantity: Double, unit: String) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                category = category,
                expirationDate = expirationDate,
                quantity = quantity,
                unit = unit
            )
            repository.addProduct(product)
        }
    }

    // --- ДОБАВЛЕНО: Метод для получения недавно добавленных продуктов ---
    fun getRecentlyAddedProducts(limit: Int): Flow<List<Product>> {
        return products.map { allProducts ->
            allProducts
                .filter { product -> product.isMyProduct }
                .sortedByDescending { product -> product.createdAt }
                .take(limit)
        }
    }
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    // --- ДОБАВЛЕНО: Метод для обновления продукта ---
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    // --- ДОБАВЛЕНО: Внутренний Factory ---
    companion object {
        fun provideFactory(
            productRepository: ProductRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
                    return ProductsViewModel(productRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---
}