// ui/app_product/ProductsViewModel.kt
package com.example.foodcare.ui.products // Убедись, что пакет правильный

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // <-- Добавлен импорт
import androidx.lifecycle.viewModelScope
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Убираем аннотации Hilt
// import dagger.hilt.android.lifecycle.HiltViewModel
// import javax.inject.Inject

// Убираем аннотации Hilt
// @HiltViewModel
class ProductsViewModel( // Убираем @Inject
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

    fun addSampleProducts() {
        viewModelScope.launch {
            repository.addSampleProducts()
        }
    }

    fun updateProductQuantity(product: Product, newQuantity: String) {
        viewModelScope.launch {
            try {
                // Парсим строку в Double
                val quantityValue = newQuantity.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0

                val updatedProduct = product.copy(
                    quantity = quantityValue, // Передаем Double, а не String
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
                quantity = quantity, // Уже Double
                unit = unit
            )
            repository.addProduct(product)
        }
    }

    // --- ДОБАВИТЬ: Внутренний Factory ---
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