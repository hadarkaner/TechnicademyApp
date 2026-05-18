package com.example.technicademy.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.technicademy.R
import com.example.technicademy.data.repository.FirestoreUserRepository
import com.example.technicademy.service.UserPreferencesServiceImpl
import com.example.technicademy.ui.fragments.ContactFragment
import com.example.technicademy.ui.fragments.HomeFragment
import com.example.technicademy.ui.fragments.LoginFragment
import com.example.technicademy.ui.fragments.ProfileFragment
import com.example.technicademy.ui.fragments.RegisterFragment
import com.example.technicademy.ui.fragments.ScheduleFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity ראשי – מנהל את הניווט התחתון ומחליף בין Fragments.
 * בפתיחת האפליקציה: בודק אם יש משתמש מחובר (Firebase או שמירה מקומית) ומציג מסך התחברות או תוכן ראשי.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetAllCourseRegistrationsOnceIfNeeded()
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        bottomAppBar = findViewById(R.id.bottomAppBar)
        bottomNav = findViewById(R.id.bottom_navigation)
        fragmentContainer = findViewById(R.id.fragment_container)

        // צבעי הניווט התחתון – הטאב הנבחר באפור (לא סגול)
        val navColors = ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)
        bottomNav.itemIconTintList = navColors
        bottomNav.itemTextColor = navColors

        // בפתיחה ראשונה: בדיקה אם יש משתמש מחובר והצגת מסך מתאים
        if (savedInstanceState == null) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val identifier = firebaseUser.email ?: firebaseUser.uid
                UserPreferencesServiceImpl.setCurrentUser(this, identifier)
                FirestoreUserRepository.syncFromFirestore(this, identifier) {
                    showMainContent()
                }
            } else {
                val currentUser = UserPreferencesServiceImpl.getCurrentUserKey(this)
                if (currentUser.isBlank()) {
                    showLoginScreen()
                } else {
                    showMainContent()
                }
            }
        }

        // לחיצה על פריט בניווט התחתון – החלפת Fragment בהתאם
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_schedule -> replaceFragment(ScheduleFragment())
                R.id.nav_purchases -> replaceFragment(RegisterFragment())
                R.id.nav_contact -> replaceFragment(ContactFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
            }
            true
        }
    }

    /** מעבר למסך התחברות – מסתיר את הניווט התחתון ומציג LoginFragment */
    fun showLoginScreen() {
        bottomAppBar.visibility = View.GONE
        setFragmentContainerBottomMargin(0)
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    /** אחרי התחברות/הרשמה – הצגת הניווט והמעבר לאזור האישי (פרופיל) */
    fun showMainContent() {
        bottomAppBar.visibility = View.VISIBLE
        setFragmentContainerBottomMargin(resources.getDimensionPixelSize(R.dimen.bottom_bar_reserve_height))
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        bottomNav.selectedItemId = R.id.nav_profile
        replaceFragment(ProfileFragment())
    }

    /** החלפת ה-Fragment המוצג בתוך fragment_container */
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setFragmentContainerBottomMargin(marginPx: Int) {
        val params = fragmentContainer.layoutParams as CoordinatorLayout.LayoutParams
        params.bottomMargin = marginPx
        fragmentContainer.layoutParams = params
    }

    /** איפוס חד-פעמי של כל הרשמות לחוגים (הרצה הבאה של האפליקציה אחרי עדכון זה). */
    private fun resetAllCourseRegistrationsOnceIfNeeded() {
        val flagPrefs = getSharedPreferences(FLAG_PREFS, Context.MODE_PRIVATE)
        if (flagPrefs.getInt(KEY_COURSES_RESET_VERSION, 0) < COURSES_RESET_VERSION) {
            UserPreferencesServiceImpl.clearAllRegistrations(this)
            flagPrefs.edit { putInt(KEY_COURSES_RESET_VERSION, COURSES_RESET_VERSION) }
        }
    }

    private companion object {
        private const val FLAG_PREFS = "app_flags"
        private const val KEY_COURSES_RESET_VERSION = "courses_reset_version"
        private const val COURSES_RESET_VERSION = 2
    }
}
