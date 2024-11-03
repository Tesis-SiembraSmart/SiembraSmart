package com.example.siembrasmart.controllers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.siembrasmart.models.Clima
import com.example.siembrasmart.views.ClimaActivity
import com.example.siembrasmart.views.ForecastsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ClimaController(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val clima = Clima()
    private val client = OkHttpClient()
    private val dias = 14

    suspend fun obtenerDatosClima(callback: (Clima) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Manejo de permisos
            callback(clima) // o manejarlo de acuerdo a los permisos
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    fetchDatosClima(it.latitude, it.longitude)
                    withContext(Dispatchers.Main) {
                        callback(clima)
                    }
                }
            }
        }
    }

    private suspend fun fetchDatosClima(latitud: Double, longitud: Double) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitud&longitude=$longitud&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,cloud_cover,wind_speed_10m&forecast_days=$dias"
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.string()?.let {
                actualizarDatosDesdeJson(JSONObject(it))
            }
        }
    }

    private fun actualizarDatosDesdeJson(json: JSONObject) {
        val currentData = json.getJSONObject("current")
        clima.temperatura = currentData.optDouble("temperature_2m", Double.NaN)
        clima.humedad = currentData.optInt("relative_humidity_2m", -1)
        clima.aparenteTemperatura = currentData.optDouble("apparent_temperature", Double.NaN)
        clima.precipitacion = currentData.optDouble("precipitation", Double.NaN)
        clima.nubosidad = currentData.optInt("cloud_cover", -1)
        clima.velocidadViento = currentData.optDouble("wind_speed_10m", Double.NaN)
    }

    suspend fun updateMap(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Habilitar la capa de ubicación para mostrar el punto azul
            map.isMyLocationEnabled = true

            // Obtener la última ubicación y centrar el mapa en ella
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val ubicacionActual = LatLng(it.latitude, it.longitude)
                    map.addMarker(MarkerOptions().position(ubicacionActual).title("Mi Ubicación"))
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
                }
            }
        }
    }


    fun navigateToForecast(activity: ClimaActivity) {
        val intent = Intent(activity, ForecastsActivity::class.java).apply {
            putExtra("temperaturas", clima.temperaturas.toDoubleArray())
            putExtra("humedades", clima.humedades.toIntArray())
            putExtra("probabilidadesPrecipitacion", clima.probabilidadesPrecipitacion.toIntArray())
            putExtra("precipitaciones", clima.precipitaciones.toDoubleArray())
            putExtra("evapotranspiraciones", clima.evapotranspiraciones.toDoubleArray())
            putExtra("velocidadesViento", clima.velocidadesViento.toDoubleArray())
            putExtra("humedadesSuelo", clima.humedadesSuelo.toDoubleArray())
            putStringArrayListExtra("times", ArrayList(clima.times))
        }
        activity.startActivity(intent)
    }
}
