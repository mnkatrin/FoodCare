package com.example.foodcare.ui.app_product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodcare.data.repository.ProductRepository

class AddProductViewModelFactory(
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddProductViewModel::class.java)) {
            return AddProductViewModel(productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}