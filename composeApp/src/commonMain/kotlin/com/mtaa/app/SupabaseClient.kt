package com.mtaa.app

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer // Import required for JSON fix
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MtaaSupabase {

    private const val SUPABASE_URL = "https://jqgkcvowfmezfcusgtsz.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_hxV3BBG0uXMlLPMrij3Wpw_aCbeT8mv"

    lateinit var client: io.github.jan.supabase.SupabaseClient

    fun init(settings: Settings) {
        client = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "app"
                host = "supabase.com"

                sessionManager = object : SessionManager {
                    override suspend fun saveSession(session: UserSession) {
                        val sessionString = Json.encodeToString(session)
                        settings["supabase_session"] = sessionString
                    }

                    override suspend fun loadSession(): UserSession? {
                        val sessionString = settings.getStringOrNull("supabase_session") ?: return null
                        return try {
                            Json.decodeFromString(sessionString)
                        } catch(e: Exception) {
                            null
                        }
                    }

                    override suspend fun deleteSession() {
                        settings.remove("supabase_session")
                    }
                }
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)

            // FIXED: Explicitly configure the Serializer for Functions to handle JSON bodies
            install(Functions) {
                serializer = KotlinXSerializer(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }
    }
}