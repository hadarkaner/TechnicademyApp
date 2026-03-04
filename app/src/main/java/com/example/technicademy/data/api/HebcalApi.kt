package com.example.technicademy.data.api

import com.example.technicademy.data.model.HolidayResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/** ממשק Retrofit ל-API של Hebcal – שליפת חגים בעברית */
interface HebcalApi {
    @GET("hebcal?v=1&maj=on&min=on&mod=on&nx=on&year=now&month=x&ss=on&mf=on&c=on&geo=zip&zip=90100&m=50&s=on")
    fun getHolidays(
        @Query("cfg") config: String = "json",
        @Query("lg") language: String = "h"
    ): Call<HolidayResponse>
}
