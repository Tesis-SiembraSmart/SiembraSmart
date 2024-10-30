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
import com.example.siembrasmart.MainActivity
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

class AlertActivity : Navigation() {
    private lateinit var binding: ActivityAlertBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var alertController: AlertController
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var times: MutableList<String> = mutableListOf()
    var descargaRioMax: MutableList<Double> = mutableListOf()
    var descargaRioMedia: MutableList<Double> = mutableListOf()
    var descargaRioMin: MutableList<Double> = mutableListOf()
    private var totalAlertas = 0
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
        // Al hacer click, se muestra un DatePicker para seleccionar la fecha de la alarma
        binding.datePickerButton.setOnClickListener {
            showDatePickerDialog()
        }
        // Al hacer click, se muestra un TimePicker para seleccionar la hora de la alarma
        binding.timePickerButton.setOnClickListener {
            showTimePickerDialog()
        }


        // Agregamos opciones de frecuencia
        val frequencies = arrayOf("Diaria", "Semanal", "Cada 2 días")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencies)
        binding.frequencySpinner.adapter = adapter

        binding.alertButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                }
            }
            getLocationAndFetchData()
            // scheduleCustomNotification()  // Se llama a la nueva función
        }


        binding.switchAlertRed.setOnCheckedChangeListener { _, isChecked ->
            binding.alertTextViewRed.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Switch para la alerta amarilla de inundación
        binding.switchAlertYellow.setOnCheckedChangeListener { _, isChecked ->
            binding.alertTextViewYellow.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Switch para la alerta roja de sequía
        binding.switchAlertDroughtRed.setOnCheckedChangeListener { _, isChecked ->
            binding.alertTextViewDroughtRed.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Switch para la alerta amarilla de sequía
        binding.switchAlertDroughtYellow.setOnCheckedChangeListener { _, isChecked ->
            binding.alertTextViewDroughtYellow.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Navegación inferior
        binding.bottomNavigation.selectedItemId = R.id.navigation_notificacion
        setupBottomNavigationView(binding.bottomNavigation)
        // Configuración de la Toolbar
        setupToolbar(binding.toolbar)


    }

    private fun getLocationAndFetchData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    fetchWeatherData(it.latitude, it.longitude)
                } ?: run {
                    // Manejar caso de ubicación nula
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
        }
    }

    private fun fetchWeatherData(latitud: Double, longitud: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance()
            val startDate = today.clone() as Calendar
            startDate.add(Calendar.DAY_OF_YEAR, -658)
            val endDate = today.clone() as Calendar
            endDate.add(Calendar.DAY_OF_YEAR, 72)

            val startDateString = dateFormat.format(startDate.time)
            val endDateString = dateFormat.format(endDate.time)

            alertController.fetchDatosOpenMeteo(latitud, longitud, startDateString, endDateString) { json ->
                json?.let {
                    val dailyData = it.getJSONObject("daily")

                    // Limpiamos las listas antes de agregar los nuevos datos
                    times.clear()
                    descargaRioMax.clear()
                    descargaRioMedia.clear()
                    descargaRioMin.clear()

                    val allTimes = alertController.jsonArrayToStringList(dailyData.getJSONArray("time"))
                    val allDescargaRioMax = alertController.jsonArrayToDoubleList(dailyData.getJSONArray("river_discharge_max"))
                    val allDescargaRioMedia = alertController.jsonArrayToDoubleList(dailyData.getJSONArray("river_discharge_mean"))
                    val allDescargaRioMin = alertController.jsonArrayToDoubleList(dailyData.getJSONArray("river_discharge_min"))

                    filterDataFromToday(allTimes, allDescargaRioMax, allDescargaRioMedia, allDescargaRioMin)

                    val maxThresholdRed = calculateThresholdRed(descargaRioMax)
                    val meanThresholdYellow = calculateThresholdYellow(descargaRioMedia)
                    val droughtThresholdRed = calculateDroughtThresholdRed(descargaRioMin)
                    val droughtThresholdYellow = calculateDroughtThresholdYellow(descargaRioMin)

                    val alertas = generateFloodAndDroughtAlerts(
                        descargaRioMax,
                        descargaRioMedia,
                        descargaRioMin,
                        maxThresholdRed,
                        meanThresholdYellow,
                        droughtThresholdRed,
                        droughtThresholdYellow
                    )

                    runOnUiThread {
                        // Limpiamos los TextViews antes de añadir las alertas
                        binding.alertTextViewRed.text = ""
                        binding.alertTextViewYellow.text = ""
                        binding.alertTextViewDroughtRed.text = ""
                        binding.alertTextViewDroughtYellow.text = ""

                        // Ahora actualizamos con las nuevas alertas
                        binding.alertTextViewRed.text = alertas.alertaInundacionRoja
                        binding.alertTextViewYellow.text = alertas.alertaInundacionAmarilla
                        binding.alertTextViewDroughtRed.text = alertas.alertaSequíaRoja
                        binding.alertTextViewDroughtYellow.text = alertas.alertaSequíaAmarilla
                    }
                }
            }
            scheduleCustomNotification()
        }
    }


    private fun filterDataFromToday(
        allTimes: MutableList<String>,
        allDescargaRioMax: MutableList<Double>,
        allDescargaRioMedia: MutableList<Double>,
        allDescargaRioMin: MutableList<Double>
    ) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        for (i in allTimes.indices) {
            if (allTimes[i] >= today) {

                times.add(allTimes[i])
                descargaRioMax.add(allDescargaRioMax[i])
                descargaRioMedia.add(allDescargaRioMedia[i])
                descargaRioMin.add(allDescargaRioMin[i])
            }
        }
    }

    private fun generateFloodAndDroughtAlerts(
        dischargeMax: MutableList<Double>,
        dischargeMean: MutableList<Double>,
        dischargeMin: MutableList<Double>,
        maxThresholdRed: Double,
        meanThresholdYellow: Double,
        droughtThresholdRed: Double,
        droughtThresholdYellow: Double
    ): Alertas {
        val inundacionRoja = StringBuilder()
        val inundacionAmarilla = StringBuilder()
        val sequiaRoja = StringBuilder()
        val sequiaAmarilla = StringBuilder()

        var inundacionRojaCount = 0
        var inundacionAmarillaCount = 0
        var sequiaRojaCount = 0
        var sequiaAmarillaCount = 0

        for (i in dischargeMax.indices) {
            if (dischargeMax[i] > maxThresholdRed) {
                inundacionRoja.append("¡Predicción de Alerta Roja de Inundación! El ${times[i]}, la descarga máxima del río se estima en ${dischargeMax[i]} m³/s.\n")
                inundacionRojaCount++
            } else if (dischargeMean[i] > meanThresholdYellow) {
                inundacionAmarilla.append("Predicción de Alerta Amarilla de Inundación: El ${times[i]}, se estima que la descarga media del río será de ${dischargeMean[i]} m³/s.\n")
                inundacionAmarillaCount++
            }

            if (dischargeMin[i] < droughtThresholdRed) {
                sequiaRoja.append("¡Predicción de Alerta Roja de Sequía! El ${times[i]}, se espera que la descarga mínima del río sea de ${dischargeMin[i]} m³/s.\n")
                sequiaRojaCount++
            } else if (dischargeMin[i] < droughtThresholdYellow) {
                sequiaAmarilla.append("Predicción de Alerta Amarilla de Sequía: El ${times[i]}, se estima que la descarga mínima del río será de ${dischargeMin[i]} m³/s.\n")
                sequiaAmarillaCount++
            }
        }

        totalAlertas = inundacionRojaCount + inundacionAmarillaCount + sequiaRojaCount + sequiaAmarillaCount

        return Alertas(
            alertaInundacionRoja = inundacionRoja.toString(),
            alertaInundacionAmarilla = inundacionAmarilla.toString(),
            alertaSequíaRoja = sequiaRoja.toString(),
            alertaSequíaAmarilla = sequiaAmarilla.toString()
        )
    }

    private fun calculateThresholdRed(descargaRioMax: MutableList<Double>): Double {
        return descargaRioMax.maxOrNull()?.times(0.9) ?: 150.0
    }

    private fun calculateThresholdYellow(descargaRioMedia: MutableList<Double>): Double {
        return if (descargaRioMedia.isNotEmpty()) {
            descargaRioMedia.average().times(1.1)
        } else {
            100.0
        }
    }

    private fun calculateDroughtThresholdRed(descargaRioMin: MutableList<Double>): Double {
        return descargaRioMin.minOrNull()?.times(1.1) ?: 70.0
    }

    private fun calculateDroughtThresholdYellow(descargaRioMin: MutableList<Double>): Double {
        return descargaRioMin.average().times(1.2)
    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(applicationContext)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)

        AlertDialog.Builder(this)
            .setTitle("Notification Scheduled")
            .setMessage(
                "Title: $title\nMessage: $message\nAt: ${dateFormat.format(date)} ${timeFormat.format(date)}"
            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun createNotificationChannel() {
        val name = "canal de notificaciones"
        val desc = "notificaciones para catastrofes"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleCustomNotification() {
        val intent = Intent(applicationContext, Notification::class.java)

        val title = "Recordatorio de Alerta"

        // Genera un mensaje de notificación activa
        val notificationMessage = when (binding.frequencySpinner.selectedItem.toString()) {
            "Diaria" -> "¡Alerta diaria! Revisa los datos importantes a las ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)} cada día."
            "Semanal" -> "¡Alerta semanal! Te recordamos revisar los datos a las ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)} cada semana."
            "Cada 2 días" -> "¡Recordatorio cada 2 días! Mantente atento a los datos importantes a las ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}."
            else -> "Tienes una alerta activa."
        }

        // Se configura la información de la notificación activa
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, notificationMessage)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Configura la alerta de acuerdo a la frecuencia seleccionada
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

        // Genera un mensaje de programación de la alerta
        val programMessage = when (binding.frequencySpinner.selectedItem.toString()) {
            "Diaria" -> "Alerta diaria programada a las ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)} todos los días."
            "Semanal" -> "Alerta semanal programada a las ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)} cada semana."
            "Cada 2 días" -> "Alerta cada 2 días programada a las ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}."
            else -> "Alerta programada."
        }

        // Mostrar el mensaje de que la alerta se ha programado
        lifecycleScope.launch(Dispatchers.Main) {
            showAlert(calendar.timeInMillis, "Alerta Programada", programMessage)
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Actualiza la fecha en el calendario
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
                // Actualiza la hora en el calendario
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)  // Opcionalmente establecer los segundos en 0
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true  // Usa formato 24 horas
        )
        timePicker.show()
    }


}