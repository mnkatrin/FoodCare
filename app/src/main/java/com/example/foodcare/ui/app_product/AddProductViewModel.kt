package com.example.foodcare.ui.app_product // УБЕДИСЬ, ЧТО ПАКЕТ ПРАВИЛЬНЫЙ

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel // Добавь импорт
import kotlinx.coroutines.launch
import javax.inject.Inject // Добавь импорт

// Добавь аннотации
@HiltViewModel
class AddProductViewModel @Inject constructor( // Добавь @Inject к конструктору
    private val productRepository: ProductRepository // Будет внедрён через Hilt
) : ViewModel() {

    fun addProduct(product: Product) {
        viewModelScope.launch {
            productRepository.addProduct(product)
        }
    }
}