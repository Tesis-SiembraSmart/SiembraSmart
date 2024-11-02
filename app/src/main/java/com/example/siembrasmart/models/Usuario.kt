package com.example.siembrasmart.models

data class Usuario(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var modelosUsados: List<String> = emptyList(),
    var cultivos: List<Cultivo> = emptyList()
)
