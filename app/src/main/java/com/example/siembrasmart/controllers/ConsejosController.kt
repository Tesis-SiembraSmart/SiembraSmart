package com.example.siembrasmart.controllers

import android.R
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class ConsejosController {
    private val client = OkHttpClient()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun makePredictionRequest(data: JSONObject, callback: (String) -> Unit) {
        // Replace the IP with your Render service URL
        val url = "https://api-modelos.onrender.com/predict" // Replace with your actual URL
        Log.d("ConsejosController", "URL de la API: $url")

        // Create the request body with JSON
        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), data.toString())

        // Create the POST request
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        Log.d("ConsejosController", "Solicitud POST creada")

        // Execute the request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ConsejosController", "Error al realizar la solicitud: ${e.message}")
                callback("Error en la solicitud: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("ConsejosController", "Solicitud completada con éxito")
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    Log.d("ConsejosController", "Respuesta del servidor: $responseBody")
                    val jsonResponse = JSONObject(responseBody)
                    val rendimientoPredicho = jsonResponse.optDouble("Rendimiento_Predicho", Double.NaN)

                    if (!rendimientoPredicho.isNaN()) {
                        callback("Rendimiento Predicho: $rendimientoPredicho")
                    } else {
                        callback("Error en el análisis de la respuesta")
                    }
                } else {
                    callback("Respuesta sin cuerpo")
                }
            }
        })
    }

    // Configuración del spinner para seleccionar modelos
    fun configurarSpinner(
        userId: String,
        spinner: Spinner,
        onItemSelected: (String) -> Unit
    ) {
        database.child("users").child(userId).child("modelosUsados").get().addOnSuccessListener { dataSnapshot ->
            val modelosUsados = dataSnapshot.children.mapNotNull { it.getValue(String::class.java) }
            val cultivos = modelosUsados.map { modelo ->
                modelo.split(" ").getOrElse(1) { modelo }
            }

            // Crear un ArrayAdapter usando el listado de cultivos extraídos
            val context = spinner.context
            val adaptador = ArrayAdapter(
                context,
                R.layout.simple_spinner_item,
                cultivos
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            spinner.adapter = adaptador

            // Establecer el listener para el Spinner
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    onItemSelected(cultivos[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // No se necesita acción aquí
                }
            }
        }
    }
}
