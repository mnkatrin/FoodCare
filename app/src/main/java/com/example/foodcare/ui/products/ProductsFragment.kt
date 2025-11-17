package com.example.foodcare.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcare.ui.app_product.RecentProductsAdapter
import com.example.foodcare.R
import com.example.foodcare.data.model.Product
import com.example.foodcare.ui.main.MainActivity
import com.example.foodcare.ui.app_product.AddProductFragment
import com.google.android.material.button.MaterialButton

class ProductsFragment : Fragment() {

    private lateinit var recentProductsRecyclerView: RecyclerView
    private lateinit var recentAdapter: RecentProductsAdapter
    private lateinit var addProductButton: MaterialButton
    private lateinit var searchAutoComplete: AutoCompleteTextView
    private var recentProducts = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false)

        recentProductsRecyclerView = view.findViewById(R.id.recentProductsRecyclerView)
        addProductButton = view.findViewById(R.id.addProductButton)
        searchAutoComplete = view.findViewById(R.id.productSearchAutoComplete)

        // Initialize RecyclerView and other components
        setupRecyclerView()
        setupSearch()
        setupAddProductButton()

        loadRecentProducts()

        return view
    }

    private fun setupRecyclerView() {
        recentAdapter = RecentProductsAdapter(requireContext(), recentProducts) { product ->
            // Логика для добавления выбранного продукта
        }
        recentProductsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        recentProductsRecyclerView.adapter = recentAdapter
    }

    private fun setupSearch() {
        // TODO: Подключить адаптер для AutoCompleteTextView с API или локальными продуктами
        // Пример:
        // val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, allProducts)
        // searchAutoComplete.setAdapter(adapter)
    }

    private fun setupAddProductButton() {
        addProductButton.setOnClickListener {
            // Открытие фрагмента для добавления нового продукта
            (activity as? MainActivity)?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, AddProductFragment())
                ?.addToBackStack("addProduct")
                ?.commit()
        }
    }

    private fun loadRecentProducts() {
        // TODO: Загрузить продукты из базы данных или локально
        // Для примера добавим несколько продуктов вручную
        if (recentProducts.isEmpty()) {
            // Показать сообщение "Нет недавно добавленных продуктов"
            view?.findViewById<View>(R.id.noRecentProductsText)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<View>(R.id.noRecentProductsText)?.visibility = View.GONE
            recentAdapter.notifyDataSetChanged()
        }
    }
}
