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
import com.example.siembrasmart.models.Consejos
import com.example.siembrasmart.controllers.ConsejosController
import com.example.siembrasmart.utils.Navigation

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

        // Limpiar el contenedor antes de agregar el formulario para evitar superposición
        binding.formularioContainer.removeAllViews()

        // Inflar el formulario de cacao y agregarlo al formulario_container
        val formularioCacaoView = layoutInflater.inflate(R.layout.formulario_cacao, binding.formularioContainer, false)
        binding.formularioContainer.addView(formularioCacaoView)

        // Referencias a los campos de entrada y botones en el formulario
        val areaSembradaInput: EditText = formularioCacaoView.findViewById(R.id.etAreaSembrada)
        val areaCosechadaInput: EditText = formularioCacaoView.findViewById(R.id.etAreaCosechada)
        val produccionInput: EditText = formularioCacaoView.findViewById(R.id.etProduccion)
        val resultadoTextView: TextView = formularioCacaoView.findViewById(R.id.tvResult)
        val predictButton: Button = formularioCacaoView.findViewById(R.id.btnPredict)

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
