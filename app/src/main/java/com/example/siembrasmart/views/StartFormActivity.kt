package com.example.siembrasmart.views
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.controllers.StartFormController
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

        if (userId != null) {
            // Fetch and pre-select the user's saved models
            controller.getSelectedModels(userId) { selectedModels ->
                selectedModels?.let {
                    binding.CheckBox1.isChecked = it.contains("Modelo cacao")
                    binding.CheckBox2.isChecked = it.contains("Modelo cafe")
                    binding.CheckBox3.isChecked = it.contains("Modelo maíz")
                    binding.CheckBox4.isChecked = it.contains("Modelo frijol")
                }
            }
        }

        binding.acceptButton.setOnClickListener {
            if (userId != null) {
                val selectedModels = mutableListOf<String>()
                if (binding.CheckBox1.isChecked) selectedModels.add("Modelo cacao")
                if (binding.CheckBox2.isChecked) selectedModels.add("Modelo cafe")
                if (binding.CheckBox3.isChecked) selectedModels.add("Modelo maíz")
                if (binding.CheckBox4.isChecked) selectedModels.add("Modelo frijol")

                if (selectedModels.isNotEmpty()) {
                    controller.saveSelectedModels(userId, selectedModels) { success ->
                        if (success) {
                            Toast.makeText(
                                this,
                                "Opciones guardadas correctamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, RegistroCultivoActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Error al guardar las opciones.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Por favor selecciona al menos una opción",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
