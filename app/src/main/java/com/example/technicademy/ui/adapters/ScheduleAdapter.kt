package com.example.technicademy.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.technicademy.R
import com.example.technicademy.data.model.TrainingSession

/**
 * Adapter ל-RecyclerView של מערכת השעות – כרטיס לשיעור (שעה, קבוצה, צבע לפי יום).
 */
class ScheduleAdapter(private val sessions: List<TrainingSession>) :
    RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sideBar: View = view.findViewById<View>(R.id.sideBar)
        val time: TextView = view.findViewById<TextView>(R.id.trainingTime)
        val className: TextView = view.findViewById<TextView>(R.id.trainingClass)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_card, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val session = sessions[position]
        holder.time.text = session.time
        holder.className.text = session.className
        val colors = listOf("#c5e2f0", "#bdf2bf", "#f2e3b8", "#eccbf2", "#ebbfbc")
        val dayIndex = when (session.day) {
            "ראשון" -> 0
            "שני" -> 1
            "שלישי" -> 2
            "רביעי" -> 3
            "חמישי" -> 4
            else -> 0
        }
        holder.sideBar.setBackgroundColor(Color.parseColor(colors[dayIndex]))
    }

    override fun getItemCount() = sessions.size
}
