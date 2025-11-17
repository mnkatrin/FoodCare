package com.example.foodcare.ui.app_product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodcare.R
import com.example.foodcare.network.OFFProduct

class ProductsSearchAdapter(
    private val context: Context,
    private val listener: (OFFProduct) -> Unit
) : RecyclerView.Adapter<ProductsSearchAdapter.ViewHolder>() {

    private val items = mutableListOf<OFFProduct>()

    fun submitList(list: List<OFFProduct>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_product_search, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.productName)
        private val category: TextView = itemView.findViewById(R.id.productCategory)
        private val image: ImageView = itemView.findViewById(R.id.productImage)

        fun bind(product: OFFProduct) {
            // Название: сначала русское, потом обычное, потом заглушка
            val displayName = product.productNameRu
                ?.takeIf { it.isNotBlank() }
                ?: product.productName
                ?: "Без названия"

            // Берём только первую категорию из списка
            val displayCategory = product.categories
                ?.split(",")
                ?.map { it.trim() }
                ?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "Без категории"

            name.text = displayName
            category.text = displayCategory

            Glide.with(context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_default_food)
                .error(R.drawable.ic_default_food)
                .centerCrop()
                .into(image)

            itemView.setOnClickListener { listener(product) }
        }
    }
}
