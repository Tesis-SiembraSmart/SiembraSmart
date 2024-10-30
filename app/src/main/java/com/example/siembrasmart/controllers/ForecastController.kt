package com.example.siembrasmart.controllers

import android.content.Context
import com.example.siembrasmart.R
import com.example.siembrasmart.utils.CustomMarkerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

class ForecastController {

    fun configurarGrafico(grafico: LineChart, context: Context, tiempos: List<String>, label: String) {
        grafico.setTouchEnabled(true)
        grafico.isDragEnabled = true
        grafico.setScaleEnabled(true)
        grafico.setPinchZoom(true)

        grafico.xAxis.enableGridDashedLine(10f, 10f, 0f)
        grafico.axisLeft.enableGridDashedLine(10f, 10f, 0f)
        grafico.xAxis.setDrawGridLines(true)
        grafico.axisLeft.setDrawGridLines(true)
        grafico.axisRight.setDrawGridLines(false)

        grafico.description.isEnabled = false
        grafico.legend.isEnabled = true

        grafico.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        grafico.xAxis.granularity = 1f
        grafico.xAxis.setAvoidFirstLastClipping(true)
        grafico.xAxis.labelRotationAngle = 80f

        // Agrega el marcador personalizado con la etiqueta correspondiente
        val markerView = CustomMarkerView(context, R.layout.marker_view, tiempos.map { Pair(it, 0.0) }, label)
        grafico.marker = markerView
    }

    fun crearGrafico(entradas: List<Entry>, label: String, tiempos: List<String>, grafico: LineChart, context: Context) {
        val conjuntoDatos = LineDataSet(entradas, label).apply {
            color = android.graphics.Color.BLUE
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
        }
        val datosLinea = LineData(conjuntoDatos)
        grafico.data = datosLinea

        configurarGrafico(grafico, context, tiempos, label)
        grafico.xAxis.valueFormatter = FormateadorEjeTiempo(tiempos)
        grafico.invalidate()
    }


    // Clase interna para formatear las etiquetas del eje X con fechas y horas
    inner class FormateadorEjeTiempo(private val tiempos: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt().coerceIn(0, tiempos.size - 1)
            val partesTiempo = tiempos[index].split("T")
            return if (partesTiempo.size > 1) {
                "${partesTiempo[0]} ${partesTiempo[1].substring(0, 5)}" // Muestra la fecha y hora en HH:mm
            } else {
                tiempos[index] // Como respaldo, retorna el timestamp completo
            }
        }
    }
}