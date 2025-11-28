package com.mtaa.app.data

import com.mtaa.app.MtaaSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import io.github.jan.supabase.auth.auth // <--- CRITICAL IMPORT
import kotlinx.serialization.Serializable

@Serializable
data class Business(
    val id: String? = null,
    val name: String,
    val kra_pin: String,
    val owner_id: String
)

class SellerRepository {

    suspend fun createBusiness(name: String, kraPin: String): Boolean {
        return try {
            // This requires the 'auth' import above
            val user = MtaaSupabase.client.auth.currentUserOrNull()

            if (user == null) {
                println("Error: User is not logged in")
                return false
            }

            val newBusiness = Business(
                name = name,
                kra_pin = kraPin,
                owner_id = user.id
            )

            MtaaSupabase.client.from("businesses").insert(newBusiness)
            println("Success: Business created for ${user.email}")
            true
        } catch (e: Exception) {
            println("Error creating business: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun uploadCertificate(fileName: String, fileData: ByteArray): Boolean {
        return try {
            val user = MtaaSupabase.client.auth.currentUserOrNull() ?: return false
            val path = "${user.id}/$fileName"
            val bucket = MtaaSupabase.client.storage.from("documents")
            bucket.upload(path, fileData) { upsert = true }
            println("Upload Success: $path")
            true
        } catch (e: Exception) {
            println("Upload Failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}