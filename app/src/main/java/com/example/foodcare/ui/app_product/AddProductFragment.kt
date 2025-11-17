package com.example.foodcare.ui.app_product

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.foodcare.R
import com.example.foodcare.data.model.Product
import com.example.foodcare.ui.app_product.AddProductSearchFragment
import com.example.foodcare.ui.app_product.AddProductViewModel
import com.example.foodcare.ui.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class AddProductFragment : Fragment() {

    private val viewModel: AddProductViewModel by viewModels {
        AddProductViewModel.provideFactory(requireActivity().application)
    }

    // Поля формы
    private lateinit var productNameEditText: TextInputEditText
    private lateinit var categoryEditText: TextInputEditText
    private lateinit var expiryDateEditText: TextInputEditText
    private lateinit var quantityTextView: TextView
    private lateinit var unitEditText: AutoCompleteTextView

    // Кнопки
    private lateinit var decreaseQuantityButton: MaterialButton
    private lateinit var increaseQuantityButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var bottomButton4: Button

    private var currentQuantity: Double = 1.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Макет формы
        return inflater.inflate(R.layout.add_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Привязка view к ID из add_products.xml
        productNameEditText = view.findViewById(R.id.productNameEditText)
        categoryEditText = view.findViewById(R.id.categoryEditText)
        expiryDateEditText = view.findViewById(R.id.expiryDateEditText)
        quantityTextView = view.findViewById(R.id.quantityTextView)
        unitEditText = view.findViewById(R.id.unitEditText)

        decreaseQuantityButton = view.findViewById(R.id.decreaseQuantityButton)
        increaseQuantityButton = view.findViewById(R.id.increaseQuantityButton)
        saveButton = view.findViewById(R.id.saveButton)
        backButton = view.findViewById(R.id.backButton)
        bottomButton4 = view.findViewById(R.id.Button4) // та самая Button4 в нижней панели

        setupUnitDropdown()
        setupQuantityControls()
        setupExpiryDatePicker()
        setupButtons()
    }

    // ------------- ЕДИНИЦЫ -------------
    private fun setupUnitDropdown() {
        val units = listOf("кг", "шт", "л")
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            units
        )
        unitEditText.setAdapter(adapter)
        unitEditText.setText(units.first(), false)
    }

    // ------------- КОЛИЧЕСТВО -------------
    private fun setupQuantityControls() {
        currentQuantity = 1.0
        updateQuantityText()

        decreaseQuantityButton.setOnClickListener {
            if (currentQuantity > 1.0) {
                currentQuantity -= 1.0
                updateQuantityText()
            }
        }

        increaseQuantityButton.setOnClickListener {
            currentQuantity += 1.0
            updateQuantityText()
        }
    }

    private fun updateQuantityText() {
        if (currentQuantity % 1.0 == 0.0) {
            quantityTextView.text = currentQuantity.toInt().toString()
        } else {
            quantityTextView.text = String.format("%.1f", currentQuantity)
        }
    }

    // ------------- ДАТА -------------
    private fun setupExpiryDatePicker() {
        expiryDateEditText.isFocusable = false
        expiryDateEditText.isClickable = true

        expiryDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val dateString = String.format("%02d.%02d.%04d", d, m + 1, y)
                    expiryDateEditText.setText(dateString)
                },
                year,
                month,
                day
            ).show()
        }
    }

    // ------------- КНОПКИ -------------
    private fun setupButtons() {
        // Назад
        backButton.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager?.popBackStack()
        }

        // Сохранить продукт
        saveButton.setOnClickListener {
            saveProduct()
        }

        // 🔹 Button4 → открыть fragment_add_product (экран поиска)
        bottomButton4.setOnClickListener {
            openSearchScreen()
        }
    }

    private fun openSearchScreen() {
        val fragment = AddProductSearchFragment()

        (activity as? MainActivity)?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.main, fragment) // ⚠️ тут укажи ID контейнера фрагментов в Activity
            ?.addToBackStack(null)
            ?.commit()
    }

    // ------------- СОХРАНЕНИЕ -------------
    private fun saveProduct() {
        val name = productNameEditText.text?.toString()?.trim().orEmpty()
        val category = categoryEditText.text?.toString()?.trim().orEmpty()
        val expiration = expiryDateEditText.text?.toString()?.trim().orEmpty()
        val unit = unitEditText.text?.toString()?.trim().orEmpty()

        if (TextUtils.isEmpty(name) ||
            TextUtils.isEmpty(category) ||
            TextUtils.isEmpty(expiration) ||
            TextUtils.isEmpty(unit)
        ) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentQuantity <= 0.0) {
            Toast.makeText(requireContext(), "Количество должно быть больше 0", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val newProduct = Product(
            name = name,
            category = category,
            quantity = currentQuantity,
            unit = unit,
            expirationDate = expiration,
            isMyProduct = true
        )

        viewModel.addProduct(newProduct)

        Toast.makeText(requireContext(), "Продукт добавлен", Toast.LENGTH_SHORT).show()
        (activity as? MainActivity)?.supportFragmentManager?.popBackStack()
    }
}
