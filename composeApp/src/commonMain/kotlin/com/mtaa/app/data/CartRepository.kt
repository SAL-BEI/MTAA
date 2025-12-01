package com.mtaa.app.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// A singleton object to manage the cart globally
object CartRepository {
    private val settings: Settings = Settings()
    private const val CART_KEY = "user_cart_v1"

    // Load items from local storage
    fun getCartItems(): List<Product> {
        val jsonString = settings.getStringOrNull(CART_KEY) ?: return emptyList()
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Add item and save
    fun addToCart(product: Product) {
        val currentItems = getCartItems().toMutableList()
        // Avoid duplicates (optional logic)
        if (currentItems.none { it.id == product.id }) {
            currentItems.add(product)
            saveCart(currentItems)
        }
    }

    // Remove item and save
    fun removeFromCart(productId: String) {
        val currentItems = getCartItems().toMutableList()
        currentItems.removeAll { it.id == productId }
        saveCart(currentItems)
    }

    // Check if item is in cart (for the UI button)
    fun isInCart(productId: String): Boolean {
        return getCartItems().any { it.id == productId }
    }

    // Helper to save list to settings
    private fun saveCart(items: List<Product>) {
        val jsonString = Json.encodeToString(items)
        settings[CART_KEY] = jsonString
    }

    // Clear cart (after checkout)
    fun clearCart() {
        settings.remove(CART_KEY)
    }
}