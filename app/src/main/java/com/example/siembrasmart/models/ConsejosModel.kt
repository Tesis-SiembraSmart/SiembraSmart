package com.example.siembrasmart.models

data class ConsejosModel(
    var areaSembrada: Int = 0,
    var areaCosechada: Int = 0,
    var produccion: Int = 0,
    var rendimientoPredicho: Double = Double.NaN
)