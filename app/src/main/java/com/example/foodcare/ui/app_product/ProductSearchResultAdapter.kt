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
import com.example.foodcare.data.model.Product

class ProductSearchResultAdapter(
    private val context: Context,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductSearchResultAdapter.ViewHolder>() {

    private var productList: List<Product> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val productName: TextView = view.findViewById(R.id.productName)
        val productCategory: TextView = view.findViewById(R.id.productCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_search, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name
        holder.productCategory.text =
            if (product.category.isEmpty()) "Категория не указана" else product.category

        Glide.with(context)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_default_food)
            .error(R.drawable.ic_default_food)
            .into(holder.productImage)

        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    // Метод для обновления списка
    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged() // Обновить адаптер
    }
}
