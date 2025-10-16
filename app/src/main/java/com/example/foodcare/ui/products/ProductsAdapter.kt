package com.example.foodcare.ui.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

            // Устанавливаем количество
            productQuantity.text = product.quantity

            // Обработчики для кнопок +/-
            btnDecrease.setOnClickListener {
                val currentQuantity = product.quantity.replace(" шт", "").replace(" л", "").replace(" буханка", "").replace(" г", "").replace(" кг", "").replace(" банки", "").toIntOrNull() ?: 1
                if (currentQuantity > 1) {
                    val quantityText = product.quantity.substringAfterLast(" ")
                    val newQuantity = (currentQuantity - 1).toString() + " " + quantityText
                    onQuantityChanged(product, newQuantity)
                }
            }

            btnIncrease.setOnClickListener {
                val currentQuantity = product.quantity.replace(" шт", "").replace(" л", "").replace(" буханка", "").replace(" г", "").replace(" кг", "").replace(" банки", "").toIntOrNull() ?: 1
                val quantityText = product.quantity.substringAfterLast(" ")
                val newQuantity = (currentQuantity + 1).toString() + " " + quantityText
                onQuantityChanged(product, newQuantity)
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