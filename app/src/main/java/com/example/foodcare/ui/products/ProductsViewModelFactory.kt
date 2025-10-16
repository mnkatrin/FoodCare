package com.example.foodcare.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodcare.data.repository.ProductRepository

class ProductsViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
            return ProductsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}