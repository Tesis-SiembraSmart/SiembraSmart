package com.example.siembrasmart.models

import org.json.JSONArray
import org.json.JSONObject

class ClimaModel {
    var ubicacion: String = ""
    var temperatura: Double = 0.0
    var humedad: Int = 0
    var aparenteTemperatura: Double = 0.0
    var precipitacion: Double = 0.0
    var nubosidad: Int = 0
    var velocidadViento: Double = 0.0

    var temperaturas: MutableList<Double> = mutableListOf()
    var humedades: MutableList<Int> = mutableListOf()
    var probabilidadesPrecipitacion: MutableList<Int> = mutableListOf()
    var precipitaciones: MutableList<Double> = mutableListOf()
    var evapotranspiraciones: MutableList<Double> = mutableListOf()
    var velocidadesViento: MutableList<Double> = mutableListOf()
    var humedadesSuelo: MutableList<Double> = mutableListOf()
    var times: MutableList<String> = mutableListOf()

    fun actualizarDatosDesdeJson(json: JSONObject) {
        val currentData = json.getJSONObject("current")
        val hourlyData = json.getJSONObject("hourly")

        // Uso de optDouble y optInt con valores predeterminados en caso de error
        temperatura = currentData.optDouble("temperature_2m", Double.NaN)
        humedad = currentData.optInt("relative_humidity_2m", -1)
        aparenteTemperatura = currentData.optDouble("apparent_temperature", Double.NaN)
        precipitacion = currentData.optDouble("precipitation", Double.NaN)
        nubosidad = currentData.optInt("cloud_cover", -1)
        velocidadViento = currentData.optDouble("wind_speed_10m", Double.NaN)

        temperaturas = jsonArrayToDoubleList(hourlyData.getJSONArray("temperature_2m"))
        humedades = jsonArrayToIntList(hourlyData.getJSONArray("relative_humidity_2m"))
        probabilidadesPrecipitacion = jsonArrayToIntList(hourlyData.getJSONArray("precipitation_probability"))
        precipitaciones = jsonArrayToDoubleList(hourlyData.getJSONArray("precipitation"))
        evapotranspiraciones = jsonArrayToDoubleList(hourlyData.getJSONArray("evapotranspiration"))
        velocidadesViento = jsonArrayToDoubleList(hourlyData.getJSONArray("wind_speed_10m"))
        humedadesSuelo = jsonArrayToDoubleList(hourlyData.getJSONArray("soil_moisture_0_to_1cm"))

        times = jsonArrayToStringList(hourlyData.getJSONArray("time"))
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
