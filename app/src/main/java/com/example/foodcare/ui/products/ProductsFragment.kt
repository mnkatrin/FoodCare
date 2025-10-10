package com.example.foodcare.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodcare.data.database.AppDatabase
import com.example.foodcare.data.repository.ProductRepository
import com.example.foodcare.databinding.FragmentProductsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProductsViewModel
    private lateinit var adapter: ProductsAdapter

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

        // Инициализация ViewModel
        val database = AppDatabase.getInstance(requireContext())
        val productDao = database.productDao()
        val repository = ProductRepository(productDao)
        viewModel = ProductsViewModel(repository)

        setupRecyclerView()
        observeProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductsAdapter(
            onProductClick = { product ->
                // Обработка клика
            },
            onProductLongClick = { product ->
                viewModel.deleteProduct(product)
            }
        )

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ProductsFragment.adapter
        }
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allProducts.collectLatest { products ->
                adapter.submitList(products)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}