package com.example.inventory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InventoryAdapter(
    private val itemList: MutableList<Item>,
    private val context: Context,
    private val databaseHelper: DatabaseHelper
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    private var isSmsEnabled: Boolean = false

    /**
     * Sets the SMS notification status and notifies the adapter to refresh the view.
     *
     * @param isEnabled True if SMS notifications are enabled; false otherwise
     */
    fun setSmsEnabled(isEnabled: Boolean) {
        this.isSmsEnabled = isEnabled
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemName.text = item.name
        holder.itemQuantity.text = item.quantity.toString()
        holder.itemDateAdded.text = item.dateAdded

        setupQuantityButtons(holder, item)
        setupDeleteButton(holder, position)
    }

    private fun setupQuantityButtons(holder: InventoryViewHolder, item: Item) {
        holder.buttonAddQuantity.setOnClickListener {
            updateQuantity(holder, item, item.quantity + 1)
        }

        holder.buttonSubtractQuantity.setOnClickListener {
            val newQuantity = item.quantity - 1
            if (newQuantity >= 0) {
                updateQuantity(holder, item, newQuantity)
            }
        }
    }

    private fun updateQuantity(holder: InventoryViewHolder, item: Item, newQuantity: Int) {
        item.quantity = newQuantity
        databaseHelper.updateItemQuantity(item.id, newQuantity)
        holder.itemQuantity.text = newQuantity.toString()

        if (newQuantity == 0 && isSmsEnabled && context is OverviewActivity) {
            context.checkAndRequestSmsPermission(item.name, getPhoneNumber())
        }
    }

    private fun setupDeleteButton(holder: InventoryViewHolder, position: Int) {
        holder.buttonDeleteItem.setOnClickListener {
            databaseHelper.deleteItem(itemList[position].id)
            itemList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun getPhoneNumber(): String {
        // Implement this method to retrieve the phone number from SharedPreferences
        return "" // Placeholder return statement
    }

    override fun getItemCount(): Int = itemList.size

    class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)
        val itemDateAdded: TextView = itemView.findViewById(R.id.itemDateAdded)
        val buttonAddQuantity: ImageButton = itemView.findViewById(R.id.buttonAddQuantity)
        val buttonSubtractQuantity: ImageButton = itemView.findViewById(R.id.buttonSubtractQuantity)
        val buttonDeleteItem: ImageButton = itemView.findViewById(R.id.buttonDeleteItem)
    }
}
