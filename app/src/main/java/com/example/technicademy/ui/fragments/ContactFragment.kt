package com.example.technicademy.ui.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.technicademy.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class ContactFragment : Fragment(R.layout.fragment_contact), OnMapReadyCallback {

    private var mapView: MapView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyContactRowAlignment(view)
        mapView = view.findViewById(R.id.map_view)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    /**
     * עברית (RTL): שורות צמודות להתחלה (ימין).
     * אנגלית (LTR): שורות צמודות לסוף (ימין).
     */
    private fun applyContactRowAlignment(view: View) {
        val isHebrew = Locale.getDefault().language == "he" ||
            resources.configuration.locales[0]?.language == "he"

        val rowGravity = if (isHebrew) {
            Gravity.START or Gravity.CENTER_VERTICAL
        } else {
            Gravity.END or Gravity.CENTER_VERTICAL
        }
        val rowDirection = if (isHebrew) {
            View.LAYOUT_DIRECTION_RTL
        } else {
            View.LAYOUT_DIRECTION_LTR
        }

        listOf(
            R.id.contact_row_phone,
            R.id.contact_row_email,
            R.id.contact_row_address
        ).forEach { id ->
            view.findViewById<LinearLayout>(id)?.apply {
                layoutDirection = rowDirection
                gravity = rowGravity
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(31.896545, 35.016185)
        googleMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("פארק הדגים, מודיעין – אקדמיית TECHNICADEMY")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    override fun onStart() { super.onStart(); mapView?.onStart() }
    override fun onResume() { super.onResume(); mapView?.onResume() }
    override fun onPause() { super.onPause(); mapView?.onPause() }
    override fun onStop() { super.onStop(); mapView?.onStop() }
    override fun onDestroyView() { super.onDestroyView(); mapView?.onDestroy(); mapView = null }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); mapView?.onSaveInstanceState(outState) }
    override fun onLowMemory() { super.onLowMemory(); mapView?.onLowMemory() }
}
