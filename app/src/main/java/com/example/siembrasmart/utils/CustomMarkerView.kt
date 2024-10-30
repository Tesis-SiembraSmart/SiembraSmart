package com.example.siembrasmart.utils

import android.content.Context
import android.widget.TextView
import com.example.siembrasmart.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Locale

class CustomMarkerView(
    context: Context,
    layoutResource: Int,
    private val data: List<Pair<String, Double>>, // Lista de datos con (fecha, valor)
    private val label: String // Etiqueta para el tipo de dato (ej. Temperatura, Humedad, etc.)
) : MarkerView(context, layoutResource) {

    private val dateTextView: TextView = findViewById(R.id.marker_date)
    private val valueTextView: TextView = findViewById(R.id.marker_value)

    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale("es", "ES"))
    private val outputDateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "ES"))

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        e?.let {
            val index = it.x.toInt().coerceIn(0, data.size - 1)
            val date = data[index].first
            val value = it.y

            val formattedDate = try {
                val parsedDate = inputDateFormat.parse(date)
                outputDateFormat.format(parsedDate)
            } catch (e: Exception) {
                date
            }

            dateTextView.text = "Fecha: $formattedDate"
            valueTextView.text = "${labelWithoutUnits(label)}: $value ${getUnitForLabel(label)}"
        }
    }

    // Method to extract the label without units (e.g., "Temperatura" from "Temperatura (°C)")
    private fun labelWithoutUnits(label: String): String {
        return label.substringBefore(" (")
    }

    // Method to extract the unit from the label (e.g., "°C" from "Temperatura (°C)")
    private fun getUnitForLabel(label: String): String {
        return when (label) {
            "Temperatura (°C)" -> "°C"
            "Humedad (%)" -> "%"
            "Probabilidad de Precipitación (%)" -> "%"
            "Precipitación (mm)" -> "mm"
            "Evapotranspiración (mm)" -> "mm"
            "Velocidad del Viento (km/h)" -> "km/h"
            "Humedad del Suelo (m³/m³)" -> "m³/m³"
            else -> ""
        }
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width).toFloat(), -height.toFloat())
    }
}
