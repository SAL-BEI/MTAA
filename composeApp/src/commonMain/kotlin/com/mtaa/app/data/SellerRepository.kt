package com.mtaa.app.data

import com.mtaa.app.MtaaSupabase
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
// CHANGE 1: "gotrue" is now "auth" in version 3.0.0
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo

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
}