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
            "maíz" -> cargarFormularioMaiz()
            "frijol" -> cargarFormularioFrijol()

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


    private fun cargarFormularioMaiz() {
        val formularioMaizView = layoutInflater.inflate(R.layout.formulario_maiz, binding.formularioContainer, false)
        binding.formularioContainer.addView(formularioMaizView)

        // Input fields for maize parameters
        val yearInput: EditText = formularioMaizView.findViewById(R.id.etYear)
        val maizeAcreageInput: EditText = formularioMaizView.findViewById(R.id.etMaizeHectare)
        val maizeImprovedAcreageInput: EditText = formularioMaizView.findViewById(R.id.etMaizeImprovedHectare)
        val maizeImprovedCostInput: EditText = formularioMaizView.findViewById(R.id.etMaizeImprovedCost)
        val maizeHarvestedInput: EditText = formularioMaizView.findViewById(R.id.etMaizeHarvested)
        val maizeSoldPriceInput: EditText = formularioMaizView.findViewById(R.id.etMaizeSoldPrice)
        val maizeHarvestLossInput: EditText = formularioMaizView.findViewById(R.id.etMaizeHarvestLoss)
        val resultadoTextView: TextView = formularioMaizView.findViewById(R.id.tvResult)
        val predictButton: Button = formularioMaizView.findViewById(R.id.btnPredictMaize)
        val clasificacionTextView: TextView = formularioMaizView.findViewById(R.id.tvClassification)
        val consejosTextView: TextView = formularioMaizView.findViewById(R.id.tvAdvice)

        // Fertilizer section
        val switchFertilizer: Switch = formularioMaizView.findViewById(R.id.switchFertilizer)
        val layoutFertilizer: LinearLayout = formularioMaizView.findViewById(R.id.layoutFertilizer)
        val maizeAcreageFertilizerInput: EditText = formularioMaizView.findViewById(R.id.etMaizeHectareFertilizer)
        val maizeFertilizerCostInput: EditText = formularioMaizView.findViewById(R.id.etMaizeFertilizerCost)

        // Chemical section
        val switchChemicals: Switch = formularioMaizView.findViewById(R.id.switchChemicals)
        val layoutChemicals: LinearLayout = formularioMaizView.findViewById(R.id.layoutChemicals)
        val maizeChemicalAcreageInput: EditText = formularioMaizView.findViewById(R.id.etMaizeChemicalHectare)
        val maizeChemicalCostInput: EditText = formularioMaizView.findViewById(R.id.etMaizeChemicalCost)

        // Machinery section
        val switchMachinery: Switch = formularioMaizView.findViewById(R.id.switchMachinery)
        val layoutMachinery: LinearLayout = formularioMaizView.findViewById(R.id.layoutMachinery)
        val maizeMachineryAcreageInput: EditText = formularioMaizView.findViewById(R.id.etMaizeMachineryHectare)
        val maizeMachineryCostInput: EditText = formularioMaizView.findViewById(R.id.etMaizeMachineryCost)

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
            requestData.put("crop_type", "maiz")
            val parameters = JSONObject()

            // Main input values
            parameters.put("Year", yearInput.text.toString().toIntOrNull() ?: 2018)
            parameters.put("maize_hectare", maizeAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("maize_improved_hectare", maizeImprovedAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("maize_improved_cost", maizeImprovedCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("maize_harvested", maizeHarvestedInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("maize_sold_price", maizeSoldPriceInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("maize_harvest_loss", maizeHarvestLossInput.text.toString().toDoubleOrNull() ?: 0.0)

            // Add fertilizer parameters, set to 0.0 if hidden
            parameters.put("maize_hectare_fertilizer", if (layoutFertilizer.visibility == View.VISIBLE) {
                maizeAcreageFertilizerInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)
            parameters.put("maize_fertilizer_cost", if (layoutFertilizer.visibility == View.VISIBLE) {
                maizeFertilizerCostInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)

            // Add chemical parameters, set to 0.0 if hidden
            parameters.put("maize_chemical_hectare", if (layoutChemicals.visibility == View.VISIBLE) {
                maizeChemicalAcreageInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)
            parameters.put("maize_chemical_cost", if (layoutChemicals.visibility == View.VISIBLE) {
                maizeChemicalCostInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)

            // Add machinery parameters, set to 0.0 if hidden
            parameters.put("maize_machinery_hectare", if (layoutMachinery.visibility == View.VISIBLE) {
                maizeMachineryAcreageInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)
            parameters.put("maize_machinery_cost", if (layoutMachinery.visibility == View.VISIBLE) {
                maizeMachineryCostInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)

            requestData.put("parameters", parameters)

            // Send request using controller
            controller.makePredictionRequest(requestData) { response ->
                runOnUiThread {
                    Log.d("ConsejosActivity", "Response: $response")  // Log response for debugging

                    try {
                        val jsonResponse = JSONObject(response)

                        // Extract values from JSON response
                        val rendimientoPredicho = jsonResponse.getDouble("Rendimiento_Predicho")
                        val clasificacion = jsonResponse.getString("Clasificacion")
                        val consejosArray = jsonResponse.getJSONArray("Consejos")

                        // Display predicted yield
                        val formattedResult = String.format("%.2f kg/ha", rendimientoPredicho)
                        resultadoTextView.text = "Rendimiento Predicho: $formattedResult"

                        // Display classification
                        clasificacionTextView.text = "Clasificación: $clasificacion"

                        // Display advice in list format
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

    private fun cargarFormularioFrijol() {
        val formularioFrijolView = layoutInflater.inflate(R.layout.formulario_frijol, binding.formularioContainer, false)
        binding.formularioContainer.addView(formularioFrijolView)

        // Input fields for beans parameters
        val yearInput: EditText = formularioFrijolView.findViewById(R.id.etYear)
        val beansAcreageInput: EditText = formularioFrijolView.findViewById(R.id.etBeansHectare)
        val beansImprovedAcreageInput: EditText = formularioFrijolView.findViewById(R.id.etBeansImprovedHectare)
        val beansImprovedCostInput: EditText = formularioFrijolView.findViewById(R.id.etBeansImprovedCost)
        val beansHarvestedInput: EditText = formularioFrijolView.findViewById(R.id.etBeansHarvested)
        val beansSoldPriceInput: EditText = formularioFrijolView.findViewById(R.id.etBeansSoldPrice)
        val beansHarvestLossInput: EditText = formularioFrijolView.findViewById(R.id.etBeansHarvestLoss)
        val resultadoTextView: TextView = formularioFrijolView.findViewById(R.id.tvResult)
        val predictButton: Button = formularioFrijolView.findViewById(R.id.btnPredictBeans)
        val clasificacionTextView: TextView = formularioFrijolView.findViewById(R.id.tvClassification)
        val consejosTextView: TextView = formularioFrijolView.findViewById(R.id.tvAdvice)

        // Fertilizer section
        val switchFertilizer: Switch = formularioFrijolView.findViewById(R.id.switchFertilizer)
        val layoutFertilizer: LinearLayout = formularioFrijolView.findViewById(R.id.layoutFertilizer)
        val beansAcreageFertilizerInput: EditText = formularioFrijolView.findViewById(R.id.etBeansHectareFertilizer)
        val beansFertilizerCostInput: EditText = formularioFrijolView.findViewById(R.id.etBeansFertilizerCost)

        // Chemical section
        val switchChemicals: Switch = formularioFrijolView.findViewById(R.id.switchChemicals)
        val layoutChemicals: LinearLayout = formularioFrijolView.findViewById(R.id.layoutChemicals)
        val beansChemicalAcreageInput: EditText = formularioFrijolView.findViewById(R.id.etBeansChemicalHectare)
        val beansChemicalCostInput: EditText = formularioFrijolView.findViewById(R.id.etBeansChemicalCost)

        // Machinery section
        val switchMachinery: Switch = formularioFrijolView.findViewById(R.id.switchMachinery)
        val layoutMachinery: LinearLayout = formularioFrijolView.findViewById(R.id.layoutMachinery)
        val beansMachineryAcreageInput: EditText = formularioFrijolView.findViewById(R.id.etBeansMachineryHectare)
        val beansMachineryCostInput: EditText = formularioFrijolView.findViewById(R.id.etBeansMachineryCost)

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
            requestData.put("crop_type", "frijol")
            val parameters = JSONObject()

            // Main input values
            parameters.put("Year", yearInput.text.toString().toIntOrNull() ?: 2018)
            parameters.put("beans_hectare", beansAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("beans_improved_hectare", beansImprovedAcreageInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("beans_improved_cost", beansImprovedCostInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("beans_harvested", beansHarvestedInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("beans_sold_price", beansSoldPriceInput.text.toString().toDoubleOrNull() ?: 0.0)
            parameters.put("beans_harvest_loss", beansHarvestLossInput.text.toString().toDoubleOrNull() ?: 0.0)

            // Add fertilizer parameters, set to 0.0 if hidden
            parameters.put("beans_hectare_fertilizer", if (layoutFertilizer.visibility == View.VISIBLE) {
                beansAcreageFertilizerInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)
            parameters.put("beans_fertilizer_cost", if (layoutFertilizer.visibility == View.VISIBLE) {
                beansFertilizerCostInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)

            // Add chemical parameters, set to 0.0 if hidden
            parameters.put("beans_chemical_hectare", if (layoutChemicals.visibility == View.VISIBLE) {
                beansChemicalAcreageInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)
            parameters.put("beans_chemical_cost", if (layoutChemicals.visibility == View.VISIBLE) {
                beansChemicalCostInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)

            // Add machinery parameters, set to 0.0 if hidden
            parameters.put("beans_machinery_hectare", if (layoutMachinery.visibility == View.VISIBLE) {
                beansMachineryAcreageInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)
            parameters.put("beans_machinery_cost", if (layoutMachinery.visibility == View.VISIBLE) {
                beansMachineryCostInput.text.toString().toDoubleOrNull() ?: 0.0
            } else 0.0)

            requestData.put("parameters", parameters)

            // Send request using controller
            controller.makePredictionRequest(requestData) { response ->
                runOnUiThread {
                    Log.d("ConsejosActivity", "Response: $response")  // Log response for debugging

                    try {
                        val jsonResponse = JSONObject(response)

                        // Extract values from JSON response
                        val rendimientoPredicho = jsonResponse.getDouble("Rendimiento_Predicho")
                        val clasificacion = jsonResponse.getString("Clasificacion")
                        val consejosArray = jsonResponse.getJSONArray("Consejos")

                        // Display predicted yield
                        val formattedResult = String.format("%.2f kg/ha", rendimientoPredicho)
                        resultadoTextView.text = "Rendimiento Predicho: $formattedResult"

                        // Display classification
                        clasificacionTextView.text = "Clasificación: $clasificacion"

                        // Display advice in list format
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
