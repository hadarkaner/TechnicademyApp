package com.example.technicademy.service

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

/**
 * מימוש של UserPreferencesService – גישה ל-SharedPreferences (קובץ "UserData").
 * כל שמירה/קריאה של נתוני משתמש (שם, חוגים, תמונת פרופיל וכו') עוברת דרך השירות.
 */
object UserPreferencesServiceImpl : UserPreferencesService {

    private const val PREF_NAME = "UserData"
    const val KEY_CURRENT_USER = "current_username"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun getCurrentUserKey(context: Context): String {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) return user.email?.takeIf { it.isNotBlank() } ?: user.uid
        return prefs(context).getString(KEY_CURRENT_USER, "") ?: ""
    }

    override fun setCurrentUser(context: Context, identifier: String) {
        prefs(context).edit().putString(KEY_CURRENT_USER, identifier).apply()
    }

    override fun clearCurrentUser(context: Context) {
        prefs(context).edit().remove(KEY_CURRENT_USER).apply()
    }

    override fun getUserName(context: Context, userKey: String): String {
        val p = prefs(context)
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        return p.getString("user_name$suffix", null) ?: p.getString("user_name", "אורח") ?: "אורח"
    }

    override fun setUserName(context: Context, userKey: String, name: String) {
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        prefs(context).edit().putString("user_name$suffix", name).apply()
    }

    override fun getUserCourseKeys(context: Context, userKey: String): String {
        val p = prefs(context)
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        return p.getString("user_course_keys$suffix", null) ?: p.getString("user_course_keys", "") ?: ""
    }

    override fun setUserCourseKeys(context: Context, userKey: String, keys: String) {
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        prefs(context).edit().putString("user_course_keys$suffix", keys).apply()
    }

    override fun getUserCoursesDetails(context: Context, userKey: String): String {
        val p = prefs(context)
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        return p.getString("user_courses_details$suffix", null)
            ?: p.getString("user_courses_details", "טרם נרשמת לחוג") ?: "טרם נרשמת לחוג"
    }

    override fun setUserCoursesDetails(context: Context, userKey: String, details: String) {
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        prefs(context).edit().putString("user_courses_details$suffix", details).apply()
    }

    override fun getProfileImagePath(context: Context, userKey: String): String? {
        if (userKey.isBlank()) return null
        return prefs(context).getString("user_profile_image_$userKey", null)
    }

    override fun setProfileImagePath(context: Context, userKey: String, path: String) {
        if (userKey.isBlank()) return
        prefs(context).edit().putString("user_profile_image_$userKey", path).apply()
    }

    override fun clearAllRegistrations(context: Context) {
        val p = prefs(context)
        val keysToRemove = p.all.keys.filter { key ->
            key == "user_name" || key == "user_courses_details" || key == "user_course_keys" ||
                key.startsWith("user_name_") || key.startsWith("user_courses_details_") || key.startsWith("user_course_keys_")
        }
        p.edit().apply {
            keysToRemove.forEach { remove(it) }
            apply()
        }
    }
}
