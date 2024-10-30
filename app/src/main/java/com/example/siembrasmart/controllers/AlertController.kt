package com.example.siembrasmart.controllers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class AlertController {

    private val client = OkHttpClient()

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
