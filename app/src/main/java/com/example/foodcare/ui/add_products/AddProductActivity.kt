package com.example.foodcare.ui.add_products

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.R
import com.example.foodcare.data.model.Product
import com.example.foodcare.databinding.AddProductsBinding
import com.example.foodcare.ui.profile.ProfileClass
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: AddProductsBinding
    private var currentQuantity = 1

    // --- ИЗМЕНЕНО: Получение ViewModel через Hilt ---
    private val viewModel: com.example.foodcare.ui.app_product.AddProductViewModel by viewModels()
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    // Список категорий
    private val categories = listOf(
        "Напитки", "Хлебобулочные изделия", "Мясо, птица", "Молочные продукты",
        "Фрукты", "Овощи", "Консервы", "Бакалея", "Замороженные продукты",
        "Сладости", "Соусы и приправы", "Рыба и морепродукты"
    )

    private val units = listOf("кг", "шт", "л")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        makeFullScreen()

        setupClickListeners()
        setupCategorySelection()
        setupQuantitySelector()
        setupUnitSelection()
        setupDatePicker()
    }

    private fun setupUnitSelection() {
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, units)
        binding.unitEditText.setAdapter(unitAdapter)
        binding.unitEditText.threshold = 1

        binding.unitEditText.setOnClickListener {
            binding.unitEditText.showDropDown()
        }

        binding.unitEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.unitEditText.showDropDown()
            }
        }
    }

    private fun setupCategorySelection() {
        binding.categoryEditText.setOnClickListener {
            showCategorySelectionDialog()
        }
        binding.categoryEditText.isFocusable = false
    }

    private fun setupDatePicker() {
        binding.expiryDateEditText.setOnClickListener {
            showDatePicker()
        }
        binding.expiryDateEditText.isFocusable = false
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = android.app.DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.expiryDateEditText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun showCategorySelectionDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Выберите категорию")
            .setItems(categories.toTypedArray()) { _, which ->
                val selectedCategory = categories[which]
                binding.categoryEditText.setText(selectedCategory)
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

    private fun setupQuantitySelector() {
        binding.decreaseQuantityButton.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                updateQuantityDisplay()
            }
        }

        binding.increaseQuantityButton.setOnClickListener {
            if (currentQuantity < 999) {
                currentQuantity++
                updateQuantityDisplay()
            }
        }
        updateQuantityDisplay()
    }

    private fun updateQuantityDisplay() {
        binding.quantityTextView.text = currentQuantity.toString()
        binding.decreaseQuantityButton.isEnabled = currentQuantity > 1
        binding.decreaseQuantityButton.alpha = if (currentQuantity == 1) 0.5f else 1.0f
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            saveProduct()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        // Кнопка профиля - переход на ProfileClass
        binding.profileButton.setOnClickListener {
            val intent = Intent(this, ProfileClass::class.java)
            startActivity(intent)
        }

        binding.addBarcodeButton.setOnClickListener {
            showToast("Сканирование штрих-кода")
        }

        binding.addPhotoButtonMain.setOnClickListener {
            showToast("Открытие камеры")
        }

        binding.addPhotoButton.setOnClickListener {
            showToast("Открытие камеры")
        }

        binding.Button4.setOnClickListener { finish() }
        binding.Button3.setOnClickListener { finish() }
        binding.Button5.setOnClickListener { finish() }
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

    private fun saveProduct() {
        val productName = binding.productNameEditText.text.toString().trim()
        val category = binding.categoryEditText.text.toString().trim()
        val expiryDate = binding.expiryDateEditText.text.toString().trim()
        val quantity = currentQuantity.toDouble()
        val unit = binding.unitEditText.text.toString().trim()

        if (productName.isEmpty()) {
            showToast("Введите название продукта")
            return
        }

        if (category.isEmpty()) {
            showToast("Выберите категорию")
            return
        }

        if (expiryDate.isEmpty()) {
            showToast("Выберите дату окончания срока годности")
            return
        }

        if (unit.isEmpty() || !units.contains(unit)) {
            showToast("Выберите единицу измерения из списка: кг, шт, л")
            return
        }

        // --- ИСПРАВЛЕНО: Удалена строка получения userId из FoodCareApplication ---
        // val userId = (application as FoodCareApplication).userManager.getCurrentUserId()
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        // Создаём Product без userId. ViewModel сама его добавит.
        val product = Product(
            name = productName,
            category = category,
            expirationDate = expiryDate,
            quantity = quantity,
            unit = unit,
            isMyProduct = true,
            userId = "" // или оставляем пустым, если ViewModel сама заполнит
        )

        // --- ИЗМЕНЕНО: Вызов метода ViewModel ---
        viewModel.addProduct(product) // <-- ViewModel сама получит userId через Hilt
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        showToast("$productName добавлен в Мои продукты!")
        finish()
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
        finish()
    }
    */

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    // Остальные методы для полноэкранного режима
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) makeFullScreen()
    }

    private fun makeFullScreen() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(
                    android.view.WindowInsets.Type.statusBars() or
                            android.view.WindowInsets.Type.navigationBars()
                )
                controller.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
        supportActionBar?.hide()
    }
}