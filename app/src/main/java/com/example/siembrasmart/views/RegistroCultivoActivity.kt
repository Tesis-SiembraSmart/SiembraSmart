package com.example.siembrasmart.views

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.controllers.RegistroCultivoController
import com.example.siembrasmart.databinding.ActivityRegistroCultivoBinding
import com.example.siembrasmart.models.Cultivo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

class RegistroCultivoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroCultivoBinding
    private val controller = RegistroCultivoController()
    private var selectedDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroCultivoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fechaInicioButton.setOnClickListener {
            showDatePickerDialog()
        }

        binding.guardarButton.setOnClickListener {
            saveCultivo()
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
        val nombre = binding.nombreCultivo.text.toString()
        val tipo = binding.tipoCultivo.text.toString()
        val latitud = binding.latitudCultivo.text.toString().toDoubleOrNull() ?: 0.0
        val longitud = binding.longitudCultivo.text.toString().toDoubleOrNull() ?: 0.0
        val area = binding.areaCultivo.text.toString().toDoubleOrNull() ?: 0.0
        val fechaInicio = selectedDate ?: Date()
        val idCultivo = binding.idCultivo.text.toString()

        val cultivo = Cultivo(
            nombre = nombre,
            tipo = tipo,
            fechaInicio = fechaInicio,
            latitud = latitud,
            longitud = longitud,
            area = area,
            id = idCultivo
        )

        controller.saveCultivoForCurrentUser(cultivo) { success ->
            if (success) {
                Toast.makeText(this, "Cultivo guardado con éxito", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ClimaActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error al guardar cultivo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
