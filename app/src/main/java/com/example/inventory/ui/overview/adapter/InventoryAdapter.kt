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

/**
 * InventoryAdapter handles displaying a list of inventory items in a RecyclerView.
 * It supports user interactions such as updating item quantities and deleting items.
 *
 * @param onItemClicked Callback for when an item is clicked.
 * @param onQuantityUpdate Callback for updating the item's quantity.
 * @param onDeleteItem Callback for deleting an item.
 */
class InventoryAdapter(
    private val onItemClicked: (Item) -> Unit,
    private val onQuantityUpdate: (Item, Int) -> Unit,
    private val onDeleteItem: (Item) -> Unit
) : ListAdapter<Item, InventoryAdapter.InventoryViewHolder>(DIFF_CALLBACK) {

    /**
     * Called to create a new ViewHolder when there are no existing ViewHolders available for reuse.
     *
     * @param parent The parent ViewGroup in which the new ViewHolder will be created.
     * @param viewType The type of the new ViewHolder.
     * @return A new instance of InventoryViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(itemView)
    }

    /**
     * Called to bind data to a ViewHolder at a specific position.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the adapter.
     */
    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = getItem(position) // Retrieve the item at the current position
        holder.bind(item) // Bind the item data to the ViewHolder
        holder.itemView.setOnClickListener {
            onItemClicked(item) // Invoke the item click callback
        }
    }

    /**
     * ViewHolder class for managing the views of individual inventory items.
     *
     * @param itemView The root view of the item layout.
     */
    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // View references for item name and quantity
        private val itemName: TextView = itemView.findViewById(R.id.itemName)
        private val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)

        // View references for action buttons
        private val buttonAddQuantity: ImageButton = itemView.findViewById(R.id.buttonAddQuantity)
        private val buttonSubtractQuantity: ImageButton = itemView.findViewById(R.id.buttonSubtractQuantity)
        private val buttonDeleteItem: ImageButton = itemView.findViewById(R.id.buttonDeleteItem)

        /**
         * Binds the data of an inventory item to the views and sets up user interaction callbacks.
         *
         * @param item The inventory item to bind.
         */
        fun bind(item: Item) {
            // Display item name and quantity
            itemName.text = item.name
            itemQuantity.text = item.quantity.toString()

            // Set up click listener to increment the item's quantity
            buttonAddQuantity.setOnClickListener {
                val newQuantity = item.quantity + 1
                onQuantityUpdate(item, newQuantity) // Notify the callback with the updated quantity
            }

            // Set up click listener to decrement the item's quantity
            buttonSubtractQuantity.setOnClickListener {
                if (item.quantity > 0) {
                    val newQuantity = item.quantity - 1
                    onQuantityUpdate(item, newQuantity) // Notify the callback with the updated quantity
                } else {
                    // Display a toast if the quantity cannot be negative
                    Toast.makeText(itemView.context, "Quantity cannot be negative", Toast.LENGTH_SHORT).show()
                }
            }

            // Set up click listener to delete the item
            buttonDeleteItem.setOnClickListener {
                onDeleteItem(item) // Notify the callback to delete the item
            }
        }
    }

    companion object {
        /**
         * DiffUtil callback for calculating changes between old and new item lists.
         */
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            /**
             * Determines if two items represent the same entity based on their IDs.
             *
             * @param oldItem The old item.
             * @param newItem The new item.
             * @return true if the items have the same ID, false otherwise.
             */
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

            /**
             * Determines if the contents of two items are the same.
             *
             * @param oldItem The old item.
             * @param newItem The new item.
             * @return true if the contents are identical, false otherwise.
             */
            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }
}

/** Reference: https://www.geeksforgeeks.org/diffutil-in-recyclerview-in-android/ */