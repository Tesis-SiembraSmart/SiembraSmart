package com.example.siembrasmart.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.MainActivity
import com.example.siembrasmart.controllers.UserProfileController
import com.example.siembrasmart.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var userId: String
    private lateinit var controller: UserProfileController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        controller = UserProfileController(this)

        // Cargar perfil
        loadUserProfile()

        binding.logoutButton.setOnClickListener {
            cerrarSesion()
        }

        binding.backbutton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.saveChangesButton.setOnClickListener {
            val updatedFirstName = binding.firstNameTextView.text.toString().trim()
            val updatedLastName = binding.lastNameTextView.text.toString().trim()
            val updatedPhoneNumber = binding.phoneNumberTextView.text.toString().trim()

            controller.saveUserProfile(
                userId,
                updatedFirstName,
                updatedLastName,
                updatedPhoneNumber
            ) { success ->
                if (success) {
                    loadUserProfile()
                }
            }
        }
    }

    private fun loadUserProfile() {
        controller.loadUserProfile(userId) { user ->
            if (user != null) {
                binding.firstNameTextView.setText(user.firstName)
                binding.lastNameTextView.setText(user.lastName)
                binding.emailTextView.text = user.email
                binding.phoneNumberTextView.setText(user.phoneNumber)
            } else {
                // Error al cargar el perfil
                Toast.makeText(this, "Error al cargar el perfil.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cerrarSesion() {
        controller.signOut()
    }
}
