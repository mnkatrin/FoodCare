package com.example.foodcare.ui.app_product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.repository.ProductRepository
import kotlinx.coroutines.launch

class AddProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    fun addProduct(product: Product) {
        viewModelScope.launch {
            productRepository.addProduct(product)
        }
    }
}