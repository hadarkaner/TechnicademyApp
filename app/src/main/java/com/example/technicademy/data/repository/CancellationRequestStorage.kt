package com.example.technicademy.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.technicademy.data.model.CourseCancellationRequest
import com.example.technicademy.service.UserPreferencesServiceImpl
import org.json.JSONArray
import org.json.JSONObject

/** שמירת בקשות ביטול חוג (JSON ב-SharedPreferences). */
object CancellationRequestStorage {

    private const val PREF_NAME = "cancellation_requests_pref"
    private const val KEY_LIST = "cancellation_requests_json"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getAll(context: Context): List<CourseCancellationRequest> {
        val json = prefs(context).getString(KEY_LIST, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                CourseCancellationRequest(
                    id = obj.optString("id", ""),
                    userKey = obj.optString("userKey", ""),
                    userEmail = obj.optString("userEmail", ""),
                    userDisplayName = obj.optString("userDisplayName", ""),
                    courseKey = obj.optString("courseKey", ""),
                    courseDisplay = obj.optString("courseDisplay", ""),
                    status = obj.optString("status", CourseCancellationRequest.STATUS_PENDING),
                    createdAt = obj.optLong("createdAt", 0L)
                )
            }.sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getPending(context: Context): List<CourseCancellationRequest> =
        getAll(context).filter { it.isPending() }

    fun getByUserEmail(context: Context, userEmail: String): List<CourseCancellationRequest> =
        getAll(context).filter { it.userEmail == userEmail }

    fun hasPendingForCourse(context: Context, userEmail: String, courseKey: String): Boolean =
        getAll(context).any {
            it.userEmail == userEmail && it.courseKey == courseKey && it.isPending()
        }

    fun add(context: Context, request: CourseCancellationRequest) {
        val list = getAll(context).toMutableList()
        val newId = System.currentTimeMillis().toString() + list.size
        list.add(
            0,
            request.copy(
                id = newId,
                createdAt = System.currentTimeMillis(),
                status = CourseCancellationRequest.STATUS_PENDING
            )
        )
        save(context, list)
    }

    /** מנהל מאשר – מסיר את החוג מהמשתמש ומסמן את הבקשה כאושרה. */
    fun approve(context: Context, request: CourseCancellationRequest) {
        UserPreferencesServiceImpl.removeUserCourse(context, request.userKey, request.courseKey)
        val list = getAll(context).map {
            if (it.id == request.id) it.copy(status = CourseCancellationRequest.STATUS_APPROVED) else it
        }
        save(context, list)
    }

    private fun save(context: Context, list: List<CourseCancellationRequest>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("userKey", r.userKey)
                    put("userEmail", r.userEmail)
                    put("userDisplayName", r.userDisplayName)
                    put("courseKey", r.courseKey)
                    put("courseDisplay", r.courseDisplay)
                    put("status", r.status)
                    put("createdAt", r.createdAt)
                }
            )
        }
        prefs(context).edit { putString(KEY_LIST, arr.toString()) }
    }
}
