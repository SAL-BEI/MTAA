package com.mtaa.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

object MtaaSupabase {

    // Make sure your actual URL and Key are pasted here
    private const val SUPABASE_URL = "jqgkcvowfmezfcusgtsz.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_hxV3BBG0uXMlLPMrij3Wpw_aCbeT8mv"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth) // This should turn from red to normal color now
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}