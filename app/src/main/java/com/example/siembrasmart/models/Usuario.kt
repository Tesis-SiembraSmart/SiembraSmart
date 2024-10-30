package com.example.siembrasmart.models

data class Usuario(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var modeloUsado: String = "",
    var cultivos: List<Cultivo> = emptyList()
)
