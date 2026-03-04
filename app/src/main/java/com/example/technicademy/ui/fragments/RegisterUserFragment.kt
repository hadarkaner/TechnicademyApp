package com.example.technicademy.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.technicademy.R
import com.example.technicademy.service.UserPreferencesServiceImpl
import com.example.technicademy.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterUserFragment : Fragment(R.layout.fragment_register_user) {

    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val etPasswordConfirm = view.findViewById<EditText>(R.id.et_password_confirm)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val tvGoLogin = view.findViewById<TextView>(R.id.tv_go_login)
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val passwordConfirm = etPasswordConfirm.text.toString()
            when {
                email.isEmpty() -> Toast.makeText(requireContext(), "נא להזין אימייל", Toast.LENGTH_SHORT).show()
                password.length < 6 -> Toast.makeText(requireContext(), "הסיסמה חייבת לכלול לפחות 6 תווים", Toast.LENGTH_SHORT).show()
                password != passwordConfirm -> Toast.makeText(requireContext(), "אימות הסיסמה לא תואם", Toast.LENGTH_SHORT).show()
                else -> {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val user = auth.currentUser ?: return@addOnSuccessListener
                            val identifier = user.email ?: user.uid
                            UserPreferencesServiceImpl.setCurrentUser(requireContext(), identifier)
                            Toast.makeText(requireContext(), "נרשמת בהצלחה", Toast.LENGTH_SHORT).show()
                            (activity as? MainActivity)?.showMainContent()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "הרשמה נכשלה: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
        tvGoLogin.setOnClickListener { parentFragmentManager.popBackStack() }
    }
}
