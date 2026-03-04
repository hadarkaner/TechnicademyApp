package com.example.technicademy.data.model

/**
 * מודל מודעה – לשימוש בעמוד הבית, בפרופיל ובמנהל.
 * תומך במודעות כלליות ובמודעות ממוקדות (לחוג+יום).
 */
data class Announcement(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userDisplayName: String = "",
    val title: String = "",
    val body: String = "",
    val status: String = "approved",
    val createdAt: Long = 0L,
    /** מפתח "חוג|יום" – מודעה ממוקדת לנרשמים לחוג+יום הזה */
    val targetCourseKey: String? = null,
    /** שעת השיעור להצגה (למנהל) */
    val targetTimeDisplay: String? = null
) {
    @Suppress("unused")
    fun isPending() = status == "pending"
    @Suppress("unused")
    fun isApproved() = status == "approved"
    fun isTargeted() = !targetCourseKey.isNullOrBlank()
}
