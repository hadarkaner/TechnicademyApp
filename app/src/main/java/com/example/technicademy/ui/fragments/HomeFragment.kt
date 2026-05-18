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
        showAnnouncements(rvAnnouncements, tvAnnouncementsTitle)
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
            showAnnouncements(
                v.findViewById(R.id.rv_announcements),
                v.findViewById(R.id.tv_announcements_title)
            )
        }
    }

    private fun showAnnouncements(rv: RecyclerView, title: TextView) {
        val list = AnnouncementStorage.getGlobal(requireContext())
        val hasItems = list.isNotEmpty()
        title.isVisible = hasItems
        rv.isVisible = hasItems
        if (hasItems) {
            rv.adapter = AnnouncementAdapter(list, AnnouncementAdapter.Mode.HOME)
        }
    }
}
