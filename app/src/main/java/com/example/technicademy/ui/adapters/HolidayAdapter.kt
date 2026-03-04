package com.example.technicademy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.technicademy.R
import com.example.technicademy.data.model.HolidayItem

/**
 * Adapter ל-RecyclerView של חגים – כרטיס עם כותרת, תאריך ותיאור (מהתשובה של Hebcal).
 */
class HolidayAdapter(private val holidays: List<HolidayItem>) :
    RecyclerView.Adapter<HolidayAdapter.HolidayViewHolder>() {

    class HolidayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.holiday_title)
        val dates: TextView = view.findViewById(R.id.holiday_dates)
        val memo: TextView = view.findViewById(R.id.holiday_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_holiday_card, parent, false)
        return HolidayViewHolder(view)
    }

    override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
        val holiday = holidays[position]
        holder.title.text = holiday.title
        holder.dates.text = holiday.date
        holder.memo.text = holiday.memo ?: ""
    }

    override fun getItemCount() = holidays.size
}
