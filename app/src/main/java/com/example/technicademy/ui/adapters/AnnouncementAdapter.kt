package com.example.technicademy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.technicademy.R
import com.example.technicademy.data.model.Announcement

/**
 * Adapter לרשימת מודעות – תומך במצבים: בית (HOME), הרשימה שלי (MY_LIST), מנהל (PENDING_MANAGER).
 */
class AnnouncementAdapter(
    private val items: List<Announcement>,
    private val mode: Mode,
    private val onApprove: ((Announcement) -> Unit)? = null
) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    enum class Mode { HOME, MY_LIST, PENDING_MANAGER }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_announcement_title)
        val target: TextView = view.findViewById(R.id.tv_announcement_target)
        val body: TextView = view.findViewById(R.id.tv_announcement_body)
        val status: TextView = view.findViewById(R.id.tv_announcement_status)
        val btnApprove: Button = view.findViewById(R.id.btn_approve)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_announcement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val a = items[position]
        holder.title.text = a.title
        holder.body.text = a.body
        if (a.isTargeted()) {
            val parts = a.targetCourseKey!!.split("|")
            val targetText = buildString {
                if (parts.size >= 2) append("לשיעור ${parts[0]} ביום ${parts[1]}")
                if (!a.targetTimeDisplay.isNullOrBlank()) append(" • ${a.targetTimeDisplay}")
            }
            holder.target.text = targetText
            holder.target.visibility = View.VISIBLE
        } else {
            holder.target.visibility = View.GONE
        }
        when (mode) {
            Mode.HOME -> {
                holder.status.visibility = View.GONE
                holder.btnApprove.visibility = View.GONE
            }
            Mode.MY_LIST -> {
                holder.status.visibility = View.VISIBLE
                holder.status.text = "פורסמה"
                holder.btnApprove.visibility = View.GONE
            }
            Mode.PENDING_MANAGER -> {
                holder.status.visibility = View.GONE
                holder.btnApprove.visibility = View.VISIBLE
                holder.btnApprove.setOnClickListener { onApprove?.invoke(a) }
            }
        }
    }

    override fun getItemCount() = items.size
}
