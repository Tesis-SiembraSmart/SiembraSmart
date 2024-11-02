package com.example.siembrasmart.controllers

import android.content.Context
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class NewsController {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    // Configurar el WebView
    fun configurarWebView(webView: WebView) {
        webView.webViewClient = WebViewClient()
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
    }

    // Configurar el Spinner para obtener modelos del usuario y extraer solo la segunda palabra
    fun configurarSpinner(
        userId: String,
        spinner: Spinner,
        onItemSelected: (String) -> Unit
    ) {
        database.child("users").child(userId).child("modelosUsados").get().addOnSuccessListener { dataSnapshot ->
            val modelosUsados = dataSnapshot.children.mapNotNull { it.getValue(String::class.java) }
            val cultivos = modelosUsados.map { modelo ->
                modelo.split(" ").getOrElse(1) { modelo }
            }

            // Crear un ArrayAdapter usando el listado de cultivos extraídos
            val context: Context = spinner.context
            val adaptador = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                cultivos
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            spinner.adapter = adaptador

            // Establecer el listener para el Spinner
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    onItemSelected(cultivos[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // No se necesita acción aquí
                }
            }
        }
    }

    // Generar la URL según el cultivo seleccionado y el filtro de país
    fun generarUrlParaNoticias(cultivo: String, soloColombia: Boolean): String {
        val urlBase = "https://news.google.com/search?q=$cultivo"
        return if (soloColombia) {
            "$urlBase%20colombia&hl=es-419&gl=CO&ceid=CO%3Aes-419"
        } else {
            "$urlBase&hl=es-419&gl=CO&ceid=CO%3Aes-419"
        }
    }

    // Cargar la URL en el WebView
    fun cargarUrlEnWebView(webView: WebView, url: String) {
        webView.loadUrl(url)
    }
}
