package com.example.foodcare.history

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcare.R
import com.example.foodcare.data.model.HistoryEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val items: MutableList<HistoryEvent> = mutableListOf()
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    fun submitList(newItems: List<HistoryEvent>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_event, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, dateFormatter)
    }

    override fun getItemCount(): Int = items.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val productImage: ImageView =
            itemView.findViewById(R.id.historyProductImage)
        private val productNameText: TextView =
            itemView.findViewById(R.id.historyProductName)
        private val dateText: TextView =
            itemView.findViewById(R.id.historyDateText)
        private val actionText: TextView =
            itemView.findViewById(R.id.historyActionText)
        private val statusChip: TextView =
            itemView.findViewById(R.id.historyStatusChip)

        fun bind(item: HistoryEvent, dateFormatter: SimpleDateFormat) {
            // Название
            productNameText.text = item.productName

            // Текст действия (например "Использовано 0.5 л")
            actionText.text = item.quantityText

            // Дата без времени
            val dateString = dateFormatter.format(Date(item.createdAt))
            dateText.text = dateString

            // Иконка (пока заглушка)
            productImage.setImageResource(R.drawable.ic_food_placeholder)

            // Статусный чип по строке actionType: "USED" / "DISCARDED"
            when (item.actionType) {
                "USED" -> {
                    statusChip.text = "Использовано"
                    statusChip.setTextColor(Color.parseColor("#10B981")) // зелёный
                    // background можно оставить из XML (bg_history_chip_used),
                    // либо явно задать, если хочешь:
                    // statusChip.setBackgroundResource(R.drawable.bg_history_chip_used)
                }

                "DISCARDED" -> {
                    statusChip.text = "Выброшено"
                    statusChip.setTextColor(Color.parseColor("#EF4444")) // красный
                    // statusChip.setBackgroundResource(R.drawable.bg_history_chip_discarded)
                }

                else -> {
                    // На всякий случай дефолт
                    statusChip.text = ""
                    statusChip.setTextColor(Color.parseColor("#6B7280"))
                }
            }
        }
    }
}
