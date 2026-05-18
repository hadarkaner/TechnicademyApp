package com.example.technicademy.service

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File

/**
 * העלאת תמונת פרופיל ל-Firebase Storage וקבלת קישור הורדה.
 * נתיב: profile_images/{userId}.jpg
 */
object ProfileImageStorageService {

    private const val TAG = "ProfileImageStorage"
    private const val FOLDER = "profile_images"

    /** Bucket מה-google-services.json */
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    fun storagePathForUser(userId: String) = "$FOLDER/$userId.jpg"

    fun upload(
        imageFile: File,
        userId: String,
        onSuccess: (downloadUrl: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!imageFile.exists() || imageFile.length() == 0L) {
            onFailure(Exception("קובץ התמונה לא נמצא או ריק"))
            return
        }

        val bytes = try {
            imageFile.readBytes()
        } catch (e: Exception) {
            onFailure(Exception("לא ניתן לקרוא את התמונה: ${e.message}", e))
            return
        }

        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        val ref = storage.reference.child(storagePathForUser(userId))
        Log.d(TAG, "Upload start: ${storagePathForUser(userId)}, size=${bytes.size}")

        ref.putBytes(bytes, metadata)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.d(TAG, "Upload success: $uri")
                        onSuccess(uri.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "downloadUrl failed", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "putBytes failed", e)
                onFailure(e)
            }
    }

    fun fetchDownloadUrl(
        userId: String,
        onSuccess: (downloadUrl: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        storage.reference.child(storagePathForUser(userId))
            .downloadUrl
            .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
            .addOnFailureListener { onFailure(it) }
    }

    fun formatError(e: Exception): String {
        val msg = e.message ?: return "שגיאה לא ידועה"
        return when {
            msg.contains("Object does not exist", ignoreCase = true) ->
                "Firebase Storage לא מוגדר. הפעילי Storage בקונסול"
            msg.contains("403", ignoreCase = true) || msg.contains("Permission", ignoreCase = true) ->
                "אין הרשאה. עדכני את כללי Storage ב-Firebase"
            msg.contains("404", ignoreCase = true) ->
                "Storage לא נמצא. ודאי שהפעלת Storage בפרויקט"
            else -> msg
        }
    }
}
