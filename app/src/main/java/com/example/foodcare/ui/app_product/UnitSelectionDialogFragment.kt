package com.example.foodcare.ui.app_product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.foodcare.R

class UnitSelectionDialogFragment(private val onUnitSelected: (String) -> Unit) : DialogFragment() {

    private lateinit var unitListView: ListView
    private lateinit var closeButton: Button

    private val units = listOf("кг", "шт", "л") // Пример единиц измерения

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_unit_selection, container, false)

        unitListView = view.findViewById(R.id.unitListView)
        closeButton = view.findViewById(R.id.closeButton)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, units)
        unitListView.adapter = adapter

        unitListView.setOnItemClickListener { _, _, position, _ ->
            onUnitSelected(units[position])
            dismiss()
        }

        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }
}
