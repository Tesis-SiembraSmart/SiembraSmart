package com.example.siembrasmart.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.MainActivity
import com.example.siembrasmart.controllers.StartFormController
import com.example.siembrasmart.databinding.ActivitySignupBinding
import com.example.siembrasmart.databinding.ActivityStartFormBinding
import com.google.firebase.auth.FirebaseAuth

class StartFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartFormBinding
    private val controller = StartFormController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        binding.acceptButton.setOnClickListener {
            if (userId != null) {
                val selectedModel = when {
                    binding.radioButton1.isChecked -> "Modelo 1"
                    binding.radioButton2.isChecked -> "Modelo 2"
                    binding.radioButton3.isChecked -> "Modelo 3"
                    binding.radioButton4.isChecked -> "Modelo 4"
                    else -> ""
                }

                if (selectedModel.isNotEmpty()) {
                    controller.saveSelectedModel(userId, selectedModel) {
                        if (it) {
                            Toast.makeText(this, "Opción guardada correctamente.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, RegistroCultivoActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error al guardar la opción.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Por favor selecciona una opción", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}