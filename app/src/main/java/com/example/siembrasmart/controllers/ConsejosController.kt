package com.example.siembrasmart.controllers

import android.util.Log
import com.example.siembrasmart.models.Consejos
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class ConsejosController(private val model: Consejos) {
    private val client = OkHttpClient()

    fun makePredictionRequest(callback: (String) -> Unit) {
        val url = "http://18.224.151.11:8000/predict"
        Log.d("ConsejosController", "URL de la API: $url")

        // Crear el JSON con los datos
        val json = JSONObject().apply {
            put("Area_Sembrada", model.areaSembrada)
            put("Area_Cosechada", model.areaCosechada)
            put("Produccion", model.produccion)
        }
        Log.d("ConsejosController", "JSON de la solicitud: $json")

        // Crear el cuerpo de la solicitud con JSON
        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())

        // Crear la solicitud POST
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        Log.d("ConsejosController", "Solicitud POST creada")

        // Ejecutar la solicitud
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
                    model.rendimientoPredicho = jsonResponse.optDouble("Rendimiento_Predicho", Double.NaN)

                    if (!model.rendimientoPredicho.isNaN()) {
                        callback("Rendimiento Predicho: ${model.rendimientoPredicho}")
                    } else {
                        callback("Error en el análisis de la respuesta")
                    }
                } else {
                    callback("Respuesta sin cuerpo")
                }
            }
        })
    }
}
