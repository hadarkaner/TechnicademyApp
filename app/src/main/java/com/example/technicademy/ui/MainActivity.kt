package com.example.technicademy.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.technicademy.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomAppBar = findViewById(R.id.bottomAppBar)
        bottomNav = findViewById(R.id.bottom_navigation)

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
                showMainContent()
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
        bottomAppBar.visibility = android.view.View.GONE
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    /** אחרי התחברות/הרשמה – הצגת הניווט והמעבר לאזור האישי (פרופיל) */
    fun showMainContent() {
        bottomAppBar.visibility = android.view.View.VISIBLE
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
}
