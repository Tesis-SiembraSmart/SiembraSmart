package com.example.siembrasmart.views

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.example.siembrasmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.siembrasmart.databinding.ActivityConsejosBinding
import com.example.siembrasmart.controllers.ConsejosController
import com.example.siembrasmart.utils.Navigation
import org.json.JSONObject

class ConsejosActivity : Navigation() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityConsejosBinding
    private lateinit var controller: ConsejosController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración de ViewBinding
        binding = ActivityConsejosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de Firebase Auth
        auth = Firebase.auth

        // Inicialización del controlador
        controller = ConsejosController()

        // Configuración de la navegación inferior y la Toolbar
        binding.bottomNavigation.selectedItemId = R.id.navigation_modelo
        setupBottomNavigationView(binding.bottomNavigation)
        setupToolbar(binding.toolbar)

        // Configurar el spinner para seleccionar el modelo
        controller.configurarSpinner(auth.currentUser?.uid.orEmpty(), binding.spinnerCrop) { modeloSeleccionado ->
            cargarFormulario(modeloSeleccionado)
        }
    }

    private fun cargarFormulario(modelo: String) {
        // Limpiar el contenedor antes de agregar el formulario para evitar superposición
        binding.formularioContainer.removeAllViews()

        // Inflar el formulario adecuado según el modelo seleccionado
        when (modelo.lowercase()) {
            "cacao" -> cargarFormularioCacao()
            "cafe" -> cargarFormularioCafe()
            else -> Log.d("ConsejosActivity", "No hay formulario disponible para el modelo: $modelo")
        }
    }

    private fun cargarFormularioCacao() {
        val formularioCacaoView = layoutInflater.inflate(R.layout.formulario_cacao, binding.formularioContainer, false)
        binding.formularioContainer.addView(formularioCacaoView)

        val areaSembradaInput: EditText = formularioCacaoView.findViewById(R.id.etAreaSembrada)
        val areaCosechadaInput: EditText = formularioCacaoView.findViewById(R.id.etAreaCosechada)
        val produccionInput: EditText = formularioCacaoView.findViewById(R.id.etProduccion)
        val resultadoTextView: TextView = formularioCacaoView.findViewById(R.id.tvResult)
        val predictButton: Button = formularioCacaoView.findViewById(R.id.btnPredict)

        predictButton.setOnClickListener {
            val data = JSONObject()
            data.put("Area_Sembrada", areaSembradaInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("Area_Cosechada", areaCosechadaInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("Produccion", produccionInput.text.toString().toDoubleOrNull() ?: 0.0)

            controller.makePredictionRequest(data) { result ->
                runOnUiThread {
                    resultadoTextView.text = result
                }
            }
        }
    }

    private fun cargarFormularioCafe() {
        val formularioCafeView = layoutInflater.inflate(R.layout.formulario_cafe, binding.formularioContainer, false)
        binding.formularioContainer.addView(formularioCafeView)

        val coffeeAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeAcreage)
        val coffeeImprovedAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeImprovedAcreage)
        val coffeeImprovedCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeImprovedCost)
        val coffeeAcreageFertilizerInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeAcreageFertilizer)
        val coffeeFertilizerCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeFertilizerCost)
        val coffeeChemicalAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeChemicalAcreage)
        val coffeeChemicalCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeChemicalCost)
        val coffeeMachineryAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeMachineryAcreage)
        val coffeeMachineryCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeMachineryCost)
        val coffeeHarvestedInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeHarvested)
        val coffeeSoldPriceInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeSoldPrice)
        val coffeeHarvestLossInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeHarvestLoss)
        val resultadoTextView: TextView = formularioCafeView.findViewById(R.id.tvResultCoffee)
        val predictButton: Button = formularioCafeView.findViewById(R.id.btnPredictCoffee)

        predictButton.setOnClickListener {
            val data = JSONObject()
            data.put("coffee_acreage", coffeeAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_improved_acreage", coffeeImprovedAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_improved_cost", coffeeImprovedCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_acreage_fertilizer", coffeeAcreageFertilizerInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_fertilizer_cost", coffeeFertilizerCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_chemical_acreage", coffeeChemicalAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_chemical_cost", coffeeChemicalCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_machinery_acreage", coffeeMachineryAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_machinery_cost", coffeeMachineryCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_harvested", coffeeHarvestedInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_sold_price", coffeeSoldPriceInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_harvest_loss", coffeeHarvestLossInput.text.toString().toDoubleOrNull() ?: 0.0)

            controller.makePredictionRequest(data) { result ->
                runOnUiThread {
                    resultadoTextView.text = result
                }
            }
        }
    }
}
