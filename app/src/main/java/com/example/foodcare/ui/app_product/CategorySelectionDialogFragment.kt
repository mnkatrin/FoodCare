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

class CategorySelectionDialogFragment(
    private val categories: List<String>,  // Список категорий
    private val onCategorySelected: (String) -> Unit // Функция, которая будет вызвана при выборе категории
) : DialogFragment() {

    private lateinit var categoryListView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.dialog_category_selection, container, false)

        // Инициализация ListView
        categoryListView = view.findViewById(R.id.categoryListView)
        val closeButton: Button = view.findViewById(R.id.closeButton)

        // Настройка адаптера для ListView с категориями
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        categoryListView.adapter = adapter

        // Обработка клика по элементу списка
        categoryListView.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = categories[position]
            onCategorySelected(selectedCategory)  // Отправляем выбранную категорию обратно
            dismiss()  // Закрываем диалог
        }

        // Кнопка закрытия
        closeButton.setOnClickListener {
            dismiss()  // Просто закрываем диалог
        }

        return view
    }
}
