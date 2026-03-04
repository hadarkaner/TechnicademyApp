package com.example.technicademy.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.technicademy.R
import com.example.technicademy.service.UserPreferencesServiceImpl
import com.example.technicademy.ui.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * מסך התחברות – אימייל+סיסמה או Google Sign-In.
 * אחרי התחברות מוצג MainContent (ניווט תחתון + פרופיל).
 */
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val auth = FirebaseAuth.getInstance()

    // Launcher להתחברות עם Google – מקבל תוצאה ומעדכן Firebase
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { saveUserAndGoMain() }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "התחברות נכשלה: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } ?: run {
                    Toast.makeText(requireContext(), "שגיאה בקבלת פרטי חשבון", Toast.LENGTH_SHORT).show()
                }
            } catch (_: ApiException) {
                Toast.makeText(requireContext(), "התחברות עם Google נכשלה", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // חיבור רכיבי המסך
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val btnLogin = view.findViewById<Button>(R.id.btn_login)
        val btnGoogleLogin = view.findViewById<Button>(R.id.btn_google_login)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tv_forgot_password)
        val tvGoRegister = view.findViewById<TextView>(R.id.tv_go_register)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "נא למלא אימייל וסיסמה", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { saveUserAndGoMain() }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "התחברות נכשלה: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        btnGoogleLogin.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
        tvForgotPassword.setOnClickListener { showForgotPasswordDialog(etEmail.text.toString().trim()) }
        tvGoRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterUserFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /** דיאלוג שחזור סיסמה – שדה אימייל ושליחה ל-Firebase */
    private fun showForgotPasswordDialog(prefillEmail: String) {
        val input = EditText(requireContext()).apply {
            hint = "הכנסי אימייל"
            setText(prefillEmail)
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(48.dpToPx(), 32.dpToPx(), 48.dpToPx(), 32.dpToPx())
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dpToPx(), 8.dpToPx(), 24.dpToPx(), 8.dpToPx())
            addView(input)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("שחזור סיסמה")
            .setView(container)
            .setPositiveButton("שלח") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(requireContext(), "נא להזין אימייל", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener { Toast.makeText(requireContext(), "נשלח קישור לאימייל", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { e -> Toast.makeText(requireContext(), "שליחה נכשלה: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    /** שמירת מפתח המשתמש ב-Service ומעבר למסך הראשי */
    private fun saveUserAndGoMain() {
        val user = auth.currentUser ?: return
        val identifier = user.email ?: user.uid
        UserPreferencesServiceImpl.setCurrentUser(requireContext(), identifier)
        (activity as? MainActivity)?.showMainContent()
    }

    @Suppress("unused")
    companion object {
        const val KEY_CURRENT_USER = "current_username"
        /** מפתח המשתמש הנוכחי – לשימוש בשאר האפליקציה (פרופיל, הרשמה וכו') */
        fun getCurrentUserKey(context: Context): String = UserPreferencesServiceImpl.getCurrentUserKey(context)
        /** איפוס כל נתוני ההרשמות – למנהל */
        fun clearAllRegistrations(context: Context) = UserPreferencesServiceImpl.clearAllRegistrations(context)
    }
}
