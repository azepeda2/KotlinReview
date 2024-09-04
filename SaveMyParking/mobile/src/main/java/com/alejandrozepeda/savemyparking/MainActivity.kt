package com.alejandrozepeda.savemyparking

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.StringBuilder
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var countdownText: TextView
    private lateinit var startButton: Button
    private var countdownTimer: CountDownTimer? = null
    private lateinit var addTimeButton: Button
    private lateinit var subTimeButton: Button
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var apiKey : String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        apiKey = getString(R.string.google_maps_api_key)
        countdownText = findViewById(R.id.countdown_text)
        startButton = findViewById(R.id.start_button)
        addTimeButton = findViewById(R.id.add_time_button)
        subTimeButton = findViewById(R.id.sub_time_button)
        mapView = findViewById<MapView>(R.id.map_view)
        mapView.onCreate(savedInstanceState)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        startButton.setOnClickListener {
            startCountdown()
            Log.d("Log", countdownText.text.toString())
        }

        addTimeButton.setOnClickListener {
            addMinutesToTimer(5)
            Log.d("Log", "called add time to countdown timer button")
        }

        subTimeButton.setOnClickListener {
            subMinutesToTimer(5)
            Log.d("Log", "called sub time to countdown timer button")
        }


    }

    private fun startCountdown() {
        countdownTimer?.cancel() // Cancel any existing countdown
        //countdownTimer = object : CountDownTimer(parseTimeToSeconds(countdownText.toString()), 1000) {
        countdownTimer = object : CountDownTimer(parseTimeToMilliSeconds(countdownText.text.toString()), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / 3600000
                val minutes = (millisUntilFinished % 3600000) / 60000
                val seconds = (millisUntilFinished % 60000) / 1000

                val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                countdownText.text = timeString

            }

            override fun onFinish() {
                countdownText.text = "Time's up!"
                Toast.makeText(applicationContext,
                    "Time is up! Please check on your parking!",
                    Toast.LENGTH_LONG).show()
            }
        }
        countdownTimer?.start()
    }

    private fun parseTimeInput(timeString: String): Long {
        val pattern = Regex("^(\\d{1,2}):(\\d{2}):(\\d{2})$")
        val match = pattern.matchEntire(timeString) ?: return 0

        val hours = match.groupValues[1].toInt()
        val minutes = match.groupValues[2].toInt()
        val seconds = match.groupValues[3].toInt()

        if (hours !in 0..23 || minutes !in 0..59 || seconds !in 0..59) {
            Toast.makeText(applicationContext,
                "Please choose a valid time!",
                Toast.LENGTH_SHORT).show()
            return 0 // Invalid time
        }

        return hours * 3600L + minutes * 60L + seconds
    }

    private fun parseTimeToMilliSeconds(timeString: String) : Long {
        val splitStrings = timeString.split(":")
        var seconds = 0L
        seconds += splitStrings[0].toLong() * 3600L
        seconds += splitStrings[1].toLong() * 60L
        seconds += splitStrings[2].toLong()

        Log.d("Log", "time in seconds: $seconds")

        seconds *= 1000

        Log.d("Log", "time in milliseconds: $seconds")

        return seconds
    }

    private fun addMinutesToTimer(min : Long) {
        val splitStrings = countdownText.text.toString().split(":")
        var hours = splitStrings[0].toLong()
        var minutes = splitStrings[1].toLong()
        var seconds = splitStrings[2].toLong()
        var updatedTime : StringBuilder = StringBuilder()

        minutes += min

        if (minutes >= 0) {
            hours += minutes / 60
        } else {
            hours -= 1
            minutes += 60

            if (hours < 0) {
                hours = 0
                minutes = 0
            }
        }

        minutes = minutes % 60

        if (hours < 9)
            updatedTime.append("0$hours:")
        else
            updatedTime.append("$hours:")

        if (minutes < 9)
            updatedTime.append("0$minutes:")
        else
            updatedTime.append("$minutes:")

        if (seconds < 9)
            updatedTime.append("0$seconds")
        else
            updatedTime.append("$seconds")


        Log.d("Log", "Added $min minutes: New countdown: $updatedTime")

        countdownText.text = updatedTime.toString()
    }

    private fun subMinutesToTimer(min : Long) {
        addMinutesToTimer(-min)
    }

    private fun hasLocationPermission(context: Context) : Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            return true
        } else {
            val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                context as MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
                )

            if (rationalRequired) {
                Toast.makeText(context,
                    "Location is needed to save your parking",
                    Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                        2000)
            }
            return false
        }
    }

    @SuppressLint("MissingPermission")
    private fun takeScreenshot() {
        if (hasLocationPermission(this)) {
            Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show()
            // PERMISSION is Already granted ((navigate to Next Screen...))
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val userLocation = LatLng(location.latitude, location.longitude)
                mapView.getMapAsync { googleMap ->
                    // Handle the GoogleMap object
                    var map = googleMap
                    map.addMarker(MarkerOptions().position(userLocation))
                    val bitmap = mapView.drawToBitmap()
                }
            }
        }
    }
}
