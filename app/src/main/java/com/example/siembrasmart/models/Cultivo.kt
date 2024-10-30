package com.example.siembrasmart.models

import java.util.Date

data class Cultivo (
    var nombre: String = "",
    var tipo: String = "",
    var fechaInicio: Date = Date(),
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var area: Double = 0.0,
    var id: String = ""
)
