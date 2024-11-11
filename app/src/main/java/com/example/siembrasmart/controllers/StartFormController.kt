package com.example.siembrasmart.controllers


import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

import com.google.firebase.database.GenericTypeIndicator

class StartFormController {

    private val database = Firebase.database.reference

    fun getSelectedModels(userId: String, callback: (List<String>?) -> Unit) {
        database.child("users").child(userId).child("modelosUsados").get()
            .addOnSuccessListener { snapshot ->
                // Use GenericTypeIndicator to properly handle List<String> type
                val typeIndicator = object : GenericTypeIndicator<List<String>>() {}
                val selectedModels: List<String>? = snapshot.getValue(typeIndicator)
                callback(selectedModels)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun saveSelectedModels(userId: String, selectedModels: List<String>, callback: (Boolean) -> Unit) {
        database.child("users").child(userId).child("modelosUsados").setValue(selectedModels)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}



