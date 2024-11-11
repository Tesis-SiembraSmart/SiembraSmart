package com.example.siembrasmart.views

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.R
import com.example.siembrasmart.controllers.RegistroCultivoController
import com.example.siembrasmart.databinding.ActivityRegistroCultivoBinding
import com.example.siembrasmart.models.Cultivo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegistroCultivoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroCultivoBinding
    private val controller = RegistroCultivoController(this)
    private var selectedDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroCultivoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar el proveedor de ubicación en el controlador
        controller.initializeLocationProvider()

        // Configurar el Spinner desde el controlador
        controller.setupCultivoSpinner(binding.nombreCultivoSpinner)

        // Configurar el listener para el Spinner
        binding.nombreCultivoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val nombre = binding.nombreCultivoSpinner.selectedItem.toString()
                val tipo = controller.detectarTipoCultivo(nombre) // Detectar tipo automáticamente
                binding.tipoCultivoTextView.text = tipo // Actualizar el TextView con el tipo
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada si no hay selección
            }
        }

        // Obtener ubicación desde el controlador y actualizar la UI
        controller.obtenerUbicacionActual { latitud, longitud ->
            binding.latitudCultivo.text = latitud.toString()
            binding.longitudCultivo.text = longitud.toString()
        }

        binding.fechaInicioButton.setOnClickListener {
            showDatePickerDialog()
        }

        binding.guardarButton.setOnClickListener {
            saveCultivo()
        }
        binding.noGuardar.setOnClickListener {
            val intent = Intent(this, ClimaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate!!)
                binding.fechaInicioButton.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveCultivo() {
        val nombre = binding.nombreCultivoSpinner.selectedItem.toString()
        val tipo = binding.tipoCultivoTextView.text.toString() // Obtener el tipo del TextView actualizado
        val latitud = binding.latitudCultivo.text.toString().toDoubleOrNull() ?: 0.0
        val longitud = binding.longitudCultivo.text.toString().toDoubleOrNull() ?: 0.0
        val area = binding.areaCultivo.text.toString().toDoubleOrNull() ?: 0.0
        val fechaInicio = selectedDate ?: Date()

        val cultivo = Cultivo(
            nombre = nombre,
            tipo = tipo, // Asigna el tipo detectado automáticamente
            fechaInicio = fechaInicio,
            latitud = latitud,
            longitud = longitud,
            area = area,
        )

        controller.saveCultivoForCurrentUser(cultivo) { success ->
            if (success) {
                Toast.makeText(this, "Cultivo guardado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al guardar cultivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
