package com.example.siembrasmart.controllers

import com.example.siembrasmart.models.Alertas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AlertController {

    private val client = OkHttpClient()

    // Listas para almacenar los datos
    private val times: MutableList<String> = mutableListOf()
    private val descargaRioMax: MutableList<Double> = mutableListOf()
    private val descargaRioMedia: MutableList<Double> = mutableListOf()
    private val descargaRioMin: MutableList<Double> = mutableListOf()

    suspend fun fetchWeatherData(latitud: Double, longitud: Double, onResult: (Alertas) -> Unit) {
        withContext(Dispatchers.IO) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance()
            val startDate = today.clone() as Calendar
            startDate.add(Calendar.DAY_OF_YEAR, -658)
            val endDate = today.clone() as Calendar
            endDate.add(Calendar.DAY_OF_YEAR, 72)

            val startDateString = dateFormat.format(startDate.time)
            val endDateString = dateFormat.format(endDate.time)

            fetchDatosOpenMeteo(latitud, longitud, startDateString, endDateString) { json ->
                json?.let {
                    val dailyData = it.getJSONObject("daily")

                    // Limpiamos las listas antes de agregar nuevos datos
                    times.clear()
                    descargaRioMax.clear()
                    descargaRioMedia.clear()
                    descargaRioMin.clear()

                    val allTimes = jsonArrayToStringList(dailyData.getJSONArray("time"))
                    val allDescargaRioMax = jsonArrayToDoubleList(dailyData.getJSONArray("river_discharge_max"))
                    val allDescargaRioMedia = jsonArrayToDoubleList(dailyData.getJSONArray("river_discharge_mean"))
                    val allDescargaRioMin = jsonArrayToDoubleList(dailyData.getJSONArray("river_discharge_min"))

                    filterDataFromToday(allTimes, allDescargaRioMax, allDescargaRioMedia, allDescargaRioMin)

                    val maxThresholdRed = calculateThresholdRed(descargaRioMax)
                    val meanThresholdYellow = calculateThresholdYellow(descargaRioMedia)
                    val droughtThresholdRed = calculateDroughtThresholdRed(descargaRioMin)
                    val droughtThresholdYellow = calculateDroughtThresholdYellow(descargaRioMin)

                    val alertas = generateFloodAndDroughtAlerts(
                        descargaRioMax,
                        descargaRioMedia,
                        descargaRioMin,
                        maxThresholdRed,
                        meanThresholdYellow,
                        droughtThresholdRed,
                        droughtThresholdYellow
                    )

                    onResult(alertas)  // Devolvemos las alertas al Activity
                }
            }
        }
    }

    private fun filterDataFromToday(
        allTimes: MutableList<String>,
        allDescargaRioMax: MutableList<Double>,
        allDescargaRioMedia: MutableList<Double>,
        allDescargaRioMin: MutableList<Double>
    ) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        for (i in allTimes.indices) {
            if (allTimes[i] >= today) {
                times.add(allTimes[i])
                descargaRioMax.add(allDescargaRioMax[i])
                descargaRioMedia.add(allDescargaRioMedia[i])
                descargaRioMin.add(allDescargaRioMin[i])
            }
        }
    }

    private fun generateFloodAndDroughtAlerts(
        dischargeMax: MutableList<Double>,
        dischargeMean: MutableList<Double>,
        dischargeMin: MutableList<Double>,
        maxThresholdRed: Double,
        meanThresholdYellow: Double,
        droughtThresholdRed: Double,
        droughtThresholdYellow: Double
    ): Alertas {
        val inundacionRoja = StringBuilder()
        val inundacionAmarilla = StringBuilder()
        val sequiaRoja = StringBuilder()
        val sequiaAmarilla = StringBuilder()

        for (i in dischargeMax.indices) {
            if (dischargeMax[i] > maxThresholdRed) {
                inundacionRoja.append("¡Predicción de Alerta Roja de Inundación! El ${times[i]}, la descarga máxima del río se estima en ${dischargeMax[i]} m³/s.\n")
            } else if (dischargeMean[i] > meanThresholdYellow) {
                inundacionAmarilla.append("Predicción de Alerta Amarilla de Inundación: El ${times[i]}, se estima que la descarga media del río será de ${dischargeMean[i]} m³/s.\n")
            }

            if (dischargeMin[i] < droughtThresholdRed) {
                sequiaRoja.append("¡Predicción de Alerta Roja de Sequía! El ${times[i]}, se espera que la descarga mínima del río sea de ${dischargeMin[i]} m³/s.\n")
            } else if (dischargeMin[i] < droughtThresholdYellow) {
                sequiaAmarilla.append("Predicción de Alerta Amarilla de Sequía: El ${times[i]}, se estima que la descarga mínima del río será de ${dischargeMin[i]} m³/s.\n")
            }
        }

        return Alertas(
            alertaInundacionRoja = inundacionRoja.toString(),
            alertaInundacionAmarilla = inundacionAmarilla.toString(),
            alertaSequíaRoja = sequiaRoja.toString(),
            alertaSequíaAmarilla = sequiaAmarilla.toString()
        )
    }

    private fun calculateThresholdRed(descargaRioMax: MutableList<Double>): Double {
        return descargaRioMax.maxOrNull()?.times(0.9) ?: 150.0
    }

    private fun calculateThresholdYellow(descargaRioMedia: MutableList<Double>): Double {
        return if (descargaRioMedia.isNotEmpty()) {
            descargaRioMedia.average().times(1.1)
        } else {
            100.0
        }
    }

    private fun calculateDroughtThresholdRed(descargaRioMin: MutableList<Double>): Double {
        return descargaRioMin.minOrNull()?.times(1.1) ?: 70.0
    }

    private fun calculateDroughtThresholdYellow(descargaRioMin: MutableList<Double>): Double {
        return descargaRioMin.average().times(1.2)
    }

    suspend fun fetchDatosOpenMeteo(
        latitud: Double,
        longitud: Double,
        startDate: String,
        endDate: String,
        onDataFetched: (JSONObject?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val url = "https://flood-api.open-meteo.com/v1/flood?" +
                    "latitude=$latitud&longitude=$longitud&daily=river_discharge_mean," +
                    "river_discharge_max,river_discharge_min&start_date=$startDate&end_date=$endDate"

            val request = Request.Builder().url(url).build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val json = responseData?.let { JSONObject(it) }
                    onDataFetched(json)
                } else {
                    onDataFetched(null)
                }
            } catch (e: Exception) {
                onDataFetched(null)
            }
        }
    }

    fun jsonArrayToStringList(jsonArray: JSONArray): MutableList<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }

    fun jsonArrayToDoubleList(jsonArray: JSONArray): MutableList<Double> {
        val list = mutableListOf<Double>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getDouble(i))
        }
        return list
    }
}
