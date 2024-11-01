package com.example.siembrasmart.views

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.siembrasmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.siembrasmart.databinding.ActivityConsejosBinding
import com.example.siembrasmart.utils.Navigation
import com.example.siembrasmart.models.Consejos
import com.example.siembrasmart.controllers.ConsejosController

class ConsejosActivity : Navigation() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityConsejosBinding
    private lateinit var model: Consejos
    private lateinit var controller: ConsejosController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración de ViewBinding
        binding = ActivityConsejosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de Firebase Auth
        auth = Firebase.auth

        // Inicialización del modelo y controlador
        model = Consejos()
        controller = ConsejosController(model)

        // Configuración de la navegación inferior y la Toolbar
        binding.bottomNavigation.selectedItemId = R.id.navigation_modelo
        setupBottomNavigationView(binding.bottomNavigation)
        setupToolbar(binding.toolbar)

        // Referencias a los campos de entrada y botones
        val areaSembradaInput: EditText = binding.etAreaSembrada
        val areaCosechadaInput: EditText = binding.etAreaCosechada
        val produccionInput: EditText = binding.etProduccion
        val resultadoTextView: TextView = binding.tvResult
        val predictButton: Button = binding.btnPredict

        // Configurar el botón para realizar la predicción
        predictButton.setOnClickListener {
            // Obtener valores de entrada
            val areaSembrada = areaSembradaInput.text.toString()
            val areaCosechada = areaCosechadaInput.text.toString()
            val produccion = produccionInput.text.toString()

            if (areaSembrada.isNotEmpty() && areaCosechada.isNotEmpty() && produccion.isNotEmpty()) {
                Log.d("ConsejosActivity", "Iniciando solicitud de predicción")
                model.areaSembrada = areaSembrada.toInt()
                model.areaCosechada = areaCosechada.toInt()
                model.produccion = produccion.toInt()

                controller.makePredictionRequest { result ->
                    runOnUiThread {
                        resultadoTextView.text = result
                    }
                }
            } else {
                // Mostrar mensaje de error si algún campo está vacío
                resultadoTextView.text = "Por favor, complete todos los campos antes de continuar."
            }
        }
    }
}