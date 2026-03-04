package com.example.technicademy.data.model

/** תשובת API Hebcal – רשימת פריטי חג */
data class HolidayResponse(
    val items: List<HolidayItem>
)

data class HolidayItem(
    val title: String,        // שם החג (למשל: ראש השנה)
    val date: String,         // תאריך (למשל: 2025-09-22)
    val memo: String?,        // תיאור נוסף
    val category: String      // כדי לדעת אם זה "חג יהודי"
)
