package com.example.technicademy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technicademy.R
import com.example.technicademy.data.ScheduleData
import com.example.technicademy.ui.adapters.ScheduleAdapter
import com.google.android.material.tabs.TabLayout

/**
 * מערכת שעות – טאבים לפי ימים (ראשון–חמישי), רשימת שיעורים עם צבעים.
 * הנתונים מגיעים מ-ScheduleData.
 */
class ScheduleFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tabLayout = view.findViewById<TabLayout>(R.id.daysTabLayout)
        val recyclerView = view.findViewById<RecyclerView>(R.id.scheduleRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val colors = listOf("#c5e2f0", "#bdf2bf", "#f2e3b8", "#eccbf2", "#ebbfbc")
        val days = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי")
        days.forEach { day -> tabLayout.addTab(tabLayout.newTab().setText(day)) }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { currentTab ->
                    val selectedDay = currentTab.text.toString()
                    val filteredList = ScheduleData.allSessions.filter { it.day == selectedDay }
                    recyclerView.adapter = ScheduleAdapter(filteredList)
                    val shape = GradientDrawable().apply {
                        cornerRadius = 50f
                        setColor(Color.parseColor(colors[currentTab.position % colors.size]))
                    }
                    currentTab.view.background = shape
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { tab?.view?.background = null }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        val firstDayList = ScheduleData.allSessions.filter { it.day == "ראשון" }
        recyclerView.adapter = ScheduleAdapter(firstDayList)
        tabLayout.post {
            val firstTab = tabLayout.getTabAt(0)
            val shape = GradientDrawable().apply {
                cornerRadius = 50f
                setColor(Color.parseColor(colors[0]))
            }
            firstTab?.view?.background = shape
        }
    }
}
