package com.example.foodcare.ui.app_product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import com.example.foodcare.R

class CategorySelectionDialogFragment : DialogFragment() {

    private lateinit var categoryListView: ListView
    private lateinit var closeButton: Button
    private var categorySelectedListener: ((String) -> Unit)? = null

    private val categories = listOf(
        "Молочные продукты", "Мясо, птица", "Овощи", "Фрукты", "Напитки",
        "Хлебобулочные изделия", "Бакалея", "Замороженные продукты", "Сладости",
        "Яйца", "Консервы", "Прочее", "Снэки"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_category_selection, container, false)

        categoryListView = view.findViewById(R.id.categoryListView)
        closeButton = view.findViewById(R.id.closeButton)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        categoryListView.adapter = adapter

        categoryListView.setOnItemClickListener { _, _, position, _ ->
            categorySelectedListener?.invoke(categories[position])
            dismiss() // Закрыть диалог после выбора категории
        }

        closeButton.setOnClickListener {
            dismiss() // Закрыть диалог
        }

        return view
    }

    fun setCategorySelectedListener(listener: (String) -> Unit) {
        categorySelectedListener = listener
    }
}
