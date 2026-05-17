package com.example.technicademy.service

import android.content.Context
import androidx.core.content.edit
import com.example.technicademy.data.ScheduleData
import com.google.firebase.auth.FirebaseAuth

/**
 * מימוש של UserPreferencesService – גישה ל-SharedPreferences (קובץ "UserData").
 * כל שמירה/קריאה של נתוני משתמש (שם, חוגים, תמונת פרופיל וכו') עוברת דרך השירות.
 */
object UserPreferencesServiceImpl : UserPreferencesService {

    private const val PREF_NAME = "UserData"
    const val KEY_CURRENT_USER = "current_username"
    const val NO_COURSE_REGISTERED = "טרם נרשמת לחוג"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun getCurrentUserKey(context: Context): String {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) return user.email?.takeIf { it.isNotBlank() } ?: user.uid
        return prefs(context).getString(KEY_CURRENT_USER, "") ?: ""
    }

    override fun setCurrentUser(context: Context, identifier: String) {
        prefs(context).edit { putString(KEY_CURRENT_USER, identifier) }
    }

    override fun clearCurrentUser(context: Context) {
        prefs(context).edit { remove(KEY_CURRENT_USER) }
    }

    override fun getUserName(context: Context, userKey: String): String {
        val p = prefs(context)
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        return p.getString("user_name$suffix", null) ?: p.getString("user_name", "אורח") ?: "אורח"
    }

    override fun setUserName(context: Context, userKey: String, name: String) {
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        prefs(context).edit { putString("user_name$suffix", name) }
    }

    override fun getUserCourseKeys(context: Context, userKey: String): String {
        val p = prefs(context)
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        val perUser = p.getString("user_course_keys$suffix", null)
        if (userKey.isNotBlank()) return perUser.orEmpty()
        return perUser ?: p.getString("user_course_keys", "").orEmpty()
    }

    override fun setUserCourseKeys(context: Context, userKey: String, keys: String) {
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        prefs(context).edit { putString("user_course_keys$suffix", keys) }
    }

    override fun getUserCoursesDetails(context: Context, userKey: String): String {
        val stored = cleanStoredCoursesDetails(getStoredCoursesDetails(context, userKey))
        if (stored.isNotBlank()) return stored

        val keys = getUserCourseKeys(context, userKey)
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (keys.isNotEmpty()) return buildDetailsFromKeys(keys)

        return NO_COURSE_REGISTERED
    }

    /** פרטי חוגים כפי שנשמרו בלבד (בלי בנייה מחדש ממפתחות). */
    fun getStoredCoursesDetailsOnly(context: Context, userKey: String): String =
        cleanStoredCoursesDetails(getStoredCoursesDetails(context, userKey))

    private fun getStoredCoursesDetails(context: Context, userKey: String): String {
        val p = prefs(context)
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        val perUser = p.getString("user_courses_details$suffix", null)
        if (userKey.isNotBlank()) return perUser.orEmpty()
        return perUser ?: p.getString("user_courses_details", null).orEmpty()
    }

    private fun cleanStoredCoursesDetails(stored: String): String {
        if (stored.isBlank() || stored == NO_COURSE_REGISTERED) return ""
        val prefix = "$NO_COURSE_REGISTERED\n\n"
        return if (stored.startsWith(prefix)) stored.removePrefix(prefix) else stored
    }

    private fun buildDetailsFromKeys(keys: List<String>): String =
        keys.joinToString("\n\n") { key ->
            val parts = key.split("|", limit = 2)
            if (parts.size != 2) return@joinToString key
            val className = parts[0]
            val day = parts[1]
            val times = ScheduleData.allSessions
                .filter { it.className == className && it.day == day }
                .map { it.time }
                .distinct()
                .joinToString(", ")
            buildString {
                append("חוג: $className\n")
                if (times.isNotBlank()) append("שעות: $times\n")
                append("יום: $day")
            }
        }

    override fun setUserCoursesDetails(context: Context, userKey: String, details: String) {
        val suffix = if (userKey.isNotBlank()) "_$userKey" else ""
        prefs(context).edit { putString("user_courses_details$suffix", details) }
    }

    override fun getProfileImagePath(context: Context, userKey: String): String? {
        if (userKey.isBlank()) return null
        return prefs(context).getString("user_profile_image_$userKey", null)
    }

    override fun setProfileImagePath(context: Context, userKey: String, path: String) {
        if (userKey.isBlank()) return
        prefs(context).edit { putString("user_profile_image_$userKey", path) }
    }

    override fun clearAllRegistrations(context: Context) {
        val p = prefs(context)
        val keysToRemove = p.all.keys.filter { key -> isRegistrationPreferenceKey(key) }
        p.edit {
            keysToRemove.forEach { remove(it) }
        }
    }

    /** מסיר חוג בודד מהרשמת המשתמש (לאחר אישור מנהל לביטול). */
    fun removeUserCourse(context: Context, userKey: String, courseKey: String) {
        val keysList = getUserCourseKeys(context, userKey)
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()
        keysList.remove(courseKey)
        setUserCourseKeys(context, userKey, keysList.joinToString("\n"))

        val stored = cleanStoredCoursesDetails(getStoredCoursesDetails(context, userKey))
        if (stored.isNotBlank()) {
            val remaining = stored.split("\n\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() && !courseBlockMatchesKey(it, courseKey) }
            setUserCoursesDetails(
                context,
                userKey,
                if (remaining.isEmpty()) "" else remaining.joinToString("\n\n")
            )
        } else if (keysList.isEmpty()) {
            setUserCoursesDetails(context, userKey, "")
        }
    }

    fun getUserCourseOptions(context: Context, userKey: String): List<Pair<String, String>> =
        getUserCourseKeys(context, userKey)
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { key ->
                val parts = key.split("|", limit = 2)
                val label = if (parts.size == 2) "${parts[0]} – יום ${parts[1]}" else key
                key to label
            }

    private fun courseBlockMatchesKey(block: String, courseKey: String): Boolean {
        val parts = courseKey.split("|", limit = 2)
        if (parts.size != 2) return block.contains(courseKey)
        return block.contains("חוג: ${parts[0]}") && block.contains("יום: ${parts[1]}")
    }

    /** מוחק חוגים של משתמש בודד (אחרי איפוס מלא או לפני הרשמה מחדש). */
    fun clearUserCourses(context: Context, userKey: String) {
        if (userKey.isBlank()) {
            prefs(context).edit {
                remove("user_course_keys")
                remove("user_courses_details")
                putString("user_course_keys", "")
                putString("user_courses_details", "")
            }
            return
        }
        prefs(context).edit {
            remove("user_course_keys_$userKey")
            remove("user_courses_details_$userKey")
            putString("user_course_keys_$userKey", "")
            putString("user_courses_details_$userKey", "")
        }
    }

    private fun isRegistrationPreferenceKey(key: String): Boolean =
        key == "user_name" ||
            key == "user_courses_details" ||
            key == "user_course_keys" ||
            key.startsWith("user_name_") ||
            key.startsWith("user_courses_details_") ||
            key.startsWith("user_course_keys_")
}
