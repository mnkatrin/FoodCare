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

        // ПОЛНОЭКРАННЫЙ РЕЖИМ
        makeFullScreen()

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
        setupUnitSelection()
    }

    private fun makeFullScreen() {
        // Убираем статус бар и навигационную панель
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Для новых версий Android
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun setupCategorySelection() {
        binding.categoryEditText.setOnClickListener {
            showCategorySelectionDialog()
        }
        binding.categoryEditText.isFocusable = false
        binding.categoryEditText.isClickable = true
    }

    private fun setupUnitSelection() {
        binding.unitEditText.setOnClickListener {
            showUnitSelectionDialog()
        }
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

        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        categoryListView.adapter = adapter

        categoryListView.setOnItemClickListener { parent, view, position, id ->
            val selectedCategory = adapter.getItem(position)
            binding.categoryEditText.setText(selectedCategory)
            dialog.dismiss()
        }

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

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showUnitSelectionDialog() {
        // Временное решение с AlertDialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Выберите единицу измерения")
        builder.setItems(units.toTypedArray()) { dialog, which ->
            val selectedUnit = units[which]
            binding.unitEditText.setText(selectedUnit)
            dialog.dismiss()
        }
        builder.setNegativeButton("Закрыть") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
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

        if (currentQuantity == 1) {
            binding.decreaseQuantityButton.alpha = 0.5f
        } else {
            binding.decreaseQuantityButton.alpha = 1.0f
        }
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            saveProduct()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.profileButton.setOnClickListener {
            showProfile()
        }

        binding.addBarcodeButton.setOnClickListener {
            openBarcodeScanner()
        }

        binding.addPhotoButtonMain.setOnClickListener {
            openCamera()
        }

        binding.addPhotoButtonBottom.setOnClickListener {
            openCamera()
        }

        binding.expiryDateEditText.setOnClickListener {
            showDatePicker()
        }

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

        android.widget.Toast.makeText(this, "Продукт сохранен: $productName\nКоличество: $quantity $unit", android.widget.Toast.LENGTH_LONG).show()
        finish()
    }

    private fun showProfile() {
        profileManager.showProfile()
        binding.backgroundDim.visibility = View.VISIBLE
        binding.backgroundDim.isClickable = true
        isProfileShowing = true
        setOtherElementsEnabled(false)

        // Блокируем прокрутку основного контента
        binding.main.isEnabled = false
    }

    private fun hideProfile() {
        profileManager.hideProfile()
        binding.backgroundDim.visibility = View.GONE
        binding.backgroundDim.isClickable = false
        isProfileShowing = false
        setOtherElementsEnabled(true)

        // Разблокируем основной контент
        binding.main.isEnabled = true
    }

    private fun setOtherElementsEnabled(enabled: Boolean) {
        val elements = arrayOf(
            binding.backButton,
            binding.saveButton,
            binding.addBarcodeButton,
            binding.addPhotoButtonMain,
            binding.addPhotoButtonBottom,
            binding.buttonList,
            binding.buttonFridge,
            binding.buttonRecipes,
            binding.productNameEditText,
            binding.categoryEditText,
            binding.expiryDateEditText,
            binding.unitEditText,
            binding.decreaseQuantityButton,
            binding.increaseQuantityButton
        )

        elements.forEach { it?.isEnabled = enabled }
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
        binding.main.isEnabled = true
    }

    private fun performLogout() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        profileManager.cleanup()
    }
}