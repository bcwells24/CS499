package com.example.inventory.ui.overview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.R
import com.example.inventory.data.model.Item

class InventoryAdapter(
    private val onItemClicked: (Item) -> Unit,
    private val onQuantityUpdate: (Item, Int) -> Unit,
    private val onDeleteItem: (Item) -> Unit
) : ListAdapter<Item, InventoryAdapter.InventoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
    }

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.itemName)
        private val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)
        // private val itemDateAdded: TextView = itemView.findViewById(R.id.itemDateAdded)
        private val buttonAddQuantity: ImageButton = itemView.findViewById(R.id.buttonAddQuantity)
        private val buttonSubtractQuantity: ImageButton = itemView.findViewById(R.id.buttonSubtractQuantity)
        private val buttonDeleteItem: ImageButton = itemView.findViewById(R.id.buttonDeleteItem)

        fun bind(item: Item) {
            itemName.text = item.name
            itemQuantity.text = item.quantity.toString()
            // itemDateAdded.text = item.dateAdded

            // Increment quantity
            buttonAddQuantity.setOnClickListener {
                val newQuantity = item.quantity + 1
                onQuantityUpdate(item, newQuantity)
            }

            // Decrement quantity
            buttonSubtractQuantity.setOnClickListener {
                if (item.quantity > 0) {
                    val newQuantity = item.quantity - 1
                    onQuantityUpdate(item, newQuantity)
                } else {
                    Toast.makeText(itemView.context, "Quantity cannot be negative", Toast.LENGTH_SHORT).show()
                }
            }

            // Delete item
            buttonDeleteItem.setOnClickListener {
                onDeleteItem(item)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }
}
