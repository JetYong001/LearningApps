package com.example.project.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

val supabase = createSupabaseClient(
    supabaseUrl = "https://lkzyelnbnsntaghlvfsr.supabase.co",
    supabaseKey = "sb_publishable_jb5r5faPKmJUeI3ZBbizwQ_NyZM2RHc"
) {
    defaultSerializer = KotlinXSerializer(Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    })
    install(Postgrest)
    install(Auth)
}