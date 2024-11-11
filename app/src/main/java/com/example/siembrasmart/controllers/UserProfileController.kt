package com.example.siembrasmart.controllers

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.siembrasmart.MainActivity
import com.example.siembrasmart.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserProfileController(private val context: Context) {

    private val userRef = FirebaseDatabase.getInstance().reference.child("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun loadUserProfile(userId: String, onDataLoaded: (Usuario?) -> Unit) {
        userRef.child(userId).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(Usuario::class.java)
            onDataLoaded(user)
        }.addOnFailureListener {
            Toast.makeText(context, "Error al cargar el perfil.", Toast.LENGTH_SHORT).show()
            onDataLoaded(null)
        }
    }

    fun saveUserProfile(
        userId: String,
        updatedFirstName: String,
        updatedLastName: String,
        updatedPhoneNumber: String,
        onSaveComplete: (Boolean) -> Unit
    ) {
        if (updatedFirstName.isEmpty() || updatedLastName.isEmpty() || updatedPhoneNumber.isEmpty()) {
            Toast.makeText(context, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
            onSaveComplete(false)
            return
        }

        val updatedUserData = mapOf(
            "firstName" to updatedFirstName,
            "lastName" to updatedLastName,
            "phoneNumber" to updatedPhoneNumber,
        )

        userRef.child(userId).updateChildren(updatedUserData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Cambios guardados exitosamente.", Toast.LENGTH_SHORT).show()
                onSaveComplete(true)
            } else {
                Toast.makeText(context, "Error al guardar los cambios.", Toast.LENGTH_SHORT).show()
                onSaveComplete(false)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
