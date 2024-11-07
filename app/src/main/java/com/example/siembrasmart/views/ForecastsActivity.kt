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
import com.example.siembrasmart.utils.mostrarDialogoAyuda

class ForecastsActivity : AppCompatActivity() {
    private lateinit var forecastController: ForecastController
    private lateinit var binding: ActivityForecastsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForecastsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        forecastController = ForecastController()

        // Obtener datos del intent y crear el objeto Forecast
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

        // Llamar a fillNullValues para rellenar los valores nulos
        val filledForecast = forecastController.fillNullValues(forecast, 0.0)

        // Luego, recortar los datos
        val trimmedForecast = forecastController.trimForecastData(filledForecast)

        // Crear gráficos con los datos recortados
        crearGraficos(trimmedForecast)

        binding.topAppBar.setNavigationOnClickListener {
            startActivity(Intent(this, ClimaActivity::class.java))
        }


        configurarBotonesDeZoom(binding.graficoTemperatura, binding.buttonZoomInTemperatura, binding.buttonZoomOutTemperatura)
        configurarBotonesDeZoom(binding.graficoHumedad, binding.buttonZoomInHumedad, binding.buttonZoomOutHumedad)
        configurarBotonesDeZoom(binding.graficoProbabilidadPrecipitacion, binding.buttonZoomInProbabilidadPrecipitacion, binding.buttonZoomOutProbabilidadPrecipitacion)
        configurarBotonesDeZoom(binding.graficoPrecipitacion, binding.buttonZoomInPrecipitacion, binding.buttonZoomOutPrecipitacion)
        configurarBotonesDeZoom(binding.graficoEvapotranspiracion, binding.buttonZoomInEvapotranspiracion, binding.buttonZoomOutEvapotranspiracion)
        configurarBotonesDeZoom(binding.graficoVelocidadViento, binding.buttonZoomInVelocidadViento, binding.buttonZoomOutVelocidadViento)
        configurarBotonesDeZoom(binding.graficoHumedadSuelo, binding.buttonZoomInHumedadSuelo, binding.buttonZoomOutHumedadSuelo)
        configurarBotonesAyuda()

    }

    private fun crearGraficos(forecast: Forecast) {
        val tiempos = forecast.tiempos
        forecastController.crearGrafico(
            forecast.temperaturas.mapIndexed { index, temp -> Entry(index.toFloat(), temp.toFloat()) },
            "Temperatura (°C)",
            tiempos,
            binding.graficoTemperatura,
            this
        )

        forecastController.crearGrafico(
            forecast.humedades.mapIndexed { index, humidity -> Entry(index.toFloat(), humidity.toFloat()) },
            "Humedad (%)",
            tiempos,
            binding.graficoHumedad,
            this
        )

        forecastController.crearGrafico(
            forecast.probabilidadesPrecipitacion.mapIndexed { index, prob -> Entry(index.toFloat(), prob.toFloat()) },
            "Probabilidad de Precipitación (%)",
            tiempos,
            binding.graficoProbabilidadPrecipitacion,
            this
        )

        forecastController.crearGrafico(
            forecast.precipitaciones.mapIndexed { index, precip -> Entry(index.toFloat(), precip.toFloat()) },
            "Precipitación (mm)",
            tiempos,
            binding.graficoPrecipitacion,
            this
        )

        forecastController.crearGrafico(
            forecast.evapotranspiraciones.mapIndexed { index, et -> Entry(index.toFloat(), et.toFloat()) },
            "Evapotranspiración (mm)",
            tiempos,
            binding.graficoEvapotranspiracion,
            this
        )

        forecastController.crearGrafico(
            forecast.velocidadesViento.mapIndexed { index, windSpeed -> Entry(index.toFloat(), windSpeed.toFloat()) },
            "Velocidad del Viento (km/h)",
            tiempos,
            binding.graficoVelocidadViento,
            this
        )

        forecastController.crearGrafico(
            forecast.humedadesSuelo.mapIndexed { index, soilMoisture -> Entry(index.toFloat(), soilMoisture.toFloat()) },
            "Humedad del Suelo (m³/m³)",
            tiempos,
            binding.graficoHumedadSuelo,
            this
        )
    }

    private fun configurarBotonesDeZoom(grafico: LineChart, buttonZoomIn: ImageButton, buttonZoomOut: ImageButton) {
        buttonZoomIn.setOnClickListener { grafico.zoomIn() }
        buttonZoomOut.setOnClickListener { grafico.zoomOut() }
    }

    private fun configurarBotonesAyuda() {
        binding.buttonHelpTemperatura.setOnClickListener {
            mostrarDialogoAyuda(
                this,
                "Gráfico de Temperatura",
                "Este gráfico muestra la temperatura diaria en grados Celsius, permitiendo visualizar las variaciones a lo largo del tiempo."
            )
        }

        binding.buttonHelpHumedad.setOnClickListener {
            mostrarDialogoAyuda(
                this,
                "Gráfico de Humedad",
                "Este gráfico muestra el porcentaje de humedad relativa diaria, útil para entender las condiciones de humedad del aire que pueden afectar el cultivo."
            )
        }

        binding.buttonHelpProbabilidadPrecipitacion.setOnClickListener {
            mostrarDialogoAyuda(
                this,
                "Gráfico de Probabilidad de Precipitación",
                "Este gráfico muestra la probabilidad de precipitación en porcentaje, lo cual es clave para planificar el riego de los cultivos."
            )
        }

        binding.buttonHelpPrecipitacion.setOnClickListener {
            mostrarDialogoAyuda(
                this,
                "Gráfico de Precipitación",
                "Este gráfico muestra la cantidad de precipitación en milímetros (mm) por día, útil para el monitoreo del suministro de agua natural."
            )
        }

        binding.buttonHelpEvapotranspiracion.setOnClickListener {
            mostrarDialogoAyuda(
                this,
                "Gráfico de Evapotranspiración",
                "Este gráfico muestra la evapotranspiración en milímetros (mm) por día, indicando cuánta agua pierde el suelo debido a la evaporación y la transpiración de las plantas."
            )
        }

        binding.buttonHelpVelocidadViento.setOnClickListener {
            mostrarDialogoAyuda(
                this,
                "Gráfico de Velocidad del Viento",
                "Este gráfico muestra la velocidad del viento diaria en km/h, lo cual puede afectar la dispersión de semillas y el riesgo de erosión del suelo."
            )
        }

        binding.buttonHelpHumedadSuelo.setOnClickListener {
            mostrarDialogoAyuda(
                this,
                "Gráfico de Humedad del Suelo",
                "Este gráfico muestra la humedad del suelo en m³/m³, importante para evaluar la disponibilidad de agua en el suelo para las raíces de los cultivos."
            )
        }
    }
}
