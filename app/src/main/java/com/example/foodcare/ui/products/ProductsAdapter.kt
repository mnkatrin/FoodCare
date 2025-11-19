package com.example.foodcare.ui.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodcare.R
import com.example.foodcare.data.model.Product

class ProductsAdapter(
    private val items: MutableList<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onQuantityChange: (Product, Double) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productExpiration: TextView = itemView.findViewById(R.id.productExpiration)
        val productQuantity: TextView = itemView.findViewById(R.id.productQuantity)
        val btnDecrease: TextView = itemView.findViewById(R.id.btnDecrease)
        val btnIncrease: TextView = itemView.findViewById(R.id.btnIncrease)
        val counterLayout: LinearLayout = itemView.findViewById(R.id.counterLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = items[position]

        holder.productName.text = product.name
        holder.productCategory.text = product.category.ifBlank { "Без категории" }

        // Количество
        holder.productQuantity.text =
            if (product.quantity % 1.0 == 0.0)
                product.quantity.toInt().toString()
            else
                String.format("%.1f", product.quantity)

        // Картинка продукта
        if (product.imageUrl.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_food_placeholder)
                .into(holder.productImage)
        } else {
            holder.productImage.setImageResource(R.drawable.ic_food_placeholder)
        }

        // Срок годности
        val daysLeft = product.getDaysUntilExpiration()
        val context = holder.itemView.context

        val text = when {
            daysLeft < 0  -> "Просрочен"
            daysLeft == 0 -> "Истекает сегодня"
            daysLeft == 1 -> "Остался 1 день"
            daysLeft in 2..4 -> "Осталось $daysLeft дня"
            else -> "Осталось $daysLeft дней"
        }

        val colorRes = when {
            daysLeft < 0  -> android.R.color.holo_red_light
            daysLeft <= 3 -> android.R.color.holo_orange_light
            else          -> android.R.color.holo_green_light
        }

        holder.productExpiration.text = text
        holder.productExpiration.setTextColor(
            ContextCompat.getColor(context, colorRes)
        )

        // Тап по карточке — открываем диалог редактирования
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }

        // Минус
        holder.btnDecrease.setOnClickListener {
            val current = product.quantity
            val newValue = (current - 1.0).coerceAtLeast(0.1)
            holder.productQuantity.text =
                if (newValue % 1.0 == 0.0)
                    newValue.toInt().toString()
                else
                    String.format("%.1f", newValue)

            onQuantityChange(product, newValue)
        }

        // Плюс
        holder.btnIncrease.setOnClickListener {
            val current = product.quantity
            val newValue = current + 1.0
            holder.productQuantity.text =
                if (newValue % 1.0 == 0.0)
                    newValue.toInt().toString()
                else
                    String.format("%.1f", newValue)

            onQuantityChange(product, newValue)
        }
    }

    fun updateProducts(newProducts: List<Product>) {
        items.clear()
        items.addAll(newProducts)
        notifyDataSetChanged()
    }

    fun getProductAt(position: Int): Product = items[position]
}
