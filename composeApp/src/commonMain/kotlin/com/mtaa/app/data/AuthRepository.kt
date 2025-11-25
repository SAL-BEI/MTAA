package com.mtaa.app.data

import com.mtaa.app.MtaaSupabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
// NEW IMPORTS: These are required for database checks (from, count, eq)
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count

class AuthRepository {

    suspend fun signUp(emailInput: String, passwordInput: String): Boolean {
        return try {
            MtaaSupabase.client.auth.signUpWith(Email) {
                email = emailInput
                password = passwordInput
            }
            true // Success
        } catch (e: Exception) {
            println("Sign Up Failed: ${e.message}")
            false
        }
    }

    // Check if the user has a shop
    suspend fun hasBusiness(): Boolean {
        val user = MtaaSupabase.client.auth.currentUserOrNull() ?: return false

        // Count rows in 'businesses' where owner_id == current user
        val count = MtaaSupabase.client.from("businesses").select {
            count(Count.EXACT)
            filter {
                eq("owner_id", user.id)
            }
        }.countOrNull() ?: 0

        return count > 0
    }

    suspend fun signIn(emailInput: String, passwordInput: String): Boolean {
        return try {
            MtaaSupabase.client.auth.signInWith(Email) {
                email = emailInput
                password = passwordInput
            }
            true
        } catch (e: Exception) {
            println("Login Failed: ${e.message}")
            false
        }
    }
}