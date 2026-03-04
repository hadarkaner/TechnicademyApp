package com.example.technicademy.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.technicademy.R
import com.example.technicademy.data.ScheduleData
import com.example.technicademy.service.UserPreferencesServiceImpl

class PaymentFragment : Fragment(R.layout.fragment_payment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = arguments?.getString("name")
        val selectedClass = arguments?.getString("class")
        val selectedDay = arguments?.getString("day")
        val tvSummary = view.findViewById<android.widget.TextView>(R.id.tv_payment_summary)
        val tvAmount = view.findViewById<android.widget.TextView>(R.id.tv_payment_amount)
        tvSummary.text = buildString {
            if (!selectedClass.isNullOrBlank()) append("חוג: $selectedClass\n")
            if (!selectedDay.isNullOrBlank()) append("יום: $selectedDay")
        }.trim().ifBlank { "" }
        tvAmount.text = "סה\"כ לתשלום: ${ScheduleData.PRICE_PER_COURSE_NIS} ₪"

        val btnFinish = view.findViewById<Button>(R.id.btn_complete_registration)
        btnFinish.setOnClickListener {
            val userKey = LoginFragment.getCurrentUserKey(requireContext())
            val prefs = UserPreferencesServiceImpl
            if (!name.isNullOrBlank()) prefs.setUserName(requireContext(), userKey, name)
            if (!selectedClass.isNullOrBlank() && !selectedDay.isNullOrBlank()) {
                val timesForSelection = ScheduleData.allSessions
                    .filter { it.day == selectedDay && it.className == selectedClass }
                    .map { it.time }
                val timesText = timesForSelection.joinToString(", ")
                val key = "$selectedClass|$selectedDay"
                val existingKeys = prefs.getUserCourseKeys(requireContext(), userKey)
                val keysList = existingKeys.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
                if (!keysList.contains(key)) {
                    prefs.setUserCourseKeys(requireContext(), userKey, (keysList + key).joinToString("\n"))
                    val existingDetails = prefs.getUserCoursesDetails(requireContext(), userKey)
                    val newEntry = buildString {
                        append("חוג: $selectedClass\n")
                        if (timesText.isNotBlank()) append("שעות: $timesText\n")
                        append("יום: $selectedDay")
                    }
                    val newDetails = if (existingDetails.isBlank()) newEntry else existingDetails + "\n\n" + newEntry
                    prefs.setUserCoursesDetails(requireContext(), userKey, newDetails)
                }
            }
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_success)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.85).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.findViewById<Button>(R.id.btn_go_to_profile).setOnClickListener {
                dialog.dismiss()
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
            }
            dialog.setCancelable(false)
            dialog.show()
        }
    }
}
