package com.example.siembrasmart.views

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.R
import com.example.siembrasmart.controllers.NewsController
import com.example.siembrasmart.databinding.ActivityNewsBinding
import com.example.siembrasmart.utils.Navigation
import com.google.firebase.auth.FirebaseAuth

class NewsActivity : Navigation() {

    private lateinit var binding: ActivityNewsBinding
    private lateinit var webView: WebView
    private lateinit var controlador: NewsController
    private var cultivoSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar el WebView
        webView = binding.webview

        // Inicializar el controlador
        controlador = NewsController()

        // Configurar el WebView mediante el controlador
        controlador.configurarWebView(webView)

        // Configurar el menú desplegable (Spinner) con modelos del usuario
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            configurarSpinner(userId)
        }

        // Manejar el Switch para filtrar solo noticias de Colombia
        binding.switchColombia.setOnCheckedChangeListener { _, _ ->
            if (cultivoSeleccionado.isNotEmpty()) {
                cargarNoticias()
            }
        }

        // Navegación inferior
        binding.bottomNavigation.selectedItemId = R.id.navigation_noticias
        setupBottomNavigationView(binding.bottomNavigation)

        // Configuración de la Toolbar
        setupToolbar(binding.toolbar)
    }

    private fun configurarSpinner(userId: String) {
        val spinner: Spinner = binding.spinnerCrop

        // Configurar el spinner a través del controlador
        controlador.configurarSpinner(
            userId,
            spinner,
            onItemSelected = { cultivo ->
                cultivoSeleccionado = cultivo.lowercase()
                cargarNoticias()
            }
        )
    }

    private fun cargarNoticias() {
        val soloColombia = binding.switchColombia.isChecked
        val url = controlador.generarUrlParaNoticias(cultivoSeleccionado, soloColombia)
        controlador.cargarUrlEnWebView(webView, url)
    }
}
