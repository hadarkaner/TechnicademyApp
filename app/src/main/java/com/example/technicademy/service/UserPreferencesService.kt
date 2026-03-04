package com.example.technicademy.service

import android.content.Context

/**
 * שירות גישה לנתוני משתמש שמורים (מאחורי SharedPreferences).
 * מפריד בין לוגיקת האפליקציה לבין אחסון מקומי.
 */
interface UserPreferencesService {

    /** מפתח משתמש נוכחי (אימייל או uid) – מ-Firebase או משמירה מקומית */
    fun getCurrentUserKey(context: Context): String

    /** שמירת משתמש נוכחי אחרי התחברות/הרשמה */
    fun setCurrentUser(context: Context, identifier: String)

    /** מחיקת משתמש נוכחי (יציאה) */
    fun clearCurrentUser(context: Context)

    fun getUserName(context: Context, userKey: String): String
    fun setUserName(context: Context, userKey: String, name: String)

    fun getUserCourseKeys(context: Context, userKey: String): String
    fun setUserCourseKeys(context: Context, userKey: String, keys: String)

    fun getUserCoursesDetails(context: Context, userKey: String): String
    fun setUserCoursesDetails(context: Context, userKey: String, details: String)

    fun getProfileImagePath(context: Context, userKey: String): String?
    fun setProfileImagePath(context: Context, userKey: String, path: String)

    /** מוחק את כל נתוני ההרשמות (שם, חוגים, מפתחות) – לכל המשתמשים */
    fun clearAllRegistrations(context: Context)
}
