package com.example.siembrasmart.models

data class Forecast(
    val temperaturas: List<Double>,
    val humedades: List<Int>,
    val probabilidadesPrecipitacion: List<Int>,
    val precipitaciones: List<Double>,
    val evapotranspiraciones: List<Double>,
    val velocidadesViento: List<Double>,
    val humedadesSuelo: List<Double>,
    val tiempos: List<String>
)