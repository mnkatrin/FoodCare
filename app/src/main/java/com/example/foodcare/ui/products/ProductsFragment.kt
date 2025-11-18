package com.example.foodcare.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcare.R
import com.example.foodcare.data.model.Product
import com.example.foodcare.ui.products.ProductsAdapter

class ProductsFragment : Fragment() {

    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var button4: Button
    private lateinit var button3: Button
    private lateinit var button5: Button
    private lateinit var profileButton: Button

    private val products = mutableListOf<Product>() // Пример данных

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false)

        // Инициализация элементов
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyState)
        button4 = view.findViewById(R.id.Button4)
        button3 = view.findViewById(R.id.Button3)
        button5 = view.findViewById(R.id.Button5)
        profileButton = view.findViewById(R.id.profileButton)

        // Настроим RecyclerView
        setupRecyclerView()

        // Обработка кнопок
        setupButtons()

        return view
    }

    private fun setupRecyclerView() {
        // Инициализация адаптера
        val adapter = ProductsAdapter(products)
        productsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productsRecyclerView.adapter = adapter

        // Заполнение тестовыми данными
        loadSampleData()

        // Проверка, пуст ли список
        if (products.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
        } else {
            emptyStateText.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadSampleData() {
        // Добавление нескольких тестовых продуктов
        products.add(Product("Продукт 1", "Категория 1", "2025-12-12", 1.0, "шт"))
        products.add(Product("Продукт 2", "Категория 2", "2025-11-25", 2.0, "кг"))
    }

    private fun setupButtons() {
        button4.setOnClickListener {
            // Логика для Button4
        }

        button3.setOnClickListener {
            // Логика для Button3
        }

        button5.setOnClickListener {
            // Логика для Button5
        }

        profileButton.setOnClickListener {
            // Логика для профиля
        }
    }
}
