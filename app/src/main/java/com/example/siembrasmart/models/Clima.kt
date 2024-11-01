package com.example.siembrasmart.models

class Clima {
    var temperatura: Double = 0.0
    var humedad: Int = 0
    var aparenteTemperatura: Double = 0.0
    var precipitacion: Double = 0.0
    var nubosidad: Int = 0
    var velocidadViento: Double = 0.0

    var temperaturas: MutableList<Double> = mutableListOf()
    var humedades: MutableList<Int> = mutableListOf()
    var probabilidadesPrecipitacion: MutableList<Int> = mutableListOf()
    var precipitaciones: MutableList<Double> = mutableListOf()
    var evapotranspiraciones: MutableList<Double> = mutableListOf()
    var velocidadesViento: MutableList<Double> = mutableListOf()
    var humedadesSuelo: MutableList<Double> = mutableListOf()
    var times: MutableList<String> = mutableListOf()
}
