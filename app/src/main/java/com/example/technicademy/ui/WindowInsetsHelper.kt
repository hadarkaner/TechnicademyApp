package com.example.technicademy.ui

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.technicademy.R

/**
 * ריווח לפי סרגל המערכת וחור המצלמה – למסכים ולניווט התחתון.
 */
object WindowInsetsHelper {

    fun applyMainScreenInsets(
        fragmentContainer: View,
        bottomBar: View,
        onBottomBarHeightChanged: (Int) -> Unit
    ) {
        val gapTopPx = fragmentContainer.resources.getDimensionPixelSize(R.dimen.status_bar_content_gap)
        val fallbackTopPx = fragmentContainer.resources.getDimensionPixelSize(R.dimen.status_bar_fallback_top)
        val barParams = bottomBar.layoutParams as CoordinatorLayout.LayoutParams

        val updateBottomReserve = {
            val reserve = if (bottomBar.visibility == View.VISIBLE) {
                bottomBar.height + barParams.bottomMargin
            } else {
                0
            }
            onBottomBarHeightChanged(reserve)
        }

        bottomBar.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateBottomReserve()
        }

        ViewCompat.setOnApplyWindowInsetsListener(bottomBar) { bar, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val safeTop = maxOf(systemBars.top, cutout.top)
            val topPadding = (if (safeTop > 0) safeTop else fallbackTopPx) + gapTopPx
            fragmentContainer.updatePadding(top = topPadding)

            // הבר צמוד מעל סרגל הניווט של המכשיר (בלי רווח פנימי)
            barParams.bottomMargin = systemBars.bottom
            bar.layoutParams = barParams
            bar.updatePadding(bottom = 0)

            bar.post { updateBottomReserve() }
            insets
        }
        ViewCompat.requestApplyInsets(bottomBar)
    }
}
