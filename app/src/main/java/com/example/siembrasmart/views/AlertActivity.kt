package com.example.siembrasmart.views

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.siembrasmart.R
import com.example.siembrasmart.databinding.ActivityAlertBinding
import com.example.siembrasmart.models.Alertas
import com.example.siembrasmart.utils.Notification
import com.example.siembrasmart.utils.channelID
import com.example.siembrasmart.utils.messageExtra
import com.example.siembrasmart.utils.notificationID
import com.example.siembrasmart.utils.titleExtra
import com.example.siembrasmart.controllers.AlertController
import com.example.siembrasmart.utils.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView

class AlertActivity : Navigation() {
    private lateinit var binding: ActivityAlertBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var alertController: AlertController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        calendar = Calendar.getInstance()

        alertController = AlertController()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()

        // Configuración de botones y adaptadores
        binding.datePickerButton.setOnClickListener {
            showDatePickerDialog()
        }

        binding.timePickerButton.setOnClickListener {
            showTimePickerDialog()
        }

        val frequencies = arrayOf("Diaria", "Semanal", "Cada 2 días")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencies)
        binding.frequencySpinner.adapter = adapter

        binding.alertButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                } else {
                    scheduleCustomNotification()
                }
            } else {
                scheduleCustomNotification()
            }
        }

        // Actualizar alertas cuando los switches cambien de estado
        binding.switchCaudalAlto.setOnCheckedChangeListener { _, _ ->
            getLocationAndFetchData()
        }

        binding.switchCaudalModerado.setOnCheckedChangeListener { _, _ ->
            getLocationAndFetchData()
        }

        binding.switchCaudalMuyBajo.setOnCheckedChangeListener { _, _ ->
            getLocationAndFetchData()
        }

        binding.switchCaudalBajo.setOnCheckedChangeListener { _, _ ->
            getLocationAndFetchData()
        }

        // Navegación inferior y Toolbar
        binding.bottomNavigation.selectedItemId = R.id.navigation_notificacion
        setupBottomNavigationView(binding.bottomNavigation)
        setupToolbar(binding.toolbar)

        // Cargar las alertas al iniciar la actividad
        getLocationAndFetchData()
    }

    private fun getLocationAndFetchData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    lifecycleScope.launch(Dispatchers.IO) {
                        alertController.fetchWeatherData(it.latitude, it.longitude) { alertas ->
                            runOnUiThread {
                                // Limpiar los contenedores de alertas antes de agregar nuevas
                                binding.alertsContainerCaudalAlto.removeAllViews()
                                binding.alertsContainerCaudalModerado.removeAllViews()
                                binding.alertsContainerCaudalMuyBajo.removeAllViews()
                                binding.alertsContainerCaudalBajo.removeAllViews()

                                if (binding.switchCaudalAlto.isChecked) {
                                    addAlertsToContainer(
                                        alertas.alertaCaudalAlto,
                                        "🌊",
                                        "#B71C1C",
                                        "#FFCDD2",
                                        binding.alertsContainerCaudalAlto
                                    )
                                }

                                if (binding.switchCaudalModerado.isChecked) {
                                    addAlertsToContainer(
                                        alertas.alertaCaudalModerado,
                                        "💧",
                                        "#A84F00",
                                        "#FFF9C4",
                                        binding.alertsContainerCaudalModerado
                                    )
                                }

                                // Agregar alertas de sequía roja
                                if (binding.switchCaudalMuyBajo.isChecked) {
                                    addAlertsToContainer(
                                        alertas.alertaCaudalMuyBajo,
                                        "🌵",
                                        "#3E2723",
                                        "#D7CCC8",
                                        binding.alertsContainerCaudalMuyBajo
                                    )
                                }

                                // Agregar alertas de sequía amarilla
                                if (binding.switchCaudalBajo.isChecked) {
                                    addAlertsToContainer(
                                        alertas.alertaCaudalBajo,
                                        "☀️",
                                        "#5D4037",
                                        "#EFEBE9",
                                        binding.alertsContainerCaudalBajo
                                    )
                                }

                                // Mostrar mensaje si no hay alertas en cada sección
                                handleEmptyContainers()
                            }
                        }
                    }
                } ?: run {
                    // Manejar caso de ubicación nula
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
        }
    }
    private fun addAlertsToContainer(
        alertsString: String,
        icon: String,
        iconColor: String,
        backgroundColor: String,
        container: LinearLayout
    ) {
        val alerts = alertsString.trim().split("\n")
        for (alert in alerts) {
            if (alert.isNotBlank()) {
                // Inflar el layout de la tarjeta de alerta
                val alertView = LayoutInflater.from(this).inflate(R.layout.alert_card, container, false)

                // Configurar el icono
                val alertIcon = alertView.findViewById<TextView>(R.id.alertIcon)
                alertIcon.text = icon
                alertIcon.setTextColor(Color.parseColor(iconColor))

                // Configurar el texto de la alerta
                val alertText = alertView.findViewById<TextView>(R.id.alertText)
                alertText.text = alert

                // Configurar el color de fondo de la tarjeta
                val cardView = alertView.findViewById<CardView>(R.id.alertCardView)
                cardView.setCardBackgroundColor(Color.parseColor(backgroundColor))

                // Agregar la vista al contenedor
                container.addView(alertView)
            }
        }
    }

    private fun handleEmptyContainers() {
        // Caudal Alto
        if (binding.alertsContainerCaudalAlto.childCount == 0) {
            val message: String
            val icon: String
            val iconColor: String
            val backgroundColor: String
            if (binding.switchCaudalAlto.isChecked) {
                message = "No hay alertas de caudal alto en este momento."
                icon = "🌊"
                iconColor = "#B71C1C"
                backgroundColor = "#FFCDD2"
            } else {
                message = "Alerta de caudal alto oculta."
                icon = "🙈"
                iconColor = "#B71C1C"
                backgroundColor = "#FFCDD2"
            }
            addAlertsToContainer(
                message,
                icon,
                iconColor,
                backgroundColor,
                binding.alertsContainerCaudalAlto
            )
        }

        // Caudal Moderado
        if (binding.alertsContainerCaudalModerado.childCount == 0) {
            val message: String
            val icon: String
            val iconColor: String
            val backgroundColor: String
            if (binding.switchCaudalModerado.isChecked) {
                message = "No hay alertas de caudal moderado en este momento."
                icon = "💧"
                iconColor = "#A84F00"
                backgroundColor = "#FFF9C4"
            } else {
                message = "Alerta de caudal moderado oculta."
                icon = "🙈"
                iconColor = "#A84F00"
                backgroundColor = "#FFF9C4"
            }
            addAlertsToContainer(
                message,
                icon,
                iconColor,
                backgroundColor,
                binding.alertsContainerCaudalModerado
            )
        }

        // Caudal Bajo
        if (binding.alertsContainerCaudalBajo.childCount == 0) {
            val message: String
            val icon: String
            val iconColor: String
            val backgroundColor: String
            if (binding.switchCaudalBajo.isChecked) {
                message = "No hay alertas de caudal bajo en este momento."
                icon = "🌵"
                iconColor = "#3E2723"
                backgroundColor = "#D7CCC8"
            } else {
                message = "Alerta de caudal bajo oculta."
                icon = "🙈"
                iconColor = "#3E2723"
                backgroundColor = "#D7CCC8"
            }
            addAlertsToContainer(
                message,
                icon,
                iconColor,
                backgroundColor,
                binding.alertsContainerCaudalBajo
            )
        }

        // Caudal Muy Bajo
        if (binding.alertsContainerCaudalMuyBajo.childCount == 0) {
            val message: String
            val icon: String
            val iconColor: String
            val backgroundColor: String
            if (binding.switchCaudalMuyBajo.isChecked) {
                message = "No hay alertas de caudal muy bajo en este momento."
                icon = "☀️"
                iconColor = "#5D4037"
                backgroundColor = "#EFEBE9"
            } else {
                message = "Alerta de caudal muy bajo oculta."
                icon = "🙈"
                iconColor = "#5D4037"
                backgroundColor = "#EFEBE9"
            }
            addAlertsToContainer(
                message,
                icon,
                iconColor,
                backgroundColor,
                binding.alertsContainerCaudalMuyBajo
            )
        }
    }


    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(applicationContext)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)

        AlertDialog.Builder(this)
            .setTitle("Notificación Programada")
            .setMessage(
                "Título: $title\nMensaje: $message\nEn: ${dateFormat.format(date)} ${timeFormat.format(date)}"
            )
            .setPositiveButton("Aceptar") { _, _ -> }
            .show()
    }

    private fun createNotificationChannel() {
        val name = "canal de notificaciones"
        val desc = "notificaciones para catástrofes"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleCustomNotification() {
        val intent = Intent(applicationContext, Notification::class.java)

        val title = "Recordatorio de Alerta"

        val notificationMessage = when (binding.frequencySpinner.selectedItem.toString()) {
            "Diaria" -> "¡Alerta diaria! Revisa los datos importantes a las ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)} cada día."
            "Semanal" -> "¡Alerta semanal! Te recordamos revisar los datos a las ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)} cada semana."
            "Cada 2 días" -> "¡Recordatorio cada 2 días! Mantente atento a los datos importantes a las ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}."
            else -> "Tienes una alerta activa."
        }

        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, notificationMessage)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        when (binding.frequencySpinner.selectedItem.toString()) {
            "Diaria" -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            "Semanal" -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            }
            "Cada 2 días" -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 2,
                    pendingIntent
                )
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            showAlert(calendar.timeInMillis, "Alerta Programada", notificationMessage)
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePickerDialog() {
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }
}