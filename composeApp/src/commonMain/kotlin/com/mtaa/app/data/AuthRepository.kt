package com.mtaa.app.data

import com.mtaa.app.MtaaSupabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String,

    // ✅ MAPS TO THE NEW 'full_name' COLUMN
    @SerialName("full_name")
    val fullName: String,

    // ✅ MAPS TO THE EXISTING 'phone' COLUMN
    val phone: String,

    val role: String = "buyer",

    @SerialName("profile_image_url")
    val profileImageUrl: String? = null
)

class AuthRepository {

    val currentUserId: String?
        get() = MtaaSupabase.client.auth.currentUserOrNull()?.id

    suspend fun saveUserProfile(
        userId: String,
        email: String,
        fullName: String,
        phone: String,
        role: String = "buyer",
        profileImageUrl: String? = null
    ): Boolean {
        return try {
            val profile = UserProfile(
                id = userId,
                email = email,
                fullName = fullName, // Sends to 'full_name'
                phone = phone,       // Sends to 'phone'
                role = role,
                profileImageUrl = profileImageUrl
            )

            // Upsert
            MtaaSupabase.client.from("profiles").upsert(profile) {
                onConflict = "id"
            }
            true
        } catch (e: Exception) {
            println("Save Profile Failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // ... (Keep your existing signUp, signIn, etc. functions) ...

    suspend fun signUp(emailInput: String, passwordInput: String): Triple<Boolean, String?, String?> {
        return try {
            MtaaSupabase.client.auth.signUpWith(Email) {
                email = emailInput
                password = passwordInput
            }
            val session = MtaaSupabase.client.auth.currentSessionOrNull()
            if (session != null) Triple(true, session.user?.id, session.user?.email)
            else Triple(false, null, null)
        } catch (e: Exception) {
            Triple(false, null, null)
        }
    }

    suspend fun signIn(emailInput: String, passwordInput: String): Boolean {
        return try {
            MtaaSupabase.client.auth.signInWith(Email) {
                email = emailInput
                password = passwordInput
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun hasBusiness(): Boolean {
        val user = MtaaSupabase.client.auth.currentUserOrNull() ?: return false
        return try {
            val count = MtaaSupabase.client.from("businesses").select {
                count(Count.EXACT)
                filter { eq("owner_id", user.id) }
            }.countOrNull() ?: 0
            count > 0
        } catch (e: Exception) {
            false
        }
    }
}