package com.example.siembrasmart.controllers

import android.content.Context
import android.util.Log
import com.example.siembrasmart.R
import com.example.siembrasmart.models.Forecast
import com.example.siembrasmart.utils.CustomMarkerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

class ForecastController {
    // Nueva función para rellenar valores nulos en el Forecast
    fun fillNullValues(forecast: Forecast, fillValue: Double): Forecast {
        return Forecast(
            temperaturas = forecast.temperaturas.map { it?.takeUnless { it.isNaN() } ?: fillValue },
            humedades = forecast.humedades.map { it ?: fillValue.toInt() },
            probabilidadesPrecipitacion = forecast.probabilidadesPrecipitacion.map { it ?: fillValue.toInt() },
            precipitaciones = forecast.precipitaciones.map { it?.takeUnless { it.isNaN() } ?: fillValue },
            evapotranspiraciones = forecast.evapotranspiraciones.map { it?.takeUnless { it.isNaN() } ?: fillValue },
            velocidadesViento = forecast.velocidadesViento.map { it?.takeUnless { it.isNaN() } ?: fillValue },
            humedadesSuelo = forecast.humedadesSuelo.map { it?.takeUnless { it.isNaN() } ?: fillValue },
            tiempos = forecast.tiempos // Suponiendo que `tiempos` no contiene `null` o `NaN`
        )
    }

    // Function to trim each list in Forecast to the length of valid data with debug logs
    fun trimForecastData(forecast: Forecast): Forecast {

        // Encuentra el índice máximo válido en todas las listas
        val maxValidIndex = listOf(
            forecast.temperaturas.indexOfLast { it != null && it != 0.0 },
            forecast.humedades.indexOfLast { it != null && it != 0 },
            forecast.probabilidadesPrecipitacion.indexOfLast { it != null && it != 0 },
            forecast.precipitaciones.indexOfLast { it != null && it != 0.0 },
            forecast.evapotranspiraciones.indexOfLast { it != null && it != 0.0 },
            forecast.velocidadesViento.indexOfLast { it != null && it != 0.0 },
            forecast.humedadesSuelo.indexOfLast { it != null && it != 0.0 }
        ).maxOrNull() ?: -1

        val validLength = maxValidIndex + 1
        val trimmedForecast = Forecast(
            temperaturas = forecast.temperaturas.take(validLength),
            humedades = forecast.humedades.take(validLength),
            probabilidadesPrecipitacion = forecast.probabilidadesPrecipitacion.take(validLength),
            precipitaciones = forecast.precipitaciones.take(validLength),
            evapotranspiraciones = forecast.evapotranspiraciones.take(validLength),
            velocidadesViento = forecast.velocidadesViento.take(validLength),
            humedadesSuelo = forecast.humedadesSuelo.take(validLength),
            tiempos = forecast.tiempos.take(validLength)
        )

        return trimmedForecast
    }

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

        // Custom marker
        val markerView = CustomMarkerView(context, R.layout.marker_view, tiempos.map { Pair(it, 0.0) }, label)
        grafico.marker = markerView
    }
    fun crearGrafico(entradas: List<Entry>, label: String, tiempos: List<String>, grafico: LineChart, context: Context) {
        Log.d("ForecastController", "Entradas: $entradas")
        Log.d("ForecastController", "Label: $label")
        Log.d("ForecastController", "Tiempos: $tiempos")

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


    inner class FormateadorEjeTiempo(private val tiempos: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt().coerceIn(0, tiempos.size - 1)
            val partesTiempo = tiempos[index].split("T")
            return if (partesTiempo.size > 1) {
                "${partesTiempo[0]} ${partesTiempo[1].substring(0, 5)}" // Display date and time in HH:mm
            } else {
                tiempos[index] // Backup option to show full timestamp
            }
        }
    }
}
