package com.example.foodcare.ui.app_product

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcare.R
import com.example.foodcare.data.model.Product
import com.example.foodcare.ui.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.ArrayAdapter

class AddProductSearchFragment : Fragment() {

    private val viewModel: AddProductViewModel by viewModels {
        AddProductViewModel.provideFactory(requireActivity().application)
    }

    private lateinit var searchInput: AutoCompleteTextView
    private lateinit var addProductButton: MaterialButton
    private lateinit var backButton: View
    private lateinit var recentProductsRecyclerView: RecyclerView
    private lateinit var noRecentProductsText: TextView
    private lateinit var searchAdapter: ProductSearchResultAdapter
    private lateinit var recentAdapter: RecentProductsAdapter
    private var searchPopup: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchInput = view.findViewById(R.id.productSearchAutoComplete)
        addProductButton = view.findViewById(R.id.addProductButton)
        backButton = view.findViewById(R.id.backButton)

        recentProductsRecyclerView = view.findViewById(R.id.recentProductsRecyclerView)
        noRecentProductsText = view.findViewById(R.id.noRecentProductsText)

        setupRecentRecycler()
        setupSearchPopup()
        setupSearchField()
        setupObservers()
        setupButtons()
    }

    private fun setupRecentRecycler() {
        recentAdapter = RecentProductsAdapter(requireContext(), mutableListOf()) { product ->
            showExpirationDialog(product)
        }

        recentProductsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentAdapter
        }
    }

    private fun setupSearchPopup() {
        val popupView = layoutInflater.inflate(R.layout.layout_search_popup, null)
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.searchRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchAdapter = ProductSearchResultAdapter(requireContext()) { product ->
            onProductSelectedFromSearch(product)
        }
        recyclerView.adapter = searchAdapter

        searchPopup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
        }
    }

    private fun setupSearchField() {
        searchInput.addTextChangedListener { text ->
            val query = text?.toString()?.trim().orEmpty()
            if (query.length >= 2) {
                viewModel.searchProducts(query)
            } else {
                searchPopup?.dismiss()
            }
        }
    }

    private fun setupObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner) { products ->
            if (products.isNotEmpty()) {
                searchAdapter.updateList(products)
                if (searchPopup?.isShowing != true) {
                    val offsetY = (8 * resources.displayMetrics.density).toInt()
                    searchPopup?.showAsDropDown(searchInput, 0, offsetY)
                }
            } else {
                searchPopup?.dismiss()
            }
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (products.isNullOrEmpty()) {
                recentProductsRecyclerView.visibility = View.GONE
                noRecentProductsText.visibility = View.VISIBLE
            } else {
                val sorted = products.sortedByDescending { it.createdAt }.take(10)
                recentAdapter.updateList(sorted)

                recentProductsRecyclerView.visibility = View.VISIBLE
                noRecentProductsText.visibility = View.GONE
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        addProductButton.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragment_container, AddProductFragment())
                ?.addToBackStack("addProduct")
                ?.commit()
        }

        backButton.setOnClickListener {
            (activity as? MainActivity)?.onBackPressed()
        }
    }

    private fun onProductSelectedFromSearch(product: Product) {
        searchPopup?.dismiss()
        showExpirationDialog(product)
    }

    private fun showExpirationDialog(product: Product) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_set_expiration, null)

        val titleTextView = dialogView.findViewById<TextView>(R.id.titleTextView)
        val productInfoTextView = dialogView.findViewById<TextView>(R.id.productInfoText)
        val categoryEditText = dialogView.findViewById<AutoCompleteTextView>(R.id.categoryEditText)
        val expirationDateEditText = dialogView.findViewById<TextInputEditText>(R.id.expirationDateEditText)
        val quantityEditText = dialogView.findViewById<TextInputEditText>(R.id.quantityEditText)
        val decreaseButton = dialogView.findViewById<MaterialButton>(R.id.decreaseButton)
        val increaseButton = dialogView.findViewById<MaterialButton>(R.id.increaseButton)
        val unitEditText = dialogView.findViewById<TextInputEditText>(R.id.unitEditText)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancelButton)
        val addButton = dialogView.findViewById<MaterialButton>(R.id.addButton)

        titleTextView.text = "Установить срок годности"
        productInfoTextView.text = "Продукт: ${product.name}"

        // ---------- Категории ----------
        val categories = listOf(
            "Молочные продукты",
            "Мясо, птица",
            "Овощи",
            "Фрукты",
            "Напитки",
            "Хлебобулочные изделия",
            "Бакалея",
            "Замороженные продукты",
            "Сладости",
            "Яйца",
            "Консервы",
            "Снэки",
            "Прочее"

        )

        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        categoryEditText.setAdapter(categoryAdapter)

        categoryEditText.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Выберите категорию")
                .setItems(categories.toTypedArray()) { _, which ->
                    categoryEditText.setText(categories[which])
                }
                .show()
        }

        val rawCategory = product.category.trim()
        if (rawCategory.isNotEmpty()) {
            categoryEditText.setText(rawCategory, false)
        }

        // ---------- Дата ----------
        expirationDateEditText.isFocusable = false
        expirationDateEditText.isClickable = true

        expirationDateEditText.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val dateString = String.format("%02d.%02d.%04d", d, m + 1, y)
                    expirationDateEditText.setText(dateString)
                },
                year, month, day
            ).show()
        }

        // ---------- Количество ----------
        quantityEditText.setText("1.0")

        decreaseButton.setOnClickListener {
            val current = quantityEditText.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 1.0
            val newValue = (current - 1.0).coerceAtLeast(0.1)
            quantityEditText.setText(newValue.toString())
        }

        increaseButton.setOnClickListener {
            val current = quantityEditText.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 1.0
            val newValue = current + 1.0
            quantityEditText.setText(newValue.toString())
        }

        // ---------- Единицы ----------
        val units = listOf("кг", "шт", "л")
        unitEditText.isFocusable = false
        unitEditText.isClickable = true
        unitEditText.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Единица измерения")
                .setItems(units.toTypedArray()) { _, which ->
                    unitEditText.setText(units[which])
                }
                .show()
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        addButton.setOnClickListener {
            val dateStr = expirationDateEditText.text?.toString()?.trim().orEmpty()
            val quantityStr = quantityEditText.text?.toString()?.trim().orEmpty()
            val unitStr = unitEditText.text?.toString()?.trim().orEmpty()
            val categoryStr = categoryEditText.text?.toString()?.trim().orEmpty()

            if (dateStr.isEmpty() || quantityStr.isEmpty() || unitStr.isEmpty() || categoryStr.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityStr.replace(",", ".").toDoubleOrNull()
            if (quantity == null || quantity <= 0.0) {
                Toast.makeText(requireContext(), "Некорректное количество", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val finalProduct = product.copy(
                category = categoryStr,
                expirationDate = dateStr,
                quantity = quantity,
                unit = unitStr,
                isMyProduct = true
            )

            viewModel.addProduct(finalProduct)
            Toast.makeText(requireContext(), "Продукт добавлен", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchPopup?.dismiss()
        searchPopup = null
    }
}


