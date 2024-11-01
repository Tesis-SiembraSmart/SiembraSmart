package com.example.siembrasmart.controllers

import com.example.siembrasmart.models.Clima
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class ClimaController(private val clima: Clima) {

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
                        actualizarDatosDesdeJson(json)
                    }
                } else {
                    // Manejar el error
                }
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }

    private fun actualizarDatosDesdeJson(json: JSONObject) {
        val currentData = json.getJSONObject("current")
        val hourlyData = json.getJSONObject("hourly")

        clima.temperatura = currentData.optDouble("temperature_2m", Double.NaN)
        clima.humedad = currentData.optInt("relative_humidity_2m", -1)
        clima.aparenteTemperatura = currentData.optDouble("apparent_temperature", Double.NaN)
        clima.precipitacion = currentData.optDouble("precipitation", Double.NaN)
        clima.nubosidad = currentData.optInt("cloud_cover", -1)
        clima.velocidadViento = currentData.optDouble("wind_speed_10m", Double.NaN)

        clima.temperaturas = jsonArrayToDoubleList(hourlyData.getJSONArray("temperature_2m"))
        clima.humedades = jsonArrayToIntList(hourlyData.getJSONArray("relative_humidity_2m"))
        clima.probabilidadesPrecipitacion = jsonArrayToIntList(hourlyData.getJSONArray("precipitation_probability"))
        clima.precipitaciones = jsonArrayToDoubleList(hourlyData.getJSONArray("precipitation"))
        clima.evapotranspiraciones = jsonArrayToDoubleList(hourlyData.getJSONArray("evapotranspiration"))
        clima.velocidadesViento = jsonArrayToDoubleList(hourlyData.getJSONArray("wind_speed_10m"))
        clima.humedadesSuelo = jsonArrayToDoubleList(hourlyData.getJSONArray("soil_moisture_0_to_1cm"))

        clima.times = jsonArrayToStringList(hourlyData.getJSONArray("time"))
    }

    private fun jsonArrayToStringList(jsonArray: JSONArray): MutableList<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.optString(i, "")) // Agregar verificación por si algún valor es nulo
        }
        return list
    }

    private fun jsonArrayToDoubleList(jsonArray: JSONArray): MutableList<Double> {
        val list = mutableListOf<Double>()
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.optDouble(i, Double.NaN)
            if (!value.isNaN()) { // Solo agregar valores válidos
                list.add(value)
            }
        }
        return list
    }

    private fun jsonArrayToIntList(jsonArray: JSONArray): MutableList<Int> {
        val list = mutableListOf<Int>()
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.optInt(i, -1)
            if (value != -1) { // Solo agregar valores válidos
                list.add(value)
            }
        }
        return list
    }
}
