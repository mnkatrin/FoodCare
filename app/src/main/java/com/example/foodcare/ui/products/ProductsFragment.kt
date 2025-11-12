package com.example.foodcare.ui.products

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Убираем фабрику
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodcare.R
import com.example.foodcare.data.repository.ProductRepository // Импортируем, если нужно вручную инжектировать
import com.example.foodcare.databinding.FragmentProductsBinding
import com.example.foodcare.ui.profile.ProfileClass
import com.example.foodcare.ui.app_product.ProductsViewModel // Убедитесь, что путь к ViewModel правильный
import dagger.hilt.android.AndroidEntryPoint // <-- Добавлен импорт
import javax.inject.Inject // <-- Добавлен импорт

// <-- Добавлена аннотация
@AndroidEntryPoint
class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    // --- ИНЖЕКТИРУЕМ ProductRepository (опционально, если нужен в Fragment) ---
    // @Inject lateinit var productRepository: ProductRepository
    // --- КОНЕЦ ИНЖЕКТИРОВАНИЯ ---

    // --- ИЗМЕНЕНО: Получение ViewModel через Hilt ---
    private val viewModel: ProductsViewModel by viewModels() // <-- Убрана фабрика
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeProducts()
        setupProfileButton()
        setupBackButton()
        // УБРАТЬ setupBackgroundDim() - больше не нужен
    }

    private fun setupBackButton() {
        binding.imageButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupProfileButton() {
        binding.profileButton.setOnClickListener {
            // Переход на ProfileClass Activity
            val intent = Intent(requireContext(), ProfileClass::class.java)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product, newQuantity ->
            // Используем метод updateProductQuantity из ViewModel
            viewModel.updateProductQuantity(product, newQuantity)
        }

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ProductsFragment.adapter
            itemAnimator = null
        }
    }

    private fun observeProducts() {
        lifecycleScope.launch {
            viewModel.products.collect { products ->
                adapter.submitList(products)

                // Показываем/скрываем сообщение о пустом списке
                if (products.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.productsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.productsRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
