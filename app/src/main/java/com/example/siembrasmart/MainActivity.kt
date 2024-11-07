package com.example.siembrasmart

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.siembrasmart.views.ClimaActivity
import com.example.siembrasmart.views.SignupActivity

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = Firebase.auth
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signuptext.setOnClickListener {
            checkAndRequestPermissions {
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
            }
        }

        binding.loginbtn.setOnClickListener {
            val email = binding.loginmail.text.toString().trim()
            val password = binding.loginpw.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            checkAndRequestPermissions {
                                Log.d(TAG, "signInWithEmail:success")
                                Toast.makeText(baseContext, "Login successful!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, ClimaActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            val errorMessage = task.exception?.message ?: "Authentication failed."
                            Toast.makeText(baseContext, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(baseContext, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para verificar y solicitar permisos
    private fun checkAndRequestPermissions(onPermissionsGranted: () -> Unit) {
        val permissionsToRequest = mutableListOf<String>()

        // Solicitar permiso de ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Si hay permisos que solicitar, realizar la solicitud
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 1)
        } else {
            // Si ya se tienen los permisos, ejecutar la acción proporcionada
            onPermissionsGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, ClimaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
