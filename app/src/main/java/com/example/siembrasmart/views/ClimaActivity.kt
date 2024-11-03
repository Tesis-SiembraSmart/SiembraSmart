package com.example.siembrasmart.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.siembrasmart.R
import com.example.siembrasmart.controllers.ClimaController
import com.example.siembrasmart.databinding.ActivityClimaBinding
import com.example.siembrasmart.models.Clima
import com.example.siembrasmart.utils.Navigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClimaActivity : Navigation(), OnMapReadyCallback {
    private lateinit var auth: FirebaseAuth
    private lateinit var climaController: ClimaController
    private lateinit var binding: ActivityClimaBinding
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClimaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        climaController = ClimaController(applicationContext)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.pronostico.setOnClickListener {
            climaController.navigateToForecast(this)
        }

        // Actualizar interfaz cuando se obtienen datos
        CoroutineScope(Dispatchers.Main).launch {
            climaController.obtenerDatosClima { clima ->
                actualizarInterfaz(clima)
            }
        }

        setupBottomNavigationView(binding.bottomNavigation)
        setupToolbar(binding.toolbar)
    }

    @SuppressLint("StringFormatMatches")
    private fun actualizarInterfaz(clima: Clima) {
        binding.temperatureTextView.text = getString(R.string.temperature_text, clima.temperatura)
        binding.humidityTextView.text = getString(R.string.humidity_text, clima.humedad)
        binding.precipitationTextView.text = getString(R.string.precipitation_text, clima.precipitacion)
        binding.windSpeedTextView.text = getString(R.string.wind_speed_text, clima.aparenteTemperatura)
        binding.cloudCoverTextView.text = getString(R.string.cloud_cover_text, clima.nubosidad)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            climaController.updateMap(map)
        }
    }
}
