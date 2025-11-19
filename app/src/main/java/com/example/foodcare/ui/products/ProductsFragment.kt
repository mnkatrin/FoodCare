package com.example.foodcare.ui.products

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcare.FoodCareApplication
import com.example.foodcare.R
import com.example.foodcare.data.model.Product
import com.example.foodcare.ui.app_product.AddProductSearchFragment
import com.example.foodcare.ui.main.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class ProductsFragment : Fragment() {

    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button
    private lateinit var profileButton: Button

    private lateinit var adapter: ProductsAdapter
    private val products = mutableListOf<Product>()

    private val viewModel: ProductsViewModel by viewModels {
        ProductsViewModel.provideFactory(
            (requireActivity().application as FoodCareApplication).productRepository
        )
    }

    // Статичный список категорий
    private val categories = listOf(
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_products, container, false)

        productsRecyclerView = view.findViewById(R.id.productsRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyState)
        backButton = view.findViewById(R.id.backButton)
        button3 = view.findViewById(R.id.Button3)
        button4 = view.findViewById(R.id.Button4)
        button5 = view.findViewById(R.id.Button5)
        profileButton = view.findViewById(R.id.profileButton)

        setupRecycler()
        setupButtons()
        observeProducts()

        return view
    }

    private fun setupRecycler() {
        adapter = ProductsAdapter(
            items = products,
            onItemClick = { product -> showEditProductDialog(product) },
            onQuantityChange = { product, newQty ->
                val updated = product.copy(quantity = newQty, isDirty = true)
                viewModel.updateProduct(updated)
                // TODO: при необходимости писать в историю изменение количества
            }
        )

        productsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productsRecyclerView.adapter = adapter

        attachSwipe()
    }

    private fun attachSwipe() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return

                val product = adapter.getProductAt(position)

                // Показываем диалог с действиями
                val options = arrayOf(
                    "Отметить как использованный",
                    "Удалить (просрочен)",
                    "Отмена"
                )

                AlertDialog.Builder(requireContext())
                    .setTitle(product.name)
                    .setItems(options) { dialog, which ->
                        when (which) {
                            0 -> { // Использован
                                val updated = product.copy(
                                    quantity = 0.0,
                                    isDirty = true
                                )
                                viewModel.updateProduct(updated)
                                Toast.makeText(
                                    requireContext(),
                                    "Продукт отмечен как использованный",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // TODO: записать событие в историю: использован
                            }
                            1 -> { // Удалить (просрочен)
                                viewModel.deleteProduct(product)
                                Toast.makeText(
                                    requireContext(),
                                    "Продукт удалён как просроченный",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // TODO: записать событие в историю: удалён как просроченный
                            }
                            2 -> {
                                // Отмена — просто вернём карточку в исходное состояние
                            }
                        }

                        // В любом случае перерисуем элемент
                        adapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(productsRecyclerView)
    }

    private fun setupButtons() {
        // Назад
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Button3 – заглушка
        button3.setOnClickListener {
            Toast.makeText(requireContext(), "Раздел в разработке", Toast.LENGTH_SHORT).show()
        }

        // Button4 – открыть экран поиска
        button4.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragment_container, AddProductSearchFragment())
                ?.addToBackStack("addProductSearchFromProducts")
                ?.commit()
        }

        // Button5 – заглушка
        button5.setOnClickListener {
            Toast.makeText(requireContext(), "Раздел в разработке", Toast.LENGTH_SHORT).show()
        }

        // Профиль – вызываем метод активности
        profileButton.setOnClickListener {
            (activity as? MainActivity)?.openProfileFromFragment()
        }
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { list ->
                val filtered = list
                    .filter { it.isMyProduct && !it.isDeleted }
                    .sortedBy { it.getDaysUntilExpiration() }

                products.clear()
                products.addAll(filtered)
                adapter.notifyDataSetChanged()

                emptyStateText.visibility =
                    if (products.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    // --------- ВСПОМОГАТЕЛЬНОЕ ОКНО ВЫБОРА КАТЕГОРИИ ---------
    private fun showCategorySelectionDialog(target: AutoCompleteTextView) {
        AlertDialog.Builder(requireContext())
            .setTitle("Выберите категорию")
            .setItems(categories.toTypedArray()) { dialog, which ->
                target.setText(categories[which], false)
                dialog.dismiss()
            }
            .show()
    }

    // --------- Диалог редактирования продукта ---------
    private fun showEditProductDialog(product: Product) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_product, null)

        val productNameEditText =
            dialogView.findViewById<TextInputEditText>(R.id.productNameEditText)
        val categoryEditText =
            dialogView.findViewById<AutoCompleteTextView>(R.id.categoryEditText)
        val expirationDateEditText =
            dialogView.findViewById<TextInputEditText>(R.id.expirationDateEditText)
        val quantityEditText =
            dialogView.findViewById<TextInputEditText>(R.id.quantityEditText)
        val decreaseButton =
            dialogView.findViewById<MaterialButton>(R.id.decreaseButton)
        val increaseButton =
            dialogView.findViewById<MaterialButton>(R.id.increaseButton)
        val cancelButton =
            dialogView.findViewById<MaterialButton>(R.id.cancelButton)
        val saveButton =
            dialogView.findViewById<MaterialButton>(R.id.saveButton)

        // Название
        productNameEditText.setText(product.name)

        // Категория – выбор только из списка
        categoryEditText.inputType = InputType.TYPE_NULL
        categoryEditText.keyListener = null
        categoryEditText.isFocusable = false
        categoryEditText.isClickable = true

        if (product.category.isNotBlank()) {
            categoryEditText.setText(product.category, false)
        }

        categoryEditText.setOnClickListener {
            showCategorySelectionDialog(categoryEditText)
        }

        // Срок годности
        expirationDateEditText.setText(product.expirationDate)
        expirationDateEditText.isFocusable = false
        expirationDateEditText.isClickable = true

        expirationDateEditText.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val dateStr = String.format("%02d.%02d.%04d", d, m + 1, y)
                    expirationDateEditText.setText(dateStr)
                },
                year, month, day
            ).show()
        }

        // Количество
        quantityEditText.setText(
            if (product.quantity % 1.0 == 0.0)
                product.quantity.toInt().toString()
            else
                String.format("%.1f", product.quantity)
        )

        decreaseButton.setOnClickListener {
            val current =
                quantityEditText.text?.toString()?.replace(",", ".")?.toDoubleOrNull()
                    ?: 1.0
            val newValue = (current - 1.0).coerceAtLeast(0.1)
            quantityEditText.setText(
                if (newValue % 1.0 == 0.0)
                    newValue.toInt().toString()
                else
                    String.format("%.1f", newValue)
            )
        }

        increaseButton.setOnClickListener {
            val current =
                quantityEditText.text?.toString()?.replace(",", ".")?.toDoubleOrNull()
                    ?: 1.0
            val newValue = current + 1.0
            quantityEditText.setText(
                if (newValue % 1.0 == 0.0)
                    newValue.toInt().toString()
                else
                    String.format("%.1f", newValue)
            )
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val newName = productNameEditText.text?.toString()?.trim().orEmpty()
            val newCategory = categoryEditText.text?.toString()?.trim().orEmpty()
            val newExpiration = expirationDateEditText.text?.toString()?.trim().orEmpty()
            val quantityStr = quantityEditText.text?.toString()?.trim().orEmpty()

            if (newName.isEmpty() || newCategory.isEmpty() || newExpiration.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityStr.replace(",", ".").toDoubleOrNull()
            if (quantity == null || quantity <= 0.0) {
                Toast.makeText(requireContext(), "Некорректное количество", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedProduct = product.copy(
                name = newName,
                category = newCategory,
                expirationDate = newExpiration,
                quantity = quantity,
                isDirty = true
            )

            viewModel.updateProduct(updatedProduct)

            // локально обновим список
            val index = products.indexOfFirst { it.id == updatedProduct.id }
            if (index != -1) {
                products[index] = updatedProduct
                adapter.notifyItemChanged(index)
            }

            Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
