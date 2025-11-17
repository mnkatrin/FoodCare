package com.example.foodcare.ui.app_product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider // <-- ДОБАВЛЕН ИМПОРТ
import androidx.lifecycle.viewModelScope
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.repository.ProductRepository
import kotlinx.coroutines.launch

class AddProductFormViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    fun addProduct(product: Product) {
        viewModelScope.launch {
            productRepository.addProduct(product)
        }
    }

    fun getCategories(): List<String> {
        return listOf(
            "Напитки",
            "Хлебобулочные изделия",
            "Мясо, птица",
            "Молочные продукты",
            "Фрукты",
            "Овощи",
            "Консервы",
            "Бакалея",
            "Замороженные продукты",
            "Сладости",
            "Соусы и приправы",
            "Рыба и морепродукты"
        )
    }

    companion object {
        fun provideFactory(
            productRepository: ProductRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AddProductFormViewModel::class.java)) {
                    return AddProductFormViewModel(productRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}