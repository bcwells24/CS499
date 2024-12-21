package com.example.inventory

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.content.SharedPreferences

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "inventoryDB"
        private const val DATABASE_VERSION = 1

        // Table names
        private const val TABLE_INVENTORY = "inventory"
        private const val TABLE_USERS = "users"

        // Inventory table columns
        const val COLUMN_ITEM_ID = "id"
        const val COLUMN_ITEM_NAME = "name"
        const val COLUMN_ITEM_QUANTITY = "quantity"
        const val COLUMN_ITEM_DATE = "date"

        // Users table columns
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "username"
        private const val COLUMN_USER_PASSWORD = "password"

        private const val TAG = "DatabaseHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createInventoryTable = "CREATE TABLE $TABLE_INVENTORY (" +
                "$COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_ITEM_NAME TEXT NOT NULL, " +
                "$COLUMN_ITEM_QUANTITY INTEGER NOT NULL, " +
                "$COLUMN_ITEM_DATE TEXT NOT NULL)"

        val createUsersTable = "CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_NAME TEXT NOT NULL UNIQUE, " +
                "$COLUMN_USER_PASSWORD TEXT NOT NULL)"

        db.execSQL(createInventoryTable)
        db.execSQL(createUsersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INVENTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun addItem(name: String, quantity: Int, dateAdded: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ITEM_NAME, name)
            put(COLUMN_ITEM_QUANTITY, quantity)
            put(COLUMN_ITEM_DATE, dateAdded)
        }

        val result = db.insert(TABLE_INVENTORY, null, values)
        return result != -1L
    }

    fun updateItemQuantity(itemId: Int, newQuantity: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ITEM_QUANTITY, newQuantity)
        }

        val rowsAffected = db.update(TABLE_INVENTORY, values, "$COLUMN_ITEM_ID=?", arrayOf(itemId.toString()))
        if (rowsAffected > 0 && newQuantity == 0) {
            notifyZeroQuantity(itemId)
        }

        return rowsAffected > 0
    }

    private fun notifyZeroQuantity(itemId: Int) {
        val sharedPreferences = context.getSharedPreferences("SMSPrefs", Context.MODE_PRIVATE)
        val smsEnabled = sharedPreferences.getBoolean("sms_permission", false)
        val phoneNumber = sharedPreferences.getString("phone_number", "")

        if (smsEnabled && !phoneNumber.isNullOrEmpty()) {
            val itemName = getItemNameById(itemId)
            itemName?.let {
                SMSManager.sendSMS(phoneNumber, "Item: $it has reached zero quantity.")
            }
        }
    }

    fun getAllItems(): List<Item> {
        val itemList = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.query(TABLE_INVENTORY, null, null, null, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                try {
                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ITEM_ID))
                    val name = it.getString(it.getColumnIndexOrThrow(COLUMN_ITEM_NAME))
                    val quantity = it.getInt(it.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY))
                    val dateAdded = it.getString(it.getColumnIndexOrThrow(COLUMN_ITEM_DATE))
                    itemList.add(Item(id, name, quantity, dateAdded))
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Error fetching data: ${e.message}")
                }
            }
        }
        return itemList
    }

    fun getItemByName(name: String): Item? {
        val db = readableDatabase
        val cursor = db.query(TABLE_INVENTORY, null, "$COLUMN_ITEM_NAME=?", arrayOf(name), null, null, null)
        var item: Item? = null

        cursor?.use {
            if (it.moveToFirst()) {
                item = Item(
                    it.getInt(it.getColumnIndexOrThrow(COLUMN_ITEM_ID)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_ITEM_NAME)),
                    it.getInt(it.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY)),
                    it.getString(it.getColumnIndexOrThrow(COLUMN_ITEM_DATE))
                )
            }
        }
        return item
    }

    fun deleteItem(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_INVENTORY, "$COLUMN_ITEM_ID = ?", arrayOf(id.toString()))
    }

    fun registerUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, username)
            put(COLUMN_USER_PASSWORD, password)
        }

        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun authenticateUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_NAME = ? AND $COLUMN_USER_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))

        val isAuthenticated = cursor.use { it != null && it.moveToFirst() }
        return isAuthenticated
    }

    private fun getItemNameById(itemId: Int): String? {
        val db = readableDatabase
        val cursor = db.query(TABLE_INVENTORY, arrayOf(COLUMN_ITEM_NAME), "$COLUMN_ITEM_ID=?", arrayOf(itemId.toString()), null, null, null)

        return cursor.use {
            if (it != null && it.moveToFirst()) {
                it.getString(0)
            } else null
        }
    }
}
