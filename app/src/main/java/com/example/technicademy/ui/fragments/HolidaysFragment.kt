package com.example.technicademy.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technicademy.R
import com.example.technicademy.data.api.HebcalApi
import com.example.technicademy.data.model.HolidayResponse
import com.example.technicademy.ui.adapters.HolidayAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HolidaysFragment : Fragment(R.layout.fragment_holidays) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HolidaysDebug", "1. onViewCreated התחיל")
        val recyclerView = view.findViewById<RecyclerView>(R.id.holidays_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        Log.d("HolidaysDebug", "2. RecyclerView הוגדר")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.hebcal.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(HebcalApi::class.java)
        Log.d("HolidaysDebug", "3. Retrofit מוכן, מתחיל קריאה לרשת")
        api.getHolidays("json", "h").enqueue(object : Callback<HolidayResponse> {
            override fun onResponse(call: Call<HolidayResponse>, response: Response<HolidayResponse>) {
                Log.d("HolidaysDebug", "4. קיבלנו תשובה מהשרת! קוד: ${response.code()}")
                if (response.isSuccessful) {
                    val items = response.body()?.items ?: emptyList()
                    Log.d("HolidaysDebug", "5. מספר חגים שנמצאו: ${items.size}")
                    if (items.isNotEmpty()) {
                        recyclerView.adapter = HolidayAdapter(items)
                        Log.d("HolidaysDebug", "6. האדפטר עודכן עם נתונים")
                    } else {
                        Log.d("HolidaysDebug", "5א. הרשימה חזרה ריקה מהשרת")
                    }
                } else {
                    Log.e("HolidaysDebug", "שגיאת שרת: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<HolidayResponse>, t: Throwable) {
                Log.e("HolidaysDebug", "6. הכשלון! הודעה: ${t.message}")
            }
        })
    }
}
