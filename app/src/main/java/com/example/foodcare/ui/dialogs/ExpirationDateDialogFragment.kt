package com.example.foodcare.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.foodcare.R
import com.example.foodcare.data.model.ProductArgs
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class ExpirationDateDialogFragment : DialogFragment() {

    private var onDateSetListener: ((String, Double, String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireContext().getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE) as android.view.LayoutInflater
        val view = inflater.inflate(R.layout.dialog_set_expiration, null)

        val expirationDateEditText = view.findViewById<EditText>(R.id.expirationDateEditText)
        val quantityEditText = view.findViewById<EditText>(R.id.quantityEditText)
        val unitEditText = view.findViewById<EditText>(R.id.unitEditText)
        val decreaseButton = view.findViewById<ImageButton>(R.id.decreaseButton)
        val increaseButton = view.findViewById<ImageButton>(R.id.increaseButton)
        val cancelButton = view.findViewById<MaterialButton>(R.id.cancelButton)
        val addButton = view.findViewById<MaterialButton>(R.id.addButton)

        val selectedProduct = arguments?.getParcelable<ProductArgs>("product_arg")!!

        var currentQuantity = selectedProduct.quantity.takeIf { it > 0 } ?: 1.0
        quantityEditText.setText(String.format("%.1f", currentQuantity))
        unitEditText.setText(selectedProduct.unit)

        val calendar = Calendar.getInstance()
        expirationDateEditText.setOnClickListener {
            val datePicker = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    expirationDateEditText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis()
            datePicker.show()
        }

        decreaseButton.setOnClickListener {
            if (currentQuantity > 0.1) {
                currentQuantity = (currentQuantity - 0.1).coerceAtLeast(0.1)
                quantityEditText.setText(String.format("%.1f", currentQuantity))
            }
        }
        increaseButton.setOnClickListener {
            currentQuantity += 0.1
            quantityEditText.setText(String.format("%.1f", currentQuantity))
        }

        cancelButton.setOnClickListener { dismiss() }

        addButton.setOnClickListener {
            val dateStr = expirationDateEditText.text.toString()
            val quantityValue = quantityEditText.text.toString().toDoubleOrNull() ?: 1.0
            val unitStr = unitEditText.text.toString()
            if (dateStr.isEmpty()) {
                expirationDateEditText.error = "Выберите дату"
                return@setOnClickListener
            }
            onDateSetListener?.invoke(dateStr, quantityValue, unitStr)
            dismiss()
        }

        return AlertDialog.Builder(requireContext()).setView(view).create()
    }

    companion object {
        fun newInstance(product: ProductArgs, onDateSet: (String, Double, String) -> Unit): ExpirationDateDialogFragment {
            val fragment = ExpirationDateDialogFragment()
            fragment.onDateSetListener = onDateSet
            fragment.arguments = Bundle().apply { putParcelable("product_arg", product) }
            return fragment
        }
    }
}
