package com.example.foodcare.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProductsViewModel(private val repository: ProductRepository) : ViewModel() {

    val allProducts: Flow<List<Product>> = repository.getAllProducts()

    init {
        viewModelScope.launch {
            repository.addSampleProducts()
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.addProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}