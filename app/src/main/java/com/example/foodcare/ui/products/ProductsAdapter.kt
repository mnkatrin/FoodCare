package com.example.foodcare.ui.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcare.R
import com.example.foodcare.data.model.Product

class ProductAdapter(
    private val onQuantityChanged: (Product, String) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view, onQuantityChanged)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    class ProductViewHolder(
        itemView: View,
        private val onQuantityChanged: (Product, String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        private val productExpiration: TextView = itemView.findViewById(R.id.productExpiration)
        private val productQuantity: TextView = itemView.findViewById(R.id.productQuantity)
        private val btnDecrease: TextView = itemView.findViewById(R.id.btnDecrease)
        private val btnIncrease: TextView = itemView.findViewById(R.id.btnIncrease)

        fun bind(product: Product) {
            productName.text = product.name
            productCategory.text = product.category

            // Форматируем срок годности
            val daysLeft = product.getDaysUntilExpiration()
            val expirationText = when {
                daysLeft == 0L -> "Истекает сегодня"
                daysLeft == 1L -> "Осталось: 1 день"
                daysLeft < 5L -> "Осталось: $daysLeft дня"
                else -> "Осталось: $daysLeft дней"
            }
            productExpiration.text = expirationText

            // Устанавливаем цвет в зависимости от срока годности
            when {
                daysLeft <= 2 -> {
                    // Красный для срочных продуктов (0-2 дня)
                    productExpiration.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                    productExpiration.setBackgroundResource(R.drawable.expiration_red)
                }
                daysLeft <= 10 -> {
                    // Желтый для средних сроков (3-10 дней)
                    productExpiration.setTextColor(ContextCompat.getColor(itemView.context, R.color.yellow_dark))
                    productExpiration.setBackgroundResource(R.drawable.expiration_yellow)
                }
                else -> {
                    // Зеленый для долгих сроков (11+ дней)
                    productExpiration.setTextColor(ContextCompat.getColor(itemView.context, R.color.green_dark))
                    productExpiration.setBackgroundResource(R.drawable.expiration_green)
                }
            }

            // Устанавливаем количество
            productQuantity.text = product.quantity

            // Обработчики для кнопок +/-
            btnDecrease.setOnClickListener {
                val (currentValue, unit) = parseQuantity(product.quantity)
                if (currentValue > getMinQuantity(unit)) {
                    val newQuantity = formatQuantity(currentValue - getStep(unit), unit)
                    onQuantityChanged(product, newQuantity)
                }
            }

            btnIncrease.setOnClickListener {
                val (currentValue, unit) = parseQuantity(product.quantity)
                val newQuantity = formatQuantity(currentValue + getStep(unit), unit)
                onQuantityChanged(product, newQuantity)
            }
        }

        // Вспомогательные функции для работы с разными единицами
        private fun parseQuantity(quantity: String): Pair<Double, String> {
            return try {
                val value = quantity.replace(" шт", "").replace(" л", "").replace(" мл", "")
                    .replace(" г", "").replace(" кг", "").replace(" банки", "").trim()
                val unit = when {
                    quantity.contains("л") && !quantity.contains("мл") -> "л"
                    quantity.contains("мл") -> "мл"
                    quantity.contains("кг") -> "кг"
                    quantity.contains("г") -> "г"
                    quantity.contains("банки") -> "банки"
                    else -> "шт"
                }
                Pair(value.toDouble(), unit)
            } catch (e: Exception) {
                Pair(1.0, "шт")
            }
        }

        private fun formatQuantity(value: Double, unit: String): String {
            return when (unit) {
                "л", "кг" -> if (value == value.toInt().toDouble()) "${value.toInt()} $unit" else "$value $unit"
                "мл", "г" -> "${value.toInt()} $unit" // для мл и г используем целые числа
                "банки" -> "${value.toInt()} банки"
                else -> "${value.toInt()} шт"
            }
        }

        private fun getMinQuantity(unit: String): Double {
            return when (unit) {
                "л", "кг" -> 0.1
                "мл", "г" -> 50.0 // минимально 50 мл/г
                "банки" -> 1.0
                else -> 1.0 // для штук
            }
        }

        private fun getStep(unit: String): Double {
            return when (unit) {
                "л", "кг" -> 0.1 // шаг 100 мл для литров, 100 г для кг
                "мл", "г" -> 50.0 // шаг 50 мл/г
                "банки" -> 1.0
                else -> 1.0 // для штук
            }
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}