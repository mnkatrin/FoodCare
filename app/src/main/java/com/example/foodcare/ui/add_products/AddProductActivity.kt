package com.example.foodcare.ui.add_products

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.foodcare.FoodCareApplication
import com.example.foodcare.R
import com.example.foodcare.data.model.Product
import com.example.foodcare.databinding.AddProductsBinding // <-- ИСПРАВЛЕНО: Правильное имя Binding
import com.example.foodcare.ui.profile.ProfileClass
import com.example.foodcare.ui.app_product.AddProductFormViewModel // <-- Убедись, что путь к ViewModel правильный
import java.text.SimpleDateFormat
import java.util.*

// Убираем аннотацию @AndroidEntryPoint, т.к. Hilt не используется
class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: AddProductsBinding // <-- ИСПРАВЛЕНО: Правильное имя Binding
    private var currentQuantity = 1

    // --- ИЗМЕНЕНО: Получение ViewModel через ViewModelProvider ---
    private lateinit var viewModel: AddProductFormViewModel // <-- ИСПРАВЛЕНО: Правильное имя ViewModel
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    // Список категорий
    private val categories = listOf(
        "Напитки", "Хлебобулочные изделия", "Мясо, птица", "Молочные продукты",
        "Фрукты", "Овощи", "Консервы", "Бакалея", "Замороженные продукты",
        "Сладости", "Соусы и приправы", "Рыба и морепродукты", "Снеки"
    )

    private val units = listOf("кг", "шт", "л")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddProductsBinding.inflate(layoutInflater) // <-- ИСПРАВЛЕНО: Правильное имя Binding
        setContentView(binding.root)
        makeFullScreen()

        // --- ИЗМЕНЕНО: Создаём ViewModel вручную через ViewModelProvider ---
        val application = application as FoodCareApplication
        viewModel = ViewModelProvider( // <-- Используем ViewModelProvider
            this,
            AddProductFormViewModel.provideFactory(application.productRepository) // <-- Используем фабрику из ViewModel
        )[AddProductFormViewModel::class.java] // <-- Используем правильное имя ViewModel
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---

        setupClickListeners()
        setupCategorySelection()
        setupQuantitySelector()
        setupUnitSelection()
        setupDatePicker()
    }

    private fun setupUnitSelection() {
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, units)
        binding.unitEditText.setAdapter(unitAdapter) // <-- ИСПРАВЛЕНО: Правильный Binding
        binding.unitEditText.threshold = 1 // <-- ИСПРАВЛЕНО: Правильный Binding

        binding.unitEditText.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            binding.unitEditText.showDropDown() // <-- ИСПРАВЛЕНО: Правильный Binding
        }

        binding.unitEditText.setOnFocusChangeListener { _, hasFocus -> // <-- ИСПРАВЛЕНО: Правильный Binding
            if (hasFocus) {
                binding.unitEditText.showDropDown() // <-- ИСПРАВЛЕНО: Правильный Binding
            }
        }
    }

    private fun setupCategorySelection() {
        binding.categoryEditText.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            showCategorySelectionDialog()
        }
        binding.categoryEditText.isFocusable = false // <-- ИСПРАВЛЕНО: Правильный Binding
    }

    private fun setupDatePicker() {
        binding.expiryDateEditText.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            showDatePicker()
        }
        binding.expiryDateEditText.isFocusable = false // <-- ИСПРАВЛЕНО: Правильный Binding
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = android.app.DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.expiryDateEditText.setText(dateFormat.format(selectedDate.time)) // <-- ИСПРАВЛЕНО: Правильный Binding
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
                binding.categoryEditText.setText(selectedCategory) // <-- ИСПРАВЛЕНО: Правильный Binding
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

    private fun setupQuantitySelector() {
        binding.decreaseQuantityButton.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            if (currentQuantity > 1) {
                currentQuantity--
                updateQuantityDisplay()
            }
        }

        binding.increaseQuantityButton.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            if (currentQuantity < 999) {
                currentQuantity++
                updateQuantityDisplay()
            }
        }
        updateQuantityDisplay()
    }

    private fun updateQuantityDisplay() {
        binding.quantityTextView.text = currentQuantity.toString() // <-- ИСПРАВЛЕНО: Правильный Binding
        binding.decreaseQuantityButton.isEnabled = currentQuantity > 1 // <-- ИСПРАВЛЕНО: Правильный Binding
        binding.decreaseQuantityButton.alpha = if (currentQuantity == 1) 0.5f else 1.0f // <-- ИСПРАВЛЕНО: Правильный Binding
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            saveProduct()
        }

        binding.backButton.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            finish()
        }

        // Кнопка профиля - переход на ProfileClass
        binding.profileButton.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            val intent = Intent(this, ProfileClass::class.java)
            startActivity(intent)
        }

        binding.addBarcodeButton.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            showToast("Сканирование штрих-кода")
        }

        binding.addPhotoButtonMain.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            showToast("Открытие камеры")
        }

        binding.addPhotoButton.setOnClickListener { // <-- ИСПРАВЛЕНО: Правильный Binding
            showToast("Открытие камеры")
        }

        binding.Button4.setOnClickListener { finish() } // <-- ИСПРАВЛЕНО: Правильный Binding
        binding.Button3.setOnClickListener { finish() } // <-- ИСПРАВЛЕНО: Правильный Binding
        binding.Button5.setOnClickListener { finish() } // <-- ИСПРАВЛЕНО: Правильный Binding
    }

    private fun saveProduct() {
        val productName = binding.productNameEditText.text.toString().trim() // <-- ИСПРАВЛЕНО: Правильный Binding
        val category = binding.categoryEditText.text.toString().trim() // <-- ИСПРАВЛЕНО: Правильный Binding
        val expiryDate = binding.expiryDateEditText.text.toString().trim() // <-- ИСПРАВЛЕНО: Правильный Binding
        val quantity = currentQuantity.toDouble()
        val unit = binding.unitEditText.text.toString().trim() // <-- ИСПРАВЛЕНО: Правильный Binding

        if (productName.isEmpty()) {
            showToast("Введите название продукта")
            binding.productNameEditText.requestFocus() // <-- ИСПРАВЛЕНО: Правильный Binding
            return
        }

        if (category.isEmpty()) {
            showToast("Выберите категорию")
            binding.categoryEditText.requestFocus() // <-- ИСПРАВЛЕНО: Правильный Binding
            return
        }

        if (expiryDate.isEmpty()) {
            showToast("Выберите дату окончания срока годности")
            binding.expiryDateEditText.requestFocus() // <-- ИСПРАВЛЕНО: Правильный Binding
            return
        }

        if (unit.isEmpty() || !units.contains(unit)) {
            showToast("Выберите единицу измерения из списка: кг, шт, л")
            binding.unitEditText.requestFocus() // <-- ИСПРАВЛЕНО: Правильный Binding
            return
        }

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


        viewModel.addProduct(product)

        showToast("$productName добавлен в Мои продукты!")
        finish()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

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