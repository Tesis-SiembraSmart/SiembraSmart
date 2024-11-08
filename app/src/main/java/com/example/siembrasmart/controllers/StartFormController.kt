package com.example.siembrasmart.controllers

import android.util.Log
import com.example.siembrasmart.models.Usuario
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class StartFormController {

    private val database = Firebase.database.reference

    fun saveSelectedModels(userId: String, selectedModels: List<String>, callback: (Boolean) -> Unit) {
        // Actualiza el campo modeloUsado del usuario para almacenar múltiples modelos
        database.child("users").child(userId).child("modelosUsados").setValue(selectedModels)
            .addOnSuccessListener {
                // Llamar al callback con true si el guardado fue exitoso
                callback(true)
            }
            .addOnFailureListener {
                // Llamar al callback con false si hubo un error
                callback(false)
            }
    }

}
