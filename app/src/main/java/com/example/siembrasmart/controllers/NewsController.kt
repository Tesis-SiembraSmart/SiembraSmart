package com.example.siembrasmart.controllers

import android.webkit.WebView

class NewsController {

    // Método para generar la URL según el cultivo seleccionado y el filtro de país
    fun generarUrlParaNoticias(cultivo: String, soloColombia: Boolean): String {
        val urlBase = "https://news.google.com/search?q=$cultivo"
        return if (soloColombia) {
            "$urlBase%20colombia&hl=es-419&gl=CO&ceid=CO%3Aes-419"
        } else {
            "$urlBase&hl=es-419&gl=CO&ceid=CO%3Aes-419"
        }
    }

    // Método para cargar la URL en el WebView
    fun cargarUrlEnWebView(webView: WebView, url: String) {
        webView.loadUrl(url)
    }
}
