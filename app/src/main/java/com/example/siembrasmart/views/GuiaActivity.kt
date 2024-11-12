package com.example.siembrasmart.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.siembrasmart.databinding.ActivityGuiaBinding

class GuiaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuiaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuiaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnRegresar.setOnClickListener {
            startActivity(Intent(this, ClimaActivity::class.java))
            finish()
        }
    }
}