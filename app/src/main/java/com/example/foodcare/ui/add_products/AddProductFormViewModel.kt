package com.example.foodcare.ui.add_products

import androidx.lifecycle.ViewModel
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

    // Получение списка категорий (можно расширить для работы с БД)
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
}