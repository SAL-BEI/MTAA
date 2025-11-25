package com.mtaa.app.data

import com.mtaa.app.MtaaSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.serialization.Serializable
// Ensure you have the auth import for user ID
import io.github.jan.supabase.auth.auth

@Serializable
data class Product(
    val id: String? = null,
    val name: String,
    val description: String,
    val price: Double,
    val image_url: String? = null,
    val owner_id: String,
    val business_id: String
)

class ProductRepository {

    // Get the user's business ID (Helper function)
    private suspend fun getMyBusinessId(userId: String): String? {
        // This queries the DB to find the business owned by this user
        val result = MtaaSupabase.client.from("businesses")
            .select {
                filter {
                    eq("owner_id", userId)
                }
            }
            .decodeSingleOrNull<Business>()
        return result?.id
    }
    // Fetch products for the logged-in user
    suspend fun getMyProducts(): List<Product> {
        return try {
            val user = MtaaSupabase.client.auth.currentUserOrNull() ?: return emptyList()

            MtaaSupabase.client.from("products").select {
                filter {
                    eq("owner_id", user.id)
                }
            }.decodeList<Product>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun createProduct(name: String, desc: String, price: Double, imageBytes: ByteArray?): Boolean {
        return try {
            val user = MtaaSupabase.client.auth.currentUserOrNull() ?: return false
            val businessId = getMyBusinessId(user.id) ?: return false

            var finalImageUrl: String? = null

            // 1. Upload Image if it exists
            if (imageBytes != null) {
                val fileName = "${System.currentTimeMillis()}_${user.id}.jpg"
                val bucket = MtaaSupabase.client.storage.from("products")
                bucket.upload(fileName, imageBytes) { upsert = true }
                // Get the Public URL so buyers can see it
                finalImageUrl = bucket.publicUrl(fileName)
            }

            // 2. Save Product to DB
            val newProduct = Product(
                name = name,
                description = desc,
                price = price,
                image_url = finalImageUrl,
                owner_id = user.id,
                business_id = businessId
            )

            MtaaSupabase.client.from("products").insert(newProduct)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}