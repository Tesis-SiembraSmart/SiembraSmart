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
        private val caudalRioMax: MutableList<Double> = mutableListOf()
        private val caudalRioMedia: MutableList<Double> = mutableListOf()
        private val caudalRioMin: MutableList<Double> = mutableListOf()

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
                        caudalRioMax.clear()
                        caudalRioMedia.clear()
                        caudalRioMin.clear()

                        val allTimes = jsonArrayToStringList(dailyData.getJSONArray("time"))
                        val allCaudalRioMax = jsonArrayToDoubleList(dailyData.getJSONArray("river_discharge_max"))
                        val allCaudalRioMedia = jsonArrayToDoubleList(dailyData.getJSONArray("river_discharge_mean"))
                        val allCaudalRioMin = jsonArrayToDoubleList(dailyData.getJSONArray("river_discharge_min"))

                        filterDataFromToday(allTimes, allCaudalRioMax, allCaudalRioMedia, allCaudalRioMin)

                        val thresholdCaudalAlto = calcularUmbralCaudalAlto(caudalRioMax)
                        val thresholdCaudalModerado = calcularUmbralCaudalModerado(caudalRioMedia)
                        val thresholdCaudalMuyBajo = calcularUmbralCaudalMuyBajo(caudalRioMin)
                        val thresholdCaudalBajo = calcularUmbralCaudalBajo(caudalRioMin)

                        val alertas = generateCaudalAlerts(
                            caudalRioMax,
                            caudalRioMedia,
                            caudalRioMin,
                            thresholdCaudalAlto,
                            thresholdCaudalModerado,
                            thresholdCaudalMuyBajo,
                            thresholdCaudalBajo
                        )

                        onResult(alertas)  // Devolvemos las alertas al Activity
                    }
                }
            }
        }

        private fun filterDataFromToday(
            allTimes: MutableList<String>,
            allCaudalRioMax: MutableList<Double>,
            allCaudalRioMedia: MutableList<Double>,
            allCaudalRioMin: MutableList<Double>
        ) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            for (i in allTimes.indices) {
                if (allTimes[i] >= today) {
                    times.add(allTimes[i])
                    caudalRioMax.add(allCaudalRioMax[i])
                    caudalRioMedia.add(allCaudalRioMedia[i])
                    caudalRioMin.add(allCaudalRioMin[i])
                }
            }
        }

        private fun generateCaudalAlerts(
            caudalMax: MutableList<Double>,
            caudalMean: MutableList<Double>,
            caudalMin: MutableList<Double>,
            thresholdCaudalAlto: Double,
            thresholdCaudalModerado: Double,
            thresholdCaudalMuyBajo: Double,
            thresholdCaudalBajo: Double
        ): Alertas {
            val caudalAlto = StringBuilder()
            val caudalModerado = StringBuilder()
            val caudalMuyBajo = StringBuilder()
            val caudalBajo = StringBuilder()

            for (i in caudalMax.indices) {
                if (caudalMax[i] > thresholdCaudalAlto) {
                    caudalAlto.append("¡Predicción de Alerta de Caudal Alto! El ${times[i]}, la descarga máxima del río se estima en ${caudalMax[i]} m³/s.\n")
                } else if (caudalMean[i] > thresholdCaudalModerado) {
                    caudalModerado.append("Predicción de Alerta de Caudal Moderado: El ${times[i]}, se estima que la descarga media del río será de ${caudalMean[i]} m³/s.\n")
                }

                if (caudalMin[i] < thresholdCaudalMuyBajo) {
                    caudalMuyBajo.append("¡Predicción de Alerta de Caudal Muy Bajo! El ${times[i]}, se espera que la descarga mínima del río sea de ${caudalMin[i]} m³/s.\n")
                } else if (caudalMin[i] < thresholdCaudalBajo) {
                    caudalBajo.append("Predicción de Alerta de Caudal Bajo: El ${times[i]}, se estima que la descarga mínima del río será de ${caudalMin[i]} m³/s.\n")
                }
            }

            return Alertas(
                alertaCaudalAlto = caudalAlto.toString(),
                alertaCaudalModerado = caudalModerado.toString(),
                alertaCaudalMuyBajo = caudalMuyBajo.toString(),
                alertaCaudalBajo = caudalBajo.toString()
            )
        }

        private fun calcularUmbralCaudalAlto(caudalRioMax: MutableList<Double>): Double {
            return caudalRioMax.maxOrNull()?.times(0.9) ?: 150.0
        }

        private fun calcularUmbralCaudalModerado(caudalRioMedia: MutableList<Double>): Double {
            return if (caudalRioMedia.isNotEmpty()) {
                caudalRioMedia.average().times(1.1)
            } else {
                100.0
            }
        }

        private fun calcularUmbralCaudalMuyBajo(caudalRioMin: MutableList<Double>): Double {
            return caudalRioMin.minOrNull()?.times(1.1) ?: 70.0
        }

        private fun calcularUmbralCaudalBajo(caudalRioMin: MutableList<Double>): Double {
            return caudalRioMin.average().times(1.2)
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
