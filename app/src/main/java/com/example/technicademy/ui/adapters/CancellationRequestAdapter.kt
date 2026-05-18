package com.example.technicademy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.technicademy.R
import com.example.technicademy.data.model.CourseCancellationRequest

class CancellationRequestAdapter(
    private val items: List<CourseCancellationRequest>,
    private val onApprove: (CourseCancellationRequest) -> Unit
) : RecyclerView.Adapter<CancellationRequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val user: TextView = view.findViewById(R.id.tv_cancel_user)
        val course: TextView = view.findViewById(R.id.tv_cancel_course)
        val email: TextView = view.findViewById(R.id.tv_cancel_email)
        val btnApprove: Button = view.findViewById(R.id.btn_approve_cancellation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cancellation_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.user.text = item.userDisplayName.ifBlank { item.userEmail }
        holder.course.text = item.courseDisplay
        holder.email.text = item.userEmail
        holder.btnApprove.setOnClickListener { onApprove(item) }
    }

    override fun getItemCount() = items.size
}
