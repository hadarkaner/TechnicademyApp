package com.example.technicademy.data.model

/** מודל שיעור בודד – יום, שעות, שם קבוצה וצבע להצגה */
data class TrainingSession(
    val day: String,
    val time: String,
    val className: String,
    val color: String
)
