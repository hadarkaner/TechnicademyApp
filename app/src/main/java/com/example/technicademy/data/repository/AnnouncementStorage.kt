package com.example.technicademy.data.repository

import android.content.Context
import com.example.technicademy.data.model.Announcement
import com.example.technicademy.service.UserPreferencesServiceImpl
import org.json.JSONArray
import org.json.JSONObject

/**
 * שמירת מודעות מקומית במכשיר (SharedPreferences + JSON).
 * פרסום מודעה מופיע מיד אצל כולם בעמוד הבית.
 */
object AnnouncementStorage {

    private const val PREF_NAME = "announcements_pref"
    private const val KEY_LIST = "announcements_json"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getAll(context: Context): List<Announcement> {
        val json = prefs(context).getString(KEY_LIST, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                Announcement(
                    id = obj.optString("id", ""),
                    userId = obj.optString("userId", ""),
                    userEmail = obj.optString("userEmail", ""),
                    userDisplayName = obj.optString("userDisplayName", ""),
                    title = obj.optString("title", ""),
                    body = obj.optString("body", ""),
                    status = obj.optString("status", "approved"),
                    createdAt = obj.optLong("createdAt", 0L),
                    targetCourseKey = obj.optString("targetCourseKey", "").takeIf { it.isNotEmpty() },
                    targetTimeDisplay = obj.optString("targetTimeDisplay", "").takeIf { it.isNotEmpty() }
                )
            }.sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getByUserId(context: Context, userId: String): List<Announcement> =
        getAll(context).filter { it.userId == userId && !it.isTargeted() }

    /** המודעות שפרסם המשתמש לפי אימייל */
    fun getByUserEmail(context: Context, userEmail: String): List<Announcement> =
        getAll(context).filter { it.userEmail == userEmail && !it.isTargeted() }

    /** מודעות לכולם (בלי ממוקד חוג/יום) – לעמוד הבית */
    fun getGlobal(context: Context): List<Announcement> =
        getAll(context).filter { !it.isTargeted() }

    /** מודעות ממוקדות לנרשמים – רק למי ש־user_course_keys (לפי מייל) מכיל את המפתח */
    fun getTargetedForUser(context: Context): List<Announcement> {
        val userKey = UserPreferencesServiceImpl.getCurrentUserKey(context)
        val keysStr = UserPreferencesServiceImpl.getUserCourseKeys(context, userKey)
        val keys = keysStr.split('\n').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        return getAll(context).filter { a -> a.targetCourseKey in keys }
    }

    fun add(context: Context, announcement: Announcement) {
        val list = getAll(context).toMutableList()
        val newId = (System.currentTimeMillis().toString() + list.size)
        val withId = announcement.copy(id = newId, createdAt = System.currentTimeMillis(), status = "approved")
        list.add(0, withId)
        save(context, list)
    }

    /** מוחק את כל המודעות שפורסמו */
    fun clearAll(context: Context) {
        prefs(context).edit().putString(KEY_LIST, "[]").apply()
    }

    private fun save(context: Context, list: List<Announcement>) {
        val arr = JSONArray()
        list.forEach { a ->
            arr.put(JSONObject().apply {
                put("id", a.id)
                put("userId", a.userId)
                put("userEmail", a.userEmail)
                put("userDisplayName", a.userDisplayName)
                put("title", a.title)
                put("body", a.body)
                put("status", a.status)
                put("createdAt", a.createdAt)
                put("targetCourseKey", a.targetCourseKey ?: "")
                put("targetTimeDisplay", a.targetTimeDisplay ?: "")
            })
        }
        prefs(context).edit().putString(KEY_LIST, arr.toString()).apply()
    }
}
