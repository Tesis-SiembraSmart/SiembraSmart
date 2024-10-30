package com.example.siembrasmart.views

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.MainActivity
import com.example.siembrasmart.models.Usuario
import com.example.siembrasmart.databinding.ActivitySignupBinding
import com.example.siembrasmart.controllers.SignupController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var controller: SignupController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = SignupController(Firebase.auth, Firebase.database.reference)

        setupListeners()

        binding.backbutton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupListeners() {
        binding.saveChangesButton.setOnClickListener {
            val firstName = binding.firstName.text.toString().trim()
            val lastName = binding.lastName.text.toString().trim()
            val phoneNumber = binding.phoneNumber.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (password.length >= 6) {
                    if (password == confirmPassword) {
                        controller.createAccount(email, password, firstName, lastName, phoneNumber,this)
                    } else {
                        Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor ingrese todos los campos requeridos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onAccountCreationSuccess() {
        Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, StartFormActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun onAccountCreationFailure(errorMessage: String) {
        Toast.makeText(this, "Fallo en la creación de la cuenta: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    fun onUserSaveSuccess() {
        Toast.makeText(this, "Usuario guardado exitosamente", Toast.LENGTH_SHORT).show()
    }

    fun onUserSaveFailure(errorMessage: String) {
        Toast.makeText(this, "Error al guardar el usuario: $errorMessage", Toast.LENGTH_SHORT).show()
    }
}
