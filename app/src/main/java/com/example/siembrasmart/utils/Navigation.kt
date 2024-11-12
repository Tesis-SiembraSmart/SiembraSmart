package com.example.siembrasmart.utils

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.siembrasmart.R
import com.example.siembrasmart.views.AlertActivity
import com.example.siembrasmart.views.ClimaActivity
import com.example.siembrasmart.views.ConsejosActivity
import com.example.siembrasmart.views.NewsActivity
import com.example.siembrasmart.views.UserProfileActivity
import com.example.siembrasmart.views.StartFormActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class Navigation : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // Método para configurar el BottomNavigationView
    protected fun setupBottomNavigationView(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_noticias -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_clima -> {
                    startActivity(Intent(this, ClimaActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_modelo -> {
                    startActivity(Intent(this, ConsejosActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_mas -> {
                    showBottomDialog()
                    true
                }
                R.id.navigation_notificacion -> {
                    startActivity(Intent(this, AlertActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // Método para configurar la Toolbar
    protected fun setupToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_user -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_user -> {
                Toast.makeText(this, "Usuario", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.mostrarGuia -> {

                Toast.makeText(this, "Contenido Mostrado", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)        }
    }

    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheetlayout)

        val cancelButton: ImageView = dialog.findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        val layoutRegistrarCultivo: View = dialog.findViewById(R.id.layoutRegistrarCultivo)
        layoutRegistrarCultivo.setOnClickListener {
            Toast.makeText(this, "Registro", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, StartFormActivity::class.java))
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
}