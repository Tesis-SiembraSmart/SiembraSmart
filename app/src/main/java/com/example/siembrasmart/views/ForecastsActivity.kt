package com.example.siembrasmart.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.controllers.ForecastController
import com.example.siembrasmart.databinding.ActivityForecastsBinding
import com.example.siembrasmart.models.Forecast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import android.widget.ImageButton

class ForecastsActivity : AppCompatActivity() {
    private lateinit var forecastController: ForecastController
    private lateinit var binding: ActivityForecastsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout usando ViewBinding
        binding = ActivityForecastsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Configurar los gráficos
        val graficoTemperatura: LineChart = binding.graficoTemperatura
        val graficoHumedad: LineChart = binding.graficoHumedad
        val graficoProbabilidadPrecipitacion: LineChart = binding.graficoProbabilidadPrecipitacion
        val graficoPrecipitacion: LineChart = binding.graficoPrecipitacion
        val graficoEvapotranspiracion: LineChart = binding.graficoEvapotranspiracion
        val graficoVelocidadViento: LineChart = binding.graficoVelocidadViento
        val graficoHumedadSuelo: LineChart = binding.graficoHumedadSuelo

        // Obtener los datos del intent
        val forecast = Forecast(
            temperaturas = intent.getDoubleArrayExtra("temperaturas")?.toList() ?: listOf(),
            humedades = intent.getIntArrayExtra("humedades")?.toList() ?: listOf(),
            probabilidadesPrecipitacion = intent.getIntArrayExtra("probabilidadesPrecipitacion")?.toList() ?: listOf(),
            precipitaciones = intent.getDoubleArrayExtra("precipitaciones")?.toList() ?: listOf(),
            evapotranspiraciones = intent.getDoubleArrayExtra("evapotranspiraciones")?.toList() ?: listOf(),
            velocidadesViento = intent.getDoubleArrayExtra("velocidadesViento")?.toList() ?: listOf(),
            humedadesSuelo = intent.getDoubleArrayExtra("humedadesSuelo")?.toList() ?: listOf(),
            tiempos = intent.getStringArrayListExtra("times") ?: listOf()
        )

        forecastController = ForecastController()

        // Crear los gráficos
        crearGraficos(forecast, graficoTemperatura, graficoHumedad, graficoProbabilidadPrecipitacion, graficoPrecipitacion, graficoEvapotranspiracion, graficoVelocidadViento, graficoHumedadSuelo)
        // Configurar la barra superior
        binding.topAppBar.setNavigationOnClickListener {
            // Acción al hacer clic en el ícono de navegación (botón atrás)
            val intent = Intent(this, ClimaActivity::class.java)
            startActivity(intent)
        }
        // En tu onCreate o donde configures los gráficos
        configurarBotonesDeZoom(binding.graficoTemperatura, binding.buttonZoomInTemperatura, binding.buttonZoomOutTemperatura)
        configurarBotonesDeZoom(binding.graficoHumedad, binding.buttonZoomInHumedad, binding.buttonZoomOutHumedad)
        configurarBotonesDeZoom(binding.graficoProbabilidadPrecipitacion, binding.buttonZoomInProbabilidadPrecipitacion, binding.buttonZoomOutProbabilidadPrecipitacion)
        configurarBotonesDeZoom(binding.graficoPrecipitacion, binding.buttonZoomInPrecipitacion, binding.buttonZoomOutPrecipitacion)
        configurarBotonesDeZoom(binding.graficoEvapotranspiracion, binding.buttonZoomInEvapotranspiracion, binding.buttonZoomOutEvapotranspiracion)
        configurarBotonesDeZoom(binding.graficoVelocidadViento, binding.buttonZoomInVelocidadViento, binding.buttonZoomOutVelocidadViento)
        configurarBotonesDeZoom(binding.graficoHumedadSuelo, binding.buttonZoomInHumedadSuelo, binding.buttonZoomOutHumedadSuelo)

    }

    private fun crearGraficos(forecast: Forecast, graficoTemperatura: LineChart, graficoHumedad: LineChart, graficoProbabilidadPrecipitacion: LineChart, graficoPrecipitacion: LineChart, graficoEvapotranspiracion: LineChart, graficoVelocidadViento: LineChart, graficoHumedadSuelo: LineChart) {
        val tiempos = forecast.tiempos

        forecastController.crearGrafico(
            entradas = forecast.temperaturas.mapIndexed { index, temp -> Entry(index.toFloat(), temp.toFloat()) },
            label = "Temperatura (°C)",
            tiempos = tiempos,
            grafico = graficoTemperatura,
            context = this

        )

        forecastController.crearGrafico(
            entradas = forecast.humedades.mapIndexed { index, humidity -> Entry(index.toFloat(), humidity.toFloat()) },
            label = "Humedad (%)",
            tiempos = tiempos,
            grafico = graficoHumedad,
            context = this
        )

        forecastController.crearGrafico(
            entradas = forecast.probabilidadesPrecipitacion.mapIndexed { index, prob -> Entry(index.toFloat(), prob.toFloat()) },
            label = "Probabilidad de Precipitación (%)",
            tiempos = tiempos,
            grafico = graficoProbabilidadPrecipitacion,
            context = this
        )

        forecastController.crearGrafico(
            entradas = forecast.precipitaciones.mapIndexed { index, precip -> Entry(index.toFloat(), precip.toFloat()) },
            label = "Precipitación (mm)",
            tiempos = tiempos,
            grafico = graficoPrecipitacion,
            context = this
        )

        forecastController.crearGrafico(
            entradas = forecast.evapotranspiraciones.mapIndexed { index, et -> Entry(index.toFloat(), et.toFloat()) },
            label = "Evapotranspiración (mm)",
            tiempos = tiempos,
            grafico = graficoEvapotranspiracion,
            context = this
        )

        forecastController.crearGrafico(
            entradas = forecast.velocidadesViento.mapIndexed { index, windSpeed -> Entry(index.toFloat(), windSpeed.toFloat()) },
            label = "Velocidad del Viento (km/h)",
            tiempos = tiempos,
            grafico = graficoVelocidadViento,
            context = this
        )

        forecastController.crearGrafico(
            entradas = forecast.humedadesSuelo.mapIndexed { index, soilMoisture -> Entry(index.toFloat(), soilMoisture.toFloat()) },
            label = "Humedad del Suelo (m³/m³)",
            tiempos = tiempos,
            grafico = graficoHumedadSuelo,
            context = this
        )
    }
    // Función para configurar los botones de zoom para un gráfico específico
    fun configurarBotonesDeZoom(grafico: LineChart, buttonZoomIn: ImageButton, buttonZoomOut: ImageButton) {
        buttonZoomIn.setOnClickListener {
            grafico.zoomIn() // Realiza el zoom in en el gráfico
        }

        buttonZoomOut.setOnClickListener {
            grafico.zoomOut() // Realiza el zoom out en el gráfico
        }
    }

}
