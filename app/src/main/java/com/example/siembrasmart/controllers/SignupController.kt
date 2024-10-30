package com.example.siembrasmart.controllers

import com.example.siembrasmart.models.Usuario
import com.example.siembrasmart.views.SignupActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignupController(
    private val auth: FirebaseAuth,
    private val database: DatabaseReference
) {

    fun createAccount(email: String, password: String, firstName: String, lastName: String, phoneNumber: String, activity: SignupActivity) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val user = Usuario(
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = phoneNumber,
                        email = email,
                        cultivos = emptyList()

                    )
                    saveUserToDatabase(userId, user, activity)
                } else {
                    activity.onAccountCreationFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }

    private fun saveUserToDatabase(userId: String, user: Usuario, activity: SignupActivity) {
        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                activity.onUserSaveSuccess()
                activity.onAccountCreationSuccess()
            }
            .addOnFailureListener { e ->
                activity.onUserSaveFailure(e.message ?: "Unknown error")
            }
    }
}
