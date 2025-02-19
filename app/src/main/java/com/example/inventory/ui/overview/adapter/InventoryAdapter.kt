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
import java.text.NumberFormat
import com.example.inventory.R
import com.example.inventory.data.model.Item

/**
 * InventoryAdapter is responsible for displaying a list of inventory items in a RecyclerView.
 * It supports incrementing, decrementing, and deleting items, and notifies external
 * callbacks about these actions so that data can be updated accordingly.
 *
 * @param onItemClicked     Callback invoked when an item is clicked.
 * @param onQuantityUpdate  Callback for updating the item's quantity.
 * @param onDeleteItem      Callback for deleting an item.
 */
class InventoryAdapter(
    private val onItemClicked: (Item) -> Unit,
    private val onQuantityUpdate: (Item, Int) -> Unit,
    private val onDeleteItem: (Item) -> Unit
) : ListAdapter<Item, InventoryAdapter.InventoryViewHolder>(DIFF_CALLBACK) {

    /**
     * Inflates the item layout and creates a new ViewHolder to manage it.
     *
     * @param parent   The parent ViewGroup in which the new ViewHolder will be added.
     * @param viewType The type of view. (Not used directly here; always 0 by default.)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(itemView)
    }

    /**
     * Binds a specific item from the list to the given ViewHolder.
     *
     * @param holder   The ViewHolder that manages the item layout.
     * @param position The position of the item within the list.
     */
    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        // Set up an item click listener for the entire row
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
    }

    /**
     * ViewHolder class for managing the views of individual inventory items in the RecyclerView.
     * This class handles displaying item details and listening for user interactions.
     */
    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // References to text views for displaying the item's name and quantity
        private val itemName: TextView = itemView.findViewById(R.id.itemName)
        private val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)

        // References to action buttons for modifying quantity or deleting the item
        private val buttonAddQuantity: ImageButton = itemView.findViewById(R.id.buttonAddQuantity)
        private val buttonSubtractQuantity: ImageButton = itemView.findViewById(R.id.buttonSubtractQuantity)
        private val buttonDeleteItem: ImageButton = itemView.findViewById(R.id.buttonDeleteItem)

        /**
         * Binds the provided item to the views, setting up click listeners for quantity updates and deletion.
         *
         * @param item The inventory item to be displayed and managed.
         */
        fun bind(item: Item) {
            // Display item name and quantity with proper formatting
            itemName.text = item.name
            val formatter = NumberFormat.getInstance()
            itemQuantity.text = formatter.format(item.quantity)

            // Increase the item quantity by 1
            buttonAddQuantity.setOnClickListener {
                val newQuantity = item.quantity + 1
                onQuantityUpdate(item, newQuantity)
            }

            // Decrease the item quantity by 1, ensuring it doesn't go below 0
            buttonSubtractQuantity.setOnClickListener {
                if (item.quantity > 0) {
                    val newQuantity = item.quantity - 1
                    onQuantityUpdate(item, newQuantity)
                } else {
                    Toast.makeText(itemView.context, "Quantity cannot be negative", Toast.LENGTH_SHORT).show()
                }
            }

            // Delete the item from the list
            buttonDeleteItem.setOnClickListener {
                onDeleteItem(item)
            }
        }
    }

    companion object {
        /**
         * Defines how the adapter determines when an item changes.
         * Helps to optimize updates in the RecyclerView by comparing old and new lists.
         */
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {
            // Check if items refer to the same record based on their IDs
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

            // Check whether the contents of the items are the same
            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }
}

/** Reference: https://www.geeksforgeeks.org/diffutil-in-recyclerview-in-android/
 * Created by: Bradley Wells*/
