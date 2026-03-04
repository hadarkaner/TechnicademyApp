package com.example.technicademy.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technicademy.R
import com.example.technicademy.data.repository.AnnouncementStorage
import com.example.technicademy.ui.adapters.AnnouncementAdapter

/**
 * דף הבית – מודעות כלליות מהאקדמיה וכפתור וואטסאפ.
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvAnnouncements = view.findViewById<RecyclerView>(R.id.rv_announcements)
        val tvAnnouncementsTitle = view.findViewById<TextView>(R.id.tv_announcements_title)
        rvAnnouncements.layoutManager = LinearLayoutManager(requireContext())
        val list = AnnouncementStorage.getGlobal(requireContext())
        if (list.isNotEmpty()) {
            tvAnnouncementsTitle.isVisible = true
            rvAnnouncements.isVisible = true
            rvAnnouncements.adapter = AnnouncementAdapter(list, AnnouncementAdapter.Mode.HOME)
        }
        view.findViewById<Button>(R.id.btn_whatsapp_home)?.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://wa.me/972501234567")
            }
            startActivity(android.content.Intent.createChooser(intent, "וואטסאפ"))
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { v ->
            val rv = v.findViewById<RecyclerView>(R.id.rv_announcements)
            val tv = v.findViewById<TextView>(R.id.tv_announcements_title)
            val list = AnnouncementStorage.getGlobal(requireContext())
            if (list.isNotEmpty()) {
                tv.isVisible = true
                rv.isVisible = true
                rv.adapter = AnnouncementAdapter(list, AnnouncementAdapter.Mode.HOME)
            }
        }
    }
}
