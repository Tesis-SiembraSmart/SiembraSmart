package com.example.siembrasmart.views

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.R
import com.example.siembrasmart.controllers.NewsController
import com.example.siembrasmart.databinding.ActivityNewsBinding
import com.example.siembrasmart.utils.Navigation

class NewsActivity : Navigation() {

    private lateinit var binding: ActivityNewsBinding
    private lateinit var webView: WebView
    private lateinit var controlador: NewsController // Referencia al controlador
    private var cultivoSeleccionado: String = "cacao" // Cultivo por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar el WebView
        webView = binding.webview
        webView.webViewClient = WebViewClient()
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Inicializar el controlador
        controlador = NewsController()

        // Configurar el menú desplegable (Spinner)
        configurarSpinner()

        // Cargar el cultivo por defecto "Cacao" y la URL correspondiente
        cargarNoticias()

        // Manejar el Switch para filtrar solo noticias de Colombia
        binding.switchColombia.setOnCheckedChangeListener { _, _ ->
            cargarNoticias() // Recargar noticias cuando el switch se cambie
        }
        // Navegación inferior
        binding.bottomNavigation.selectedItemId = R.id.navigation_noticias
        setupBottomNavigationView(binding.bottomNavigation)

        // Configuración de la Toolbar
        setupToolbar(binding.toolbar)
    }

    private fun configurarSpinner() {
        // Elemento Spinner
        val spinner: Spinner = binding.spinnerCrop

        // Crear un ArrayAdapter usando un diseño simple para el spinner y el arreglo de cultivos
        ArrayAdapter.createFromResource(
            this,
            R.array.cultivos_array,  // Asegúrate de que este array exista en strings.xml
            android.R.layout.simple_spinner_item
        ).also { adaptador ->
            // Especificar el diseño a usar cuando aparezca la lista de opciones
            adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Aplicar el adaptador al spinner
            spinner.adapter = adaptador
        }

        // Establecer un listener para el Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Obtener el elemento seleccionado
                cultivoSeleccionado = parent.getItemAtPosition(position).toString().lowercase()
                cargarNoticias() // Cargar noticias según el cultivo seleccionado
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Selección por defecto a "Cacao"
                cultivoSeleccionado = "cacao"
            }
        }
    }

    // Método para cargar las noticias según el cultivo seleccionado y el estado del switch
    private fun cargarNoticias() {
        val soloColombia = binding.switchColombia.isChecked
        // Generar la URL usando el controlador
        val url = controlador.generarUrlParaNoticias(cultivoSeleccionado, soloColombia)
        // Cargar la URL en el WebView usando el controlador
        controlador.cargarUrlEnWebView(webView, url)
    }
}
