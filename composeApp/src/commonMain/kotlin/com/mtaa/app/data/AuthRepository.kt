package com.mtaa.app.data

import com.mtaa.app.MtaaSupabase
// CRITICAL IMPORT: This allows you to use .auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {

    suspend fun signUp(emailInput: String, passwordInput: String): Boolean {
        return try {
            // Now .auth will work, and it will understand 'email' and 'password'
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