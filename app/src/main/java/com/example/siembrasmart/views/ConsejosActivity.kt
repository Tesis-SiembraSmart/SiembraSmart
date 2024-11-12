package com.example.siembrasmart.views

import android.os.Bundle
import android.util.Log
import android.view.View
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
import android.widget.LinearLayout
import android.widget.Switch

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
        val clasificacionTextView: TextView = formularioCacaoView.findViewById(R.id.tvClassification)
        val consejosTextView: TextView = formularioCacaoView.findViewById(R.id.tvAdvice)
        val predictButton: Button = formularioCacaoView.findViewById(R.id.btnPredict)

        predictButton.setOnClickListener {
            val requestData = JSONObject()
            requestData.put("crop_type", "cacao")
            val parameters = JSONObject()
            parameters.put("Area_Sembrada", areaSembradaInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("Area_Cosechada", areaCosechadaInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("Produccion", produccionInput.text.toString().toDoubleOrNull() ?: 0.0)
            requestData.put("parameters", parameters)
            // Enviar la solicitud usando el controlador
            controller.makePredictionRequest(requestData) { response ->
                runOnUiThread {
                    Log.d("ConsejosActivity", "Response: $response")  // Verificar la respuesta recibida

                    try {
                        // Convertir el response a JSONObject directamente
                        val jsonResponse = JSONObject(response)

                        // Extraer los valores de la respuesta JSON
                        val rendimientoPredicho = jsonResponse.getDouble("Rendimiento_Predicho")
                        val clasificacion = jsonResponse.getString("Clasificacion")
                        val consejosArray = jsonResponse.getJSONArray("Consejos")

                        // Mostrar el rendimiento predicho
                        val formattedResult = String.format("%.2f t/ha", rendimientoPredicho)
                        resultadoTextView.text = "Rendimiento Predicho: $formattedResult"

                        // Mostrar la clasificación
                        clasificacionTextView.text = "Clasificación: $clasificacion"

                        // Mostrar los consejos en formato de lista
                        val consejosText = StringBuilder("")
                        for (i in 0 until consejosArray.length()) {
                            consejosText.append("- ${consejosArray.getString(i)}\n")
                        }
                        consejosTextView.text = consejosText.toString()

                    } catch (e: Exception) {

                        Log.e("ConsejosActivity", "Error al procesar la respuesta JSON", e)
                        resultadoTextView.text = "Error al obtener la predicción"
                        clasificacionTextView.text = ""
                        consejosTextView.text = ""
                    }
                }
            }

        }
    }



    private fun cargarFormularioCafe() {
        val formularioCafeView = layoutInflater.inflate(R.layout.formulario_cafe, binding.formularioContainer, false)
        binding.formularioContainer.addView(formularioCafeView)

        // Input fields for coffee parameters
        val yearInput: EditText = formularioCafeView.findViewById(R.id.etYear)
        val coffeeAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeHectare)
        val coffeeImprovedAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeImprovedHectare)
        val coffeeImprovedCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeImprovedCost)
        val coffeeHarvestedInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeHarvested)
        val coffeeSoldPriceInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeSoldPrice)
        val coffeeHarvestLossInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeHarvestLoss)
        val resultadoTextView: TextView = formularioCafeView.findViewById(R.id.tvResult)
        val predictButton: Button = formularioCafeView.findViewById(R.id.btnPredictCoffee)
        val clasificacionTextView: TextView = formularioCafeView.findViewById(R.id.tvClassification)
        val consejosTextView: TextView = formularioCafeView.findViewById(R.id.tvAdvice)

        // Fertilizer section
        val switchFertilizer: Switch = formularioCafeView.findViewById(R.id.switchFertilizer)
        val layoutFertilizer: LinearLayout = formularioCafeView.findViewById(R.id.layoutFertilizer)
        val coffeeAcreageFertilizerInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeHectareFertilizer)
        val coffeeFertilizerCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeFertilizerCost)

        // Chemical section
        val switchChemicals: Switch = formularioCafeView.findViewById(R.id.switchChemicals)
        val layoutChemicals: LinearLayout = formularioCafeView.findViewById(R.id.layoutChemicals)
        val coffeeChemicalAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeChemicalHectare)
        val coffeeChemicalCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeChemicalCost)

        // Machinery section
        val switchMachinery: Switch = formularioCafeView.findViewById(R.id.switchMachinery)
        val layoutMachinery: LinearLayout = formularioCafeView.findViewById(R.id.layoutMachinery)
        val coffeeMachineryAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeMachineryHectare)
        val coffeeMachineryCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeMachineryCost)



        // Listeners for switches to show/hide layouts
        switchFertilizer.setOnCheckedChangeListener { _, isChecked ->
            layoutFertilizer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        switchChemicals.setOnCheckedChangeListener { _, isChecked ->
            layoutChemicals.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        switchMachinery.setOnCheckedChangeListener { _, isChecked ->
            layoutMachinery.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Prediction button action
        predictButton.setOnClickListener {
            val requestData = JSONObject()
            requestData.put("crop_type", "cafe")
            val parameters = JSONObject()

            // Main input values
            parameters.put("Year", yearInput.text.toString().toIntOrNull() ?: 2022)
            parameters.put("coffee_hectare", coffeeAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("coffee_improved_hectare", coffeeImprovedAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("coffee_improved_cost", coffeeImprovedCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("coffee_harvested", coffeeHarvestedInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("coffee_sold_price", coffeeSoldPriceInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("coffee_harvest_loss", coffeeHarvestLossInput.text.toString().toDoubleOrNull() ?: 0.0)

            // Add fertilizer parameters, set to 0.0 if hidden
            parameters.put("coffee_hectare_fertilizer", if (layoutFertilizer.visibility == View.VISIBLE) {
                coffeeAcreageFertilizerInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)
            parameters.put("coffee_fertilizer_cost", if (layoutFertilizer.visibility == View.VISIBLE) {
                coffeeFertilizerCostInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)

            // Add chemical parameters, set to 0.0 if hidden
            parameters.put("coffee_chemical_hectare", if (layoutChemicals.visibility == View.VISIBLE) {
                coffeeChemicalAcreageInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)
            parameters.put("coffee_chemical_cost", if (layoutChemicals.visibility == View.VISIBLE) {
                coffeeChemicalCostInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)

            // Add machinery parameters, set to 0.0 if hidden
            parameters.put("coffee_machinery_hectare", if (layoutMachinery.visibility == View.VISIBLE) {
                coffeeMachineryAcreageInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)
            parameters.put("coffee_machinery_cost", if (layoutMachinery.visibility == View.VISIBLE) {
                coffeeMachineryCostInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)

            requestData.put("parameters", parameters)

            // Send request using controller
            // Enviar la solicitud usando el controlador
            controller.makePredictionRequest(requestData) { response ->
                runOnUiThread {
                    Log.d("ConsejosActivity", "Response: $response")  // Verificar la respuesta recibida

                    try {
                        // Convertir el response a JSONObject directamente
                        val jsonResponse = JSONObject(response)

                        // Extraer los valores de la respuesta JSON
                        val rendimientoPredicho = jsonResponse.getDouble("Rendimiento_Predicho")
                        val clasificacion = jsonResponse.getString("Clasificacion")
                        val consejosArray = jsonResponse.getJSONArray("Consejos")

                        // Mostrar el rendimiento predicho
                        val formattedResult = String.format("%.2f  kg/ha", rendimientoPredicho)
                        resultadoTextView.text = "Rendimiento Predicho: $formattedResult"

                        // Mostrar la clasificación
                        clasificacionTextView.text = "Clasificación: $clasificacion"

                        // Mostrar los consejos en formato de lista
                        val consejosText = StringBuilder("")
                        for (i in 0 until consejosArray.length()) {
                            consejosText.append("- ${consejosArray.getString(i)}\n")
                        }
                        consejosTextView.text = consejosText.toString()

                    } catch (e: Exception) {

                        Log.e("ConsejosActivity", "Error al procesar la respuesta JSON", e)
                        resultadoTextView.text = "Error al obtener la predicción"
                        clasificacionTextView.text = ""
                        consejosTextView.text = ""
                    }
                }
            }
        }
    }




}
