package com.example.inventory.ui.overview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.R
import com.example.inventory.data.model.Item

class InventoryAdapter(
    private val onQuantityChanged: (Item, Int) -> Unit
) : ListAdapter<Item, InventoryAdapter.InventoryViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onQuantityChanged)
    }

    class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.itemName)
        private val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)
        private val itemDateAdded: TextView = itemView.findViewById(R.id.itemDateAdded)
        private val buttonAddQuantity: ImageButton = itemView.findViewById(R.id.buttonAddQuantity)
        private val buttonSubtractQuantity: ImageButton = itemView.findViewById(R.id.buttonSubtractQuantity)
        private val buttonDeleteItem: ImageButton = itemView.findViewById(R.id.buttonDeleteItem)

        fun bind(item: Item, onQuantityChanged: (Item, Int) -> Unit) {
            itemName.text = item.name
            itemQuantity.text = item.quantity.toString()
            itemDateAdded.text = item.dateAdded

            buttonAddQuantity.setOnClickListener {
                val newQuantity = item.quantity + 1
                onQuantityChanged(item, newQuantity)
            }

            buttonSubtractQuantity.setOnClickListener {
                val newQuantity = if (item.quantity > 0) item.quantity - 1 else 0
                onQuantityChanged(item, newQuantity)
            }

            buttonDeleteItem.setOnClickListener {
                // For demonstration, you might handle deletion similarly via a callback.
                // E.g. onDelete(item)
            }
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem == newItem
}
