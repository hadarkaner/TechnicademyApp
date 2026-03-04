package com.example.technicademy.data

import com.example.technicademy.data.model.TrainingSession

/**
 * רשימת כל השיעורים – לשימוש במערכת השעות, בהרשמה ובבחירת מנהל (חוג/יום/שעה).
 * כולל קבוע מחיר לחוג (100 ₪).
 */
object ScheduleData {
    /** מחיר לכל חוג בשקלים */
    const val PRICE_PER_COURSE_NIS = 100

    val allSessions = listOf(
        TrainingSession("ראשון", "16:30 - 17:20", "כיתות ה'-ו'", "#9C27B0"),
        TrainingSession("ראשון", "17:20 - 18:10", "כיתות א'-ב'", "#4CAF50"),
        TrainingSession("ראשון", "18:10 - 19:00", "כיתות ג'-ד'", "#FFC107"),
        TrainingSession("ראשון", "19:00 - 19:50", "כיתות ז'-ט'", "#2196F3"),
        TrainingSession("ראשון", "20:00 - 21:30", "קבוצת בוגרים", "#9C27B0"),
        TrainingSession("שני", "16:30 - 17:20", "כיתות ה'-ז'", "#9C27B0"),
        TrainingSession("שני", "18:10 - 19:00", "כיתות ה'-ז'", "#FFC107"),
        TrainingSession("שני", "19:00 - 19:50", "קבוצת ליגה", "#2196F3"),
        TrainingSession("שלישי", "17:10 - 18:00", "קבוצת גנים", "#FF9800"),
        TrainingSession("שלישי", "18:30 - 19:45", "קבוצת ליגה", "#2196F3"),
        TrainingSession("שלישי", "20:00 - 21:30", "קבוצת בוגרים", "#F44336"),
        TrainingSession("רביעי", "16:30 - 17:20", "כיתות ה'-ו'", "#9C27B0"),
        TrainingSession("רביעי", "17:20 - 18:10", "כיתות א'-ב'", "#4CAF50"),
        TrainingSession("רביעי", "18:10 - 19:00", "כיתות ג'-ד'", "#FFC107"),
        TrainingSession("רביעי", "19:00 - 19:50", "כיתות ז'-ט'", "#2196F3"),
        TrainingSession("חמישי", "16:30 - 17:20", "כיתות ה'-ז'", "#FFC107"),
        TrainingSession("חמישי", "18:10 - 19:00", "כיתות ג'-ד'", "#FFC107"),
        TrainingSession("חמישי", "19:00 - 19:50", "קבוצת ליגה", "#2196F3"),
    )
}
