package com.example.siembrasmart.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

class DialogHelper {

    fun mostrarDialogoAyuda(context: Context, titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}
