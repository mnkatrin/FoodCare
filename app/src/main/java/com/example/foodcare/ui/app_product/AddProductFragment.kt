package com.example.foodcare.ui.app_product

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.foodcare.R
import com.example.foodcare.data.model.Product
import com.example.foodcare.data.repository.ProductRepository
import com.example.foodcare.databinding.FragmentAddProductBinding
import com.example.foodcare.ui.profile.ProfileManager
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddProductFragment : Fragment(), ProfileManager.ProfileListener {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileManager: ProfileManager
    private var isProfileShowing = false
    private val calendar = Calendar.getInstance()

    // Получаем repository из FoodCareApplication
    private val repository: ProductRepository by lazy {
        (requireContext().applicationContext as com.example.foodcare.FoodCareApplication).productRepository
    }

    private val viewModel: AddProductViewModel by viewModels {
        AddProductViewModelFactory(repository)
    }

    // Единицы измерения по категориям
    private val categoryUnits = mapOf(
        "Напитки" to arrayOf("л", "мл", "шт"),
        "Хлебобулочные изделия" to arrayOf("шт", "г", "кг"),
        "Мясо, птица" to arrayOf("кг", "г", "шт"),
        "Молочные продукты" to arrayOf("л", "мл", "шт"),
        "Фрукты" to arrayOf("кг", "г", "шт"),
        "Овощи" to arrayOf("кг", "г", "шт"),
        "Консервы" to arrayOf("шт", "банки"),
        "Бакалея" to arrayOf("кг", "г", "шт")
    )

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

        // Инициализируем менеджер профиля
        profileManager = ProfileManager(requireContext(), binding.root)
        profileManager.setProfileListener(this)
        profileManager.initializeProfile()

        setupClickListeners()
        setupBackgroundDim()
    }

    private fun setupClickListeners() {
        // Кнопка назад
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Кнопка профиля в нижней панели
        binding.profileButton.setOnClickListener {
            showProfile()
        }

        // Центральная кнопка добавления по фото
        binding.addPhotoButton.setOnClickListener {
            openCameraOrGallery()
        }

        // Кнопки добавления продуктов из списка
        binding.addProduct1.setOnClickListener {
            showExpirationDateDialog("Апельсиновый сок", "Напитки")
        }

        binding.addProduct2.setOnClickListener {
            showExpirationDateDialog("Хлеб Коломенский", "Хлебобулочные изделия")
        }

        binding.addProduct3.setOnClickListener {
            showExpirationDateDialog("Яйца куриные", "Мясо, птица")
        }
    }

    private fun setupBackgroundDim() {
        // Обработчик клика на затемнение для закрытия профиля
        binding.backgroundDim.setOnClickListener {
            hideProfile()
        }

        // Блокируем клики на другие элементы когда профиль открыт
        binding.main.setOnClickListener {
            if (isProfileShowing) {
                hideProfile()
            }
        }

        // Блокируем поиск когда профиль открыт
        binding.searchCard.setOnClickListener {
            if (isProfileShowing) {
                hideProfile()
            }
        }
    }

    private fun showExpirationDateDialog(productName: String, category: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_set_expiration)

        val expirationDateEditText = dialog.findViewById<TextInputEditText>(R.id.expirationDateEditText)
        val quantityEditText = dialog.findViewById<EditText>(R.id.quantityEditText)
        val unitSpinner = dialog.findViewById<Spinner>(R.id.unitSpinner)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val addButton = dialog.findViewById<Button>(R.id.addButton)

        // Настраиваем Spinner с единицами измерения для категории
        val units = categoryUnits[category] ?: arrayOf("шт")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = adapter

        // Обработчик выбора даты
        expirationDateEditText.setOnClickListener {
            showDatePickerDialog(expirationDateEditText)
        }

        // Кнопка отмены
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Кнопка добавления
        addButton.setOnClickListener {
            val expirationDate = expirationDateEditText.text.toString()
            val quantityValue = quantityEditText.text.toString()
            val selectedUnit = unitSpinner.selectedItem.toString()

            // Формируем полную строку количества
            val quantity = if (quantityValue.isNotEmpty()) {
                "$quantityValue $selectedUnit"
            } else {
                "1 $selectedUnit"
            }

            if (expirationDate.isNotEmpty()) {
                addProductToMyProducts(productName, category, expirationDate, quantity)
                dialog.dismiss()
            } else {
                showToast("Выберите дату окончания срока годности")
            }
        }

        dialog.show()
    }

    private fun showDatePickerDialog(dateEditText: TextInputEditText) {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, day: Int ->
                calendar.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                dateEditText.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun addProductToMyProducts(
        productName: String,
        category: String,
        expirationDate: String,
        quantity: String
    ) {
        try {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val expirationDateObj = dateFormat.parse(expirationDate) ?: Date()

            val product = Product(
                name = productName,
                category = category,
                expirationDate = expirationDateObj.time,
                quantity = quantity
            )

            // Используем ViewModel для добавления продукта
            viewModel.addProduct(product)

            showToast("$productName добавлен в мои продукты!\nСрок годности: $expirationDate\nКоличество: $quantity")
            requireActivity().onBackPressed()

        } catch (e: Exception) {
            showToast("Ошибка при добавлении продукта: ${e.message}")
        }
    }

    private fun openCameraOrGallery() {
        showToast("Открытие камеры/галереи")
    }

    private fun showProfile() {
        profileManager.showProfile()
        binding.backgroundDim.visibility = View.VISIBLE
        binding.backgroundDim.isClickable = true
        isProfileShowing = true

        // Блокируем взаимодействие с другими элементами
        setOtherElementsEnabled(false)
    }

    private fun hideProfile() {
        profileManager.hideProfile()
        binding.backgroundDim.visibility = View.GONE
        binding.backgroundDim.isClickable = false
        isProfileShowing = false

        // Разблокируем взаимодействие с другими элементами
        setOtherElementsEnabled(true)
    }

    private fun setOtherElementsEnabled(enabled: Boolean) {
        // Блокируем/разблокируем основные элементы интерфейса
        binding.backButton.isEnabled = enabled
        binding.addProductButton.isEnabled = enabled
        binding.searchCard.isEnabled = enabled
        binding.addPhotoButton.isEnabled = enabled
        binding.addProduct1.isEnabled = enabled
        binding.addProduct2.isEnabled = enabled
        binding.addProduct3.isEnabled = enabled
    }

    // Реализация методов ProfileListener
    override fun onUserNameUpdated(newName: String) {
        showToast("Имя обновлено: $newName")
    }

    override fun onLogoutRequested() {
        performLogout()
    }

    override fun onProfileHidden() {
        binding.backgroundDim.visibility = View.GONE
        binding.backgroundDim.isClickable = false
        isProfileShowing = false
        setOtherElementsEnabled(true)
    }

    private fun performLogout() {
        showToast("Выход из аккаунта выполнен")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profileManager.cleanup()
        _binding = null
    }
}