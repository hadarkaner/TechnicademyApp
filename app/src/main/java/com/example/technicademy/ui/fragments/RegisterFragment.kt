package com.example.technicademy.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.technicademy.R

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etName = view.findViewById<EditText>(R.id.et_name)
        val etAge = view.findViewById<EditText>(R.id.et_age)
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val spinnerClass = view.findViewById<Spinner>(R.id.spinner_class)
        val spinnerDay = view.findViewById<Spinner>(R.id.spinner_day)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val classes = listOf(
            "בחר חוג",
            "קבוצת גנים", "כיתות א'-ב'", "כיתות ג'-ד'", "כיתות ה'-ו'", "כיתות ה'-ז'",
            "כיתות ז'-ט'", "קבוצת ליגה", "קבוצת בוגרים"
        )
        val classToDays = mapOf(
            "קבוצת גנים" to listOf("שלישי"),
            "כיתות א'-ב'" to listOf("ראשון", "רביעי"),
            "כיתות ג'-ד'" to listOf("ראשון", "רביעי", "חמישי"),
            "כיתות ה'-ו'" to listOf("ראשון", "רביעי"),
            "כיתות ה'-ז'" to listOf("שני", "חמישי"),
            "כיתות ז'-ט'" to listOf("ראשון", "רביעי"),
            "קבוצת ליגה" to listOf("שני", "שלישי", "חמישי"),
            "קבוצת בוגרים" to listOf("ראשון", "שלישי")
        )
        val classAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, classes)
        spinnerClass.adapter = classAdapter
        fun updateDaysSpinnerForClass(selectedClass: String?) {
            val days = classToDays[selectedClass] ?: emptyList()
            val daysWithPlaceholder = listOf("בחר יום אימון") + days
            spinnerDay.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, daysWithPlaceholder)
        }
        spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (position == 0) updateDaysSpinnerForClass(null) else updateDaysSpinnerForClass(classes[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { updateDaysSpinnerForClass(null) }
        }
        updateDaysSpinnerForClass(null)
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val age = etAge.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val selectedClassPosition = spinnerClass.selectedItemPosition
            val selectedDayPosition = spinnerDay.selectedItemPosition
            val selectedClass = if (selectedClassPosition > 0) classes[selectedClassPosition] else ""
            val selectedDay = if (selectedDayPosition > 0 && spinnerDay.adapter != null) spinnerDay.adapter!!.getItem(selectedDayPosition).toString() else ""
            if (name.isNotEmpty() && age.isNotEmpty() && phone.isNotEmpty() && selectedClass.isNotEmpty() && selectedDay.isNotEmpty()) {
                val paymentFragment = PaymentFragment().apply {
                    arguments = Bundle().apply {
                        putString("name", name)
                        putString("age", age)
                        putString("phone", phone)
                        putString("class", selectedClass)
                        putString("day", selectedDay)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, paymentFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "נא למלא את כל שדות החובה (*) ולבחור חוג ויום אימון", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
