package com.example.siembrasmart.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.siembrasmart.R
import com.example.siembrasmart.controllers.ClimaController
import com.example.siembrasmart.models.Clima
import com.example.siembrasmart.utils.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import kotlinx.coroutines.withContext
import com.example.siembrasmart.databinding.ActivityClimaBinding


class ClimaActivity : Navigation(), OnMapReadyCallback {
    private lateinit var auth: FirebaseAuth
    private lateinit var clima: Clima
    private lateinit var climaController: ClimaController

    private lateinit var binding: ActivityClimaBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClimaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clima = Clima()
        climaController = ClimaController(clima)

        auth = Firebase.auth

        // Elementos de la interfaz
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.pronostico.setOnClickListener {
            val intent = Intent(this, ForecastsActivity::class.java)
            intent.putExtra("temperaturas", clima.temperaturas.toDoubleArray())
            intent.putExtra("humedades", clima.humedades.toIntArray())
            intent.putExtra("probabilidadesPrecipitacion", clima.probabilidadesPrecipitacion.toIntArray())
            intent.putExtra("precipitaciones", clima.precipitaciones.toDoubleArray())
            intent.putExtra("evapotranspiraciones", clima.evapotranspiraciones.toDoubleArray())
            intent.putExtra("velocidadesViento", clima.velocidadesViento.toDoubleArray())
            intent.putExtra("humedadesSuelo", clima.humedadesSuelo.toDoubleArray())
            intent.putStringArrayListExtra("times", ArrayList(clima.times)) //evitar errores

            println(clima.times.toTypedArray())

            startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Navegación inferior
        binding.bottomNavigation.selectedItemId = R.id.navigation_clima
        setupBottomNavigationView(binding.bottomNavigation)

        // Configuración de la Toolbar
        setupToolbar(binding.toolbar)

        obtenerDatosClima()
        ajustarWindowInsets()
    }

    private fun obtenerDatosClima() {
        CoroutineScope(Dispatchers.Main).launch {
            obtenerUbicacionActual()
        }
    }

    private suspend fun obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1000
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latitud = it.latitude
                val longitud = it.longitude
                CoroutineScope(Dispatchers.Main).launch {
                    climaController.fetchDatosClima(latitud, longitud)
                    actualizarInterfaz()
                    actualizarMapa(latitud, longitud)
                }
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private suspend fun actualizarInterfaz() = withContext(Dispatchers.Main) {
        binding.temperatureTextView.text = getString(R.string.temperature_text, clima.temperatura)
        binding.humidityTextView.text = getString(R.string.humidity_text, clima.humedad)
        binding.precipitationTextView.text = getString(R.string.precipitation_text, clima.precipitacion)
        binding.windSpeedTextView.text = getString(R.string.wind_speed_text, clima.aparenteTemperatura)
        binding.cloudCoverTextView.text = getString(R.string.cloud_cover_text, clima.nubosidad)
    }

    private fun actualizarMapa(latitud: Double, longitud: Double) {
        val ubicacion = LatLng(latitud, longitud)
        map.addMarker(MarkerOptions().position(ubicacion).title("Ubicación actual"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 12f))
    }

    private fun ajustarWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isCompassEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val ubicacionActual = LatLng(it.latitude, it.longitude)
                // Actualizar el mapa con la ubicación en vivo
                map.addMarker(MarkerOptions().position(ubicacionActual).title("Mi Ubicación"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))

                // Llamar a la API del clima con la ubicación actual
                CoroutineScope(Dispatchers.IO).launch {
                    climaController.fetchDatosClima(it.latitude, it.longitude)
                    withContext(Dispatchers.Main) {
                        actualizarInterfaz()
                    }
                }
            }
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permisos concedidos, habilitar la capa de ubicación
            map.isMyLocationEnabled = true

            // Mover la cámara a la ubicación actual
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val ubicacionActual = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CoroutineScope(Dispatchers.Main).launch {
                obtenerUbicacionActual()
            }
        }
    }
}