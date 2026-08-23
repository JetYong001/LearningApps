package com.example.project.data

import com.example.project.model.PlannerItem
import com.example.project.model.TaskItem
import com.example.project.model.UserProfile
import com.example.project.model.StudySession
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProgressRepository {

    suspend fun fetchUserProfile(): UserProfile? {
        return withContext(Dispatchers.IO) {
            try {
                supabase.postgrest
                    .from("profiles")
                    .select()
                    .decodeSingleOrNull<UserProfile>()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun fetchOverallProgress(): Float {
        return withContext(Dispatchers.IO) {
            try {
                val items = supabase.postgrest
                    .from("planner_items")
                    .select()
                    .decodeList<PlannerItem>()

                if (items.isEmpty()) return@withContext 0f

                val completedCount = items.count { it.status.equals("Completed", ignoreCase = true) }
                completedCount.toFloat() / items.size.toFloat()
            } catch (e: Exception) {
                e.printStackTrace()
                0f
            }
        }
    }

    suspend fun fetchStudySessions(): List<StudySession> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.postgrest
                    .from("study_sessions")
                    .select()
                    .decodeList<StudySession>()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}