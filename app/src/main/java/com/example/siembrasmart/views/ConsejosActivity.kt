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

        // Campos de entrada para café
        val coffeeAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeAcreage)
        val coffeeImprovedAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeImprovedAcreage)
        val coffeeImprovedCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeImprovedCost)
        val coffeeHarvestedInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeHarvested)
        val coffeeSoldPriceInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeSoldPrice)
        val coffeeHarvestLossInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeHarvestLoss)

        val switchFertilizer: Switch = formularioCafeView.findViewById(R.id.switchFertilizer)
        val layoutFertilizer: LinearLayout = formularioCafeView.findViewById(R.id.layoutFertilizer)
        val coffeeAcreageFertilizerInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeAcreageFertilizer)
        val coffeeFertilizerCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeFertilizerCost)

        val switchChemicals: Switch = formularioCafeView.findViewById(R.id.switchChemicals)
        val layoutChemicals: LinearLayout = formularioCafeView.findViewById(R.id.layoutChemicals)
        val coffeeChemicalAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeChemicalAcreage)
        val coffeeChemicalCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeChemicalCost)

        val switchMachinery: Switch = formularioCafeView.findViewById(R.id.switchMachinery)
        val layoutMachinery: LinearLayout = formularioCafeView.findViewById(R.id.layoutMachinery)
        val coffeeMachineryAcreageInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeMachineryAcreage)
        val coffeeMachineryCostInput: EditText = formularioCafeView.findViewById(R.id.etCoffeeMachineryCost)

        val resultadoTextView: TextView = formularioCafeView.findViewById(R.id.tvResultCoffee)
        val predictButton: Button = formularioCafeView.findViewById(R.id.btnPredictCoffee)

        // Añadir listeners a los switches para controlar la visibilidad de los campos
        switchFertilizer.setOnCheckedChangeListener { _, isChecked ->
            layoutFertilizer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        switchChemicals.setOnCheckedChangeListener { _, isChecked ->
            layoutChemicals.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        switchMachinery.setOnCheckedChangeListener { _, isChecked ->
            layoutMachinery.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Botón de predicción
        predictButton.setOnClickListener {
            val data = JSONObject()
            data.put("coffee_acreage", coffeeAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_improved_acreage", coffeeImprovedAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_improved_cost", coffeeImprovedCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_harvested", coffeeHarvestedInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_sold_price", coffeeSoldPriceInput.text.toString().toDoubleOrNull() ?: 0.0)
            data.put("coffee_harvest_loss", coffeeHarvestLossInput.text.toString().toDoubleOrNull() ?: 0.0)

            // Añadir parámetros de fertilizantes si el switch está activado
            if (switchFertilizer.isChecked) {
                data.put("coffee_acreage_fertilizer", coffeeAcreageFertilizerInput.text.toString().toDoubleOrNull() ?: 0.0)
                data.put("coffee_fertilizer_cost", coffeeFertilizerCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            } else {
                data.put("coffee_acreage_fertilizer", 0.0)
                data.put("coffee_fertilizer_cost", 0.0)
            }

            // Añadir parámetros de químicos si el switch está activado
            if (switchChemicals.isChecked) {
                data.put("coffee_chemical_acreage", coffeeChemicalAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
                data.put("coffee_chemical_cost", coffeeChemicalCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            } else {
                data.put("coffee_chemical_acreage", 0.0)
                data.put("coffee_chemical_cost", 0.0)
            }

            // Añadir parámetros de maquinaria si el switch está activado
            if (switchMachinery.isChecked) {
                data.put("coffee_machinery_acreage", coffeeMachineryAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
                data.put("coffee_machinery_cost", coffeeMachineryCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            } else {
                data.put("coffee_machinery_acreage", 0.0)
                data.put("coffee_machinery_cost", 0.0)
            }

            // Realizar la solicitud de predicción
            controller.makePredictionRequest(data) { result ->
                runOnUiThread {
                    val formattedResult2 = result.substringBefore(".") + "." + result.substringAfter(".").take(2)
                    resultadoTextView.text = "$formattedResult2 kg"
                }
            }
        }
    }

}
