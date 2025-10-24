package com.example.foodcare.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodcare.R
import com.example.foodcare.data.repository.ProductRepository
import com.example.foodcare.databinding.FragmentProductsBinding
import com.example.foodcare.ui.profile.ProfileManager
import kotlinx.coroutines.launch

class ProductsFragment : Fragment(), ProfileManager.ProfileListener {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileManager: ProfileManager
    private var isProfileShowing = false

    // Получаем repository из FoodCareApplication
    private val repository: ProductRepository by lazy {
        (requireContext().applicationContext as com.example.foodcare.FoodCareApplication).productRepository
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

        // Инициализируем менеджер профиля
        profileManager = ProfileManager(requireContext(), binding.root)
        profileManager.setProfileListener(this)
        profileManager.initializeProfile()

        setupRecyclerView()
        observeProducts()
        setupProfileButton()
        setupBackButton()
        setupBackgroundDim()

    }

    private fun setupBackButton() {
        binding.imageButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupProfileButton() {
        binding.profileButton.setOnClickListener {
            showProfile()
        }
    }

    private fun setupBackgroundDim() {
        // Обработчик клика на затемнение для закрытия профиля
        binding.backgroundDim.setOnClickListener {
            hideProfile()
        }

        // Обработчик клика на основную область (main) для закрытия профиля по клику на правую часть
        binding.main.setOnClickListener {
            if (isProfileShowing) {
                hideProfile()
            }
        }
    }

    private fun showProfile() {
        profileManager.showProfile()
        binding.backgroundDim.visibility = View.VISIBLE
        binding.backgroundDim.isClickable = true
        isProfileShowing = true

        // Блокируем прокрутку RecyclerView когда профиль открыт
        binding.productsRecyclerView.isNestedScrollingEnabled = false

        // Делаем основную область кликабельной для закрытия профиля
        binding.main.isClickable = true
    }

    private fun hideProfile() {
        profileManager.hideProfile()
        binding.backgroundDim.visibility = View.GONE
        binding.backgroundDim.isClickable = false
        isProfileShowing = false

        // Разблокируем прокрутку RecyclerView
        binding.productsRecyclerView.isNestedScrollingEnabled = true

        // Возвращаем нормальное состояние основной области
        binding.main.isClickable = false
    }

    // Реализация методов ProfileListener
    override fun onUserNameUpdated(newName: String) {
        showToast("Имя обновлено: $newName")
    }

    override fun onLogoutRequested() {
        performLogout()
    }

    override fun onProfileHidden() {
        // Вызывается когда профиль скрыт из самого ProfileManager
        binding.backgroundDim.visibility = View.GONE
        binding.backgroundDim.isClickable = false
        isProfileShowing = false
        binding.productsRecyclerView.isNestedScrollingEnabled = true
        binding.main.isClickable = false
    }

    private fun performLogout() {
        lifecycleScope.launch {
            repository.deleteAllProducts()
        }
        showToast("Выход из аккаунта выполнен")
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
            viewModel.allProducts.collect { products ->  // ← используем allProducts
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
        profileManager.cleanup()
        _binding = null
    }
}