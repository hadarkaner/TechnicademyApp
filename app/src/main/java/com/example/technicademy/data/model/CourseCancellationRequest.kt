package com.example.technicademy.data.model

/**
 * בקשת ביטול חוג – משתמש שולח, מנהל מאשר לפני הסרת ההרשמה.
 */
data class CourseCancellationRequest(
    val id: String = "",
    val userKey: String = "",
    val userEmail: String = "",
    val userDisplayName: String = "",
    /** מפתח "חוג|יום" */
    val courseKey: String = "",
    val courseDisplay: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Long = 0L
) {
    fun isPending() = status == STATUS_PENDING
    fun isApproved() = status == STATUS_APPROVED

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_APPROVED = "approved"
    }
}
