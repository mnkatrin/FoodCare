package com.example.foodcare.ui.app_product

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.foodcare.databinding.FragmentAddProductBinding
import com.example.foodcare.ui.add_products.AddProductActivity
import com.example.foodcare.ui.profile.ProfileClass // ИЗМЕНИ ЭТОТ ИМПОРТ

class AddProductFragment : Fragment() { // УБРАТЬ implements ProfileFragment.ProfileListener

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Кнопка назад
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Кнопка "Добавить продукт" - переход на AddProductActivity
        binding.addProductButton.setOnClickListener {
            navigateToAddProductActivity()
        }

        // Кнопки добавления конкретных продуктов
        binding.addProduct1.setOnClickListener {
            addProductToList("Апельсиновый сок", "Напитки")
        }

        binding.addProduct2.setOnClickListener {
            addProductToList("Хлеб Коломенский", "Хлебобулочные изделия")
        }

        binding.addProduct3.setOnClickListener {
            addProductToList("Яйца куриные", "Мясо, птица")
        }

        // Кнопка профиля в нижней панели - переход на ProfileClass
        binding.profileButton.setOnClickListener {
            val intent = Intent(requireContext(), ProfileClass::class.java)
            startActivity(intent)
        }

        // Центральная кнопка добавления по фото
        binding.addPhotoButton.setOnClickListener {
            openCameraForProduct()
        }

        // Обработчики для других кнопок
        binding.Button3.setOnClickListener {
            showToast("История продуктов")
        }

        binding.Button4.setOnClickListener {
            showToast("Категории продуктов")
        }

        binding.Button5.setOnClickListener {
            showToast("Избранные продукты")
        }
    }

    private fun navigateToAddProductActivity() {
        val intent = Intent(requireContext(), AddProductActivity::class.java)
        startActivity(intent)
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun addProductToList(productName: String, category: String) {
        showToast("$productName добавлен в ваш список!")
    }

    private fun openCameraForProduct() {
        showToast("Открытие камеры для сканирования продукта")
    }

    // УДАЛИТЬ эти методы - они больше не нужны:
    /*
    private fun showProfileFragment() {
        // УДАЛИТЬ
    }

    private fun hideProfileFragment() {
        // УДАЛИТЬ
    }
    */

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    // УДАЛИТЬ эти методы - они больше не нужны:
    /*
    // Реализация методов ProfileListener
    override fun onLogoutRequested() {
        performLogout()
    }

    override fun onProfileHidden() {
        hideProfileFragment()
    }

    private fun performLogout() {
        // Логика выхода из аккаунта
        showToast("Выход из аккаунта")
    }
    */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}