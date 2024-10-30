package com.example.siembrasmart.controllers

import com.example.siembrasmart.models.ClimaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ClimaController(private val climaModel: ClimaModel) {

    private val client = OkHttpClient()
    private val dias = 14

    suspend fun fetchDatosClima(latitud: Double, longitud: Double) {
        withContext(Dispatchers.IO) {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitud&longitude=$longitud" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,rain,cloud_cover,wind_speed_10m" +
                    "&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,evapotranspiration,wind_speed_10m,soil_moisture_0_to_1cm" +
                    "&forecast_days=$dias&models=best_match"
            print(url)
            val request = Request.Builder().url(url).build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        val json = JSONObject(it)
                        climaModel.actualizarDatosDesdeJson(json)
                    }
                } else {
                    // Manejar el error
                }
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }
}