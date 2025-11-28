package com.mtaa.app.data

import com.mtaa.app.MtaaSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.Serializable

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

    // Helper: Get the user's business ID
    private suspend fun getMyBusinessId(userId: String): String? {
        val result = MtaaSupabase.client.from("businesses")
            .select {
                filter {
                    eq("owner_id", userId)
                }
            }
            .decodeSingleOrNull<Business>() // Business class is in SellerRepository.kt (Same package)
        return result?.id
    }

    // 1. Fetch products for the SELLER Dashboard (Only their own)
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

    // 2. Fetch ALL products for the BUYER Feed (Public)
    suspend fun getAllProducts(): List<Product> {
        return try {
            MtaaSupabase.client.from("products")
                .select {
                    // Sort by newest first
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Product>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 3. Create a new product (Seller)
    suspend fun createProduct(name: String, desc: String, price: Double, imageBytes: ByteArray?): Boolean {
        return try {
            val user = MtaaSupabase.client.auth.currentUserOrNull() ?: return false
            val businessId = getMyBusinessId(user.id) ?: return false

            var finalImageUrl: String? = null

            if (imageBytes != null) {
                val fileName = "${System.currentTimeMillis()}_${user.id}.jpg"
                val bucket = MtaaSupabase.client.storage.from("products")
                bucket.upload(fileName, imageBytes) { upsert = true }
                finalImageUrl = bucket.publicUrl(fileName)
            }

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

    // 4. Delete a product
    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            MtaaSupabase.client.from("products").delete {
                filter {
                    eq("id", productId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}