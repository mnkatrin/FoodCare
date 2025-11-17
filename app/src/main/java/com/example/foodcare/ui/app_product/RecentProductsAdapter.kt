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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecentProductsAdapter(
    private val context: Context,
    private val products: MutableList<Product>,
    private val onAddClick: (Product) -> Unit
) : RecyclerView.Adapter<RecentProductsAdapter.RecentViewHolder>() {

    inner class RecentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        val addButton: FloatingActionButton = itemView.findViewById(R.id.addButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recent_product, parent, false)
        return RecentViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        val product = products[position]
        holder.productName.text = product.name
        holder.productCategory.text = product.category

        Glide.with(context)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_default_food)
            .into(holder.productImage)

        holder.addButton.setOnClickListener {
            onAddClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateList(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}



