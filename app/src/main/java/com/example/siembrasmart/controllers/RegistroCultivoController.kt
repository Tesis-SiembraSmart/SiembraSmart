package com.example.siembrasmart.controllers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import com.example.siembrasmart.R
import com.example.siembrasmart.models.Cultivo
import com.example.siembrasmart.models.Usuario
import com.example.siembrasmart.views.RegistroCultivoActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

class RegistroCultivoController(private val context: Context) {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Método para inicializar el FusedLocationProviderClient
    fun initializeLocationProvider() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun setupCultivoSpinner(spinner: Spinner) {
        val adapter = ArrayAdapter.createFromResource(
            context,
            R.array.cultivos_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    fun obtenerUbicacionActual(callback: (Double, Double) -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as RegistroCultivoActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                callback(it.latitude, it.longitude)
            } ?: run {
                callback(0.0, 0.0) // Valores por defecto si no se obtiene la ubicación
            }
        }
    }

    fun saveCultivoForCurrentUser(cultivo: Cultivo, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            callback(false)
            return
        }

        val userRef = database.child("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val usuario = snapshot.getValue<Usuario>() ?: Usuario()

            val updatedCultivos = usuario.cultivos.toMutableList()
            updatedCultivos.add(cultivo)
            usuario.cultivos = updatedCultivos

            userRef.setValue(usuario)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        }.addOnFailureListener {
            callback(false)
        }
    }
}
