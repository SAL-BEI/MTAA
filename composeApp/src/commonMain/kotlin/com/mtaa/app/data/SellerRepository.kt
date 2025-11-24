package com.mtaa.app.data

import com.mtaa.app.MtaaSupabase
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
// CHANGE 1: "gotrue" is now "auth" in version 3.0.0
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload

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
            // CHANGE 2: We need the 'auth' import above to make this work
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
    // Function to upload a file (ByteArray) to Supabase Storage
    suspend fun uploadCertificate(fileName: String, fileData: ByteArray): Boolean {
        return try {
            val user = MtaaSupabase.client.auth.currentUserOrNull() ?: return false

            // Create a unique path: user_id/filename
            val path = "${user.id}/$fileName"

            val bucket = MtaaSupabase.client.storage.from("documents")
            bucket.upload(path, fileData) {
                upsert = true // Overwrite if exists
            }

            println("Upload Success: $path")
            true
        } catch (e: Exception) {
            println("Upload Failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}