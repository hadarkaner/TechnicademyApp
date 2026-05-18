package com.example.technicademy.data.repository

import android.content.Context
import com.example.technicademy.data.firestore.FirestorePaths
import com.example.technicademy.service.UserPreferencesServiceImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * שמירת נתוני משתמש ב-Firestore (users/{uid}).
 * בנוסף מסנכרן ל-SharedPreferences לקריאה מהירה במסכים.
 */
object FirestoreUserRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun userDoc() = FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
        db.collection(FirestorePaths.USERS).document(uid)
    }

    fun syncFromFirestore(
        context: Context,
        userKey: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val doc = userDoc()
        if (doc == null || userKey.isBlank()) {
            onComplete(false)
            return
        }
        doc.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onComplete(false)
                    return@addOnSuccessListener
                }
                snapshot.getString("userName")?.let {
                    UserPreferencesServiceImpl.setUserName(context, userKey, it)
                }
                snapshot.getString("courseKeys")?.let {
                    UserPreferencesServiceImpl.setUserCourseKeys(context, userKey, it)
                }
                snapshot.getString("coursesDetails")?.let {
                    UserPreferencesServiceImpl.setUserCoursesDetails(context, userKey, it)
                }
                snapshot.getString("profileImageUrl")?.let {
                    UserPreferencesServiceImpl.setProfileImagePath(context, userKey, it)
                }
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveUserProfile(
        userKey: String,
        userName: String? = null,
        courseKeys: String? = null,
        coursesDetails: String? = null,
        profileImageUrl: String? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        val doc = userDoc() ?: return
        val data = mutableMapOf<String, Any>(
            "userKey" to userKey,
            "email" to (FirebaseAuth.getInstance().currentUser?.email ?: userKey),
            "updatedAt" to System.currentTimeMillis()
        )
        userName?.let { data["userName"] = it }
        courseKeys?.let { data["courseKeys"] = it }
        coursesDetails?.let { data["coursesDetails"] = it }
        profileImageUrl?.let { data["profileImageUrl"] = it }
        doc.set(data, SetOptions.merge())
            .addOnFailureListener { onFailure?.invoke(it) }
    }
}
