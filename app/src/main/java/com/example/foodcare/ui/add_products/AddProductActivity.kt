package com.example.foodcare.ui.add_products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.foodcare.R
import com.example.foodcare.databinding.AddProductsBinding
import com.example.foodcare.ui.profile.ProfileManager
import com.google.android.material.bottomsheet.BottomSheetDialog

class AddProductActivity : AppCompatActivity(), ProfileManager.ProfileListener {

    private lateinit var binding: AddProductsBinding
    private lateinit var profileManager: ProfileManager
    private var isProfileShowing = false
    private var currentQuantity = 1

    private val viewModel: AddProductFormViewModel by viewModels {
        AddProductFormViewModelFactory(
            (application as com.example.foodcare.FoodCareApplication).productRepository
        )
    }

    // Список категорий
    private val categories = listOf(
        "Напитки",
        "Хлебобулочные изделия",
        "Мясо, птица",
        "Молочные продукты",
        "Фрукты",
        "Овощи",
        "Консервы",
        "Бакалея",
        "Замороженные продукты",
        "Сладости",
        "Соусы и приправы",
        "Рыба и морепродукты"
    )

    // Список единиц измерения
    private val units = listOf("гр", "мл", "кг", "л", "шт", "банка", "упаковка")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ВКЛЮЧАЕМ ПОЛНОЭКРАННЫЙ РЕЖИМ
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = AddProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем менеджер профиля
        profileManager = ProfileManager(this, binding.root)
        profileManager.setProfileListener(this)
        profileManager.initializeProfile()

        setupClickListeners()
        setupBackgroundDim()
        setupCategorySelection()
        setupQuantitySelector()
        setupUnitSelection() // ДОБАВЛЯЕМ ВЫБОР ЕДИНИЦ
    }

    private fun setupCategorySelection() {
        // Обработчик клика на поле категории
        binding.categoryEditText.setOnClickListener {
            showCategorySelectionDialog()
        }

        // Запрещаем ручной ввод - только выбор из списка
        binding.categoryEditText.isFocusable = false
        binding.categoryEditText.isClickable = true
    }

    private fun setupUnitSelection() {
        // Обработчик клика на поле единиц измерения
        binding.unitEditText.setOnClickListener {
            showUnitSelectionDialog()
        }

        // Запрещаем ручной ввод - только выбор из списка
        binding.unitEditText.isFocusable = false
        binding.unitEditText.isClickable = true
    }

    private fun showCategorySelectionDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_category_selection, null)
        dialog.setContentView(dialogView)

        val categoryListView = dialogView.findViewById<android.widget.ListView>(R.id.categoryListView)
        val searchEditText = dialogView.findViewById<android.widget.EditText>(R.id.searchEditText)
        val closeButton = dialogView.findViewById<android.widget.Button>(R.id.closeButton)

        // Создаем адаптер для списка категорий
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        categoryListView.adapter = adapter

        // Обработчик выбора категории
        categoryListView.setOnItemClickListener { parent, view, position, id ->
            val selectedCategory = adapter.getItem(position)
            binding.categoryEditText.setText(selectedCategory)
            dialog.dismiss()
        }

        // Поиск по категориям
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    categoryListView.adapter = adapter
                } else {
                    val filteredCategories = categories.filter {
                        it.contains(query, ignoreCase = true)
                    }
                    val filteredAdapter = android.widget.ArrayAdapter(
                        this@AddProductActivity,
                        android.R.layout.simple_list_item_1,
                        filteredCategories
                    )
                    categoryListView.adapter = filteredAdapter
                }
            }
        })

        // Кнопка закрытия
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showUnitSelectionDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_unit_selection, null)
        dialog.setContentView(dialogView)

        val unitListView = dialogView.findViewById<android.widget.ListView>(R.id.unitListView)
        val closeButton = dialogView.findViewById<android.widget.Button>(R.id.closeButton)

        // Создаем адаптер для списка единиц измерения
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, units)
        unitListView.adapter = adapter

        // Обработчик выбора единицы
        unitListView.setOnItemClickListener { parent, view, position, id ->
            val selectedUnit = adapter.getItem(position)
            binding.unitEditText.setText(selectedUnit)
            dialog.dismiss()
        }

        // Кнопка закрытия
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

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

        // Долгое нажатие для быстрого сброса/установки
        binding.decreaseQuantityButton.setOnLongClickListener {
            currentQuantity = 1
            updateQuantityDisplay()
            true
        }

        binding.increaseQuantityButton.setOnLongClickListener {
            currentQuantity = 10
            updateQuantityDisplay()
            true
        }

        updateQuantityDisplay()
    }

    private fun updateQuantityDisplay() {
        binding.quantityTextView.text = currentQuantity.toString()
        binding.decreaseQuantityButton.isEnabled = currentQuantity > 1

        // Визуальная обратная связь
        if (currentQuantity == 1) {
            binding.decreaseQuantityButton.alpha = 0.5f
        } else {
            binding.decreaseQuantityButton.alpha = 1.0f
        }
    }

    private fun setupClickListeners() {
        // Кнопка сохранения продукта
        binding.saveButton.setOnClickListener {
            saveProduct()
        }

        // Кнопка назад
        binding.backButton.setOnClickListener {
            finish()
        }

        // Кнопка профиля
        binding.profileButton.setOnClickListener {
            showProfile()
        }

        // Кнопка добавления штрих-кода
        binding.addBarcodeButton.setOnClickListener {
            openBarcodeScanner()
        }

        // Кнопка добавления фотографии
        binding.addPhotoButtonMain.setOnClickListener {
            openCamera()
        }

        // Центральная кнопка добавления по фото в нижней панели
        binding.addPhotoButtonBottom.setOnClickListener {
            openCamera()
        }

        // Обработчик для выбора даты
        binding.expiryDateEditText.setOnClickListener {
            showDatePicker()
        }

        // Дополнительные кнопки из нижней панели
        binding.buttonList.setOnClickListener {
            finish()
        }

        binding.buttonFridge.setOnClickListener {
            finish()
        }

        binding.buttonRecipes.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        // TODO: Реализовать выбор даты
        android.widget.Toast.makeText(this, "Выбор даты", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun setupBackgroundDim() {
        binding.backgroundDim.setOnClickListener {
            hideProfile()
        }
    }

    private fun saveProduct() {
        val productName = binding.productNameEditText.text.toString().trim()
        val category = binding.categoryEditText.text.toString().trim()
        val expiryDate = binding.expiryDateEditText.text.toString().trim()
        val quantity = currentQuantity
        val unit = binding.unitEditText.text.toString().trim()

        if (productName.isEmpty() || category.isEmpty() || expiryDate.isEmpty() || unit.isEmpty()) {
            android.widget.Toast.makeText(this, "Заполните все поля", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Сохранение продукта через ViewModel
        android.widget.Toast.makeText(this, "Продукт сохранен: $productName\nКоличество: $quantity $unit", android.widget.Toast.LENGTH_LONG).show()
        finish()
    }

    private fun showProfile() {
        profileManager.showProfile()
        binding.backgroundDim.visibility = View.VISIBLE
        binding.backgroundDim.isClickable = true
        isProfileShowing = true
        setOtherElementsEnabled(false)
    }

    private fun hideProfile() {
        profileManager.hideProfile()
        binding.backgroundDim.visibility = View.GONE
        binding.backgroundDim.isClickable = false
        isProfileShowing = false
        setOtherElementsEnabled(true)
    }

    private fun setOtherElementsEnabled(enabled: Boolean) {
        binding.backButton.isEnabled = enabled
        binding.saveButton.isEnabled = enabled
        binding.addBarcodeButton.isEnabled = enabled
        binding.addPhotoButtonMain.isEnabled = enabled
        binding.addPhotoButtonBottom.isEnabled = enabled
        binding.buttonList.isEnabled = enabled
        binding.buttonFridge.isEnabled = enabled
        binding.buttonRecipes.isEnabled = enabled
        binding.productNameEditText.isEnabled = enabled
        binding.categoryEditText.isEnabled = enabled
        binding.expiryDateEditText.isEnabled = enabled
        binding.unitEditText.isEnabled = enabled
        binding.decreaseQuantityButton.isEnabled = enabled
        binding.increaseQuantityButton.isEnabled = enabled
    }

    private fun openBarcodeScanner() {
        android.widget.Toast.makeText(this, "Сканирование штрих-кода", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun openCamera() {
        android.widget.Toast.makeText(this, "Открытие камеры", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onUserNameUpdated(newName: String) {}
    override fun onLogoutRequested() { performLogout() }
    override fun onProfileHidden() {
        binding.backgroundDim.visibility = View.GONE
        binding.backgroundDim.isClickable = false
        isProfileShowing = false
        setOtherElementsEnabled(true)
    }

    private fun performLogout() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        profileManager.cleanup()
    }
}