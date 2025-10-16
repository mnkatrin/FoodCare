package com.example.foodcare.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodcare.data.database.AppDatabase
import com.example.foodcare.data.repository.ProductRepository
import com.example.foodcare.databinding.FragmentProductsBinding
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    // Создаем repository напрямую
    private val repository by lazy {
        val productDao = AppDatabase.getInstance(requireContext()).productDao() // ИСПРАВЛЕНО: getInstance вместо getDatabase
        ProductRepository(productDao)
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
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product, newQuantity ->
            val updatedProduct = product.copy(quantity = newQuantity)
            viewModel.updateProduct(updatedProduct)
        }

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ProductsFragment.adapter
            itemAnimator = null
        }
    }

    private fun observeProducts() {
        lifecycleScope.launch {
            viewModel.allProducts.collect { products ->
                adapter.submitList(products)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}