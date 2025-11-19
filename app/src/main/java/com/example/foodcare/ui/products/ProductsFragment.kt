package com.example.foodcare.ui.products

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.foodcare.ui.main.MainActivity
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcare.FoodCareApplication
import com.example.foodcare.R
import com.example.foodcare.data.model.Product
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {

    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var profileButton: Button

    private lateinit var adapter: ProductsAdapter
    private val products = mutableListOf<Product>()

    private val viewModel: ProductsViewModel by viewModels {
        ProductsViewModel.provideFactory(
            (requireActivity().application as FoodCareApplication).productRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false)

        // Initialize views
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyState)
        backButton = view.findViewById(R.id.backButton)
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
            }
        )

        productsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productsRecyclerView.adapter = adapter

        attachSwipe()
    }

    private fun attachSwipe() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return

                val product = adapter.getProductAt(position)

                val options = arrayOf("Отметить как использованный", "Удалить (просрочен)", "Отмена")

                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle(product.name)
                    .setItems(options) { dialog, which ->
                        when (which) {
                            0 -> { // Использован
                                val updated = product.copy(quantity = 0.0, isDirty = true)
                                viewModel.updateProduct(updated)
                                products.removeAt(position)
                                adapter.notifyItemRemoved(position)
                                Toast.makeText(requireContext(), "Продукт отмечен как использованный", Toast.LENGTH_SHORT).show()
                            }
                            1 -> { // Удалить (просрочен)
                                viewModel.deleteProduct(product)
                                products.removeAt(position)
                                adapter.notifyItemRemoved(position)
                                Toast.makeText(requireContext(), "Продукт удалён как просроченный", Toast.LENGTH_SHORT).show()
                            }
                            2 -> { // Отмена
                                adapter.notifyItemChanged(position)
                            }
                        }
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .create()

                dialog.show()
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(productsRecyclerView)
    }

    private fun setupButtons() {
        // Назад
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Профиль – вызываем метод активности
        profileButton.setOnClickListener {
            // В этом примере метод `openProfile` должен быть доступен в вашем `MainActivity`
            (activity as? MainActivity)?.openProfile()
        }
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { list ->
                products.clear()
                products.addAll(list)
                adapter.notifyDataSetChanged()

                emptyStateText.visibility =
                    if (products.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    // --------- Диалог редактирования продукта ---------
    private fun showEditProductDialog(product: Product) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_product, null)

        val productNameEditText = dialogView.findViewById<TextInputEditText>(R.id.productNameEditText)
        val categoryEditText = dialogView.findViewById<AutoCompleteTextView>(R.id.categoryEditText)
        val expirationDateEditText = dialogView.findViewById<TextInputEditText>(R.id.expirationDateEditText)
        val quantityEditText = dialogView.findViewById<TextInputEditText>(R.id.quantityEditText)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<MaterialButton>(R.id.saveButton)

        // Set current values
        productNameEditText.setText(product.name)
        categoryEditText.setText(product.category)
        expirationDateEditText.setText(product.expirationDate)
        quantityEditText.setText(product.quantity.toString())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val updatedProduct = product.copy(
                name = productNameEditText.text.toString(),
                category = categoryEditText.text.toString(),
                expirationDate = expirationDateEditText.text.toString(),
                quantity = quantityEditText.text.toString().toDouble()
            )
            viewModel.updateProduct(updatedProduct)
            dialog.dismiss()
        }

        dialog.show()
    }
}
