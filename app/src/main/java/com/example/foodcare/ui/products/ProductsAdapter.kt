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

class ProductsAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onProductLongClick: (Product) -> Unit
) : ListAdapter<Product, ProductsAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.productName)
        private val categoryTextView: TextView = itemView.findViewById(R.id.productCategory)
        private val expirationTextView: TextView = itemView.findViewById(R.id.productExpiration)

        fun bind(product: Product) {
            nameTextView.text = product.name
            categoryTextView.text = product.category

            val daysLeft = product.getDaysUntilExpiration()

            // Устанавливаем текст и цвет
            when {
                daysLeft == 0L -> {
                    expirationTextView.text = "Истекает сегодня!"
                    expirationTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                }
                daysLeft <= 3 -> {
                    expirationTextView.text = "Скоро истекает: $daysLeft дн."
                    expirationTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.orange))
                }
                else -> {
                    expirationTextView.text = "Осталось: $daysLeft дн."
                    expirationTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                }
            }

            itemView.setOnClickListener { onProductClick(product) }
            itemView.setOnLongClickListener {
                onProductLongClick(product)
                true
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