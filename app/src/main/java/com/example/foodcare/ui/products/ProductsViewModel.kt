package com.example.foodcare.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    // Состояние для списка продуктов
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Инициализация: Загружаем все продукты
    init {
        loadProducts()
    }

    // Загрузка всех продуктов
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Получаем все продукты из репозитория
                repository.getAllProducts().collect { productsList ->
                    _products.value = productsList
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Обновление информации о продукте
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                // Обновляем продукт в репозитории
                repository.updateProduct(product)
                // После обновления, пересчитываем список продуктов
                loadProducts()
            } catch (e: Exception) {
                // Обработка ошибок
                e.printStackTrace()
            }
        }
    }

    // Удаление продукта
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                // Удаляем продукт из репозитория
                repository.deleteProduct(product)
                // После удаления, пересчитываем список продуктов
                loadProducts()
            } catch (e: Exception) {
                // Обработка ошибок
                e.printStackTrace()
            }
        }
    }

    // Добавление нового продукта
    fun addNewProduct(name: String, category: String, expirationDate: String, quantity: Double, unit: String) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                category = category,
                expirationDate = expirationDate,
                quantity = quantity,
                unit = unit
            )
            try {
                // Добавляем новый продукт в репозиторий
                repository.addProduct(product)
                // После добавления, пересчитываем список продуктов
                loadProducts()
            } catch (e: Exception) {
                // Обработка ошибок
                e.printStackTrace()
            }
        }
    }

    // Функция для получения недавно добавленных продуктов
    fun getRecentlyAddedProducts(limit: Int) = products.value
        .filter { it.isMyProduct }
        .sortedByDescending { it.createdAt }
        .take(limit)

    // Фабрика для создания ViewModel с передачей зависимости (ProductRepository)
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
}
