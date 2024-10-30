package com.example.siembrasmart.controllers

import com.example.siembrasmart.models.Cultivo
import com.example.siembrasmart.models.Usuario
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.getValue

class RegistroCultivoController {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun saveCultivoForCurrentUser(cultivo: Cultivo, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            callback(false)
            return
        }

        val userRef = database.child("users").child(userId)

        // Fetch the existing usuario data
        userRef.get().addOnSuccessListener { snapshot ->
            val usuario = snapshot.getValue<Usuario>() ?: Usuario()

            // Update the cultivos list
            val updatedCultivos = usuario.cultivos.toMutableList()
            updatedCultivos.add(cultivo)

            // Save the updated Usuario object back to Firebase
            usuario.cultivos = updatedCultivos
            userRef.setValue(usuario)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        }.addOnFailureListener {
            callback(false)
        }
    }
}
