package com.example.foodcare.ui.app_product

import android.app.Application
import androidx.lifecycle.*
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.repository.ProductRepository
import com.example.foodcare.FoodCareApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddProductViewModel(
    application: Application,
    private val productRepository: ProductRepository
) : AndroidViewModel(application) {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> = _searchResults


    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val productsList = productRepository.getAllProducts().first()
                _products.value = productsList
            } catch (e: Exception) {
                _message.value = "Ошибка загрузки продуктов: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                _searchResults.value = emptyList()
                return@launch
            }

            _isLoading.value = true
            try {
                val results = productRepository.searchProducts(query)
                _searchResults.value = results
            } catch (e: Exception) {
                _message.value = "Ошибка поиска: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }




    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.addProduct(product)
                _message.value = "Продукт '${product.name}' добавлен!"
                loadProducts()
            } catch (e: Exception) {
                _message.value = "Ошибка добавления продукта: ${e.message}"
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.updateProduct(product)
                _message.value = "Продукт '${product.name}' обновлён!"
                loadProducts()
            } catch (e: Exception) {
                _message.value = "Ошибка обновления продукта: ${e.message}"
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
                _message.value = "Продукт '${product.name}' удалён!"
                loadProducts()
            } catch (e: Exception) {
                _message.value = "Ошибка удаления продукта: ${e.message}"
            }
        }
    }

    fun updateProductQuantity(product: Product, newQuantity: String) {
        viewModelScope.launch {
            try {
                val quantityValue = newQuantity.toDoubleOrNull() ?: 0.0
                val updatedProduct = product.copy(quantity = quantityValue, isDirty = true)
                productRepository.updateProduct(updatedProduct)
            } catch (_: Exception) {}
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddProductViewModel::class.java)) {
                        val app = application as FoodCareApplication
                        return AddProductViewModel(application, app.productRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}
