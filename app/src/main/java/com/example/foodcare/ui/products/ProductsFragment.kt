package com.example.foodcare.ui.products

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodcare.FoodCareApplication
import com.example.foodcare.R
import com.example.foodcare.data.repository.ProductRepository
import com.example.foodcare.databinding.FragmentProductsBinding
import com.example.foodcare.ui.profile.ProfileClass // ДОБАВЬ ЭТОТ ИМПОРТ
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() { // УБРАТЬ implements ProfileManager.ProfileListener

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    // Получаем repository из FoodCareApplication
    private val repository: ProductRepository by lazy {
        (requireContext().applicationContext as FoodCareApplication).productRepository
    }

    private val viewModel: ProductsViewModel by viewModels {
        ProductsViewModelFactory(repository)
    }

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