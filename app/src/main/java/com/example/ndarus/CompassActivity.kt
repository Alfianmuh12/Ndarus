package com.example.ndarus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

class CompassActivity : AppCompatActivity(), SensorEventListener, LocationListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private lateinit var compassImage: ImageView
    private lateinit var directionStatusTextView: TextView
    private var currentDegree = 0f
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)
        compassImage = findViewById(R.id.iv_compass)
        directionStatusTextView = findViewById(R.id.tv_direction_status)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            startCompass()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCompass() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10f, this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val degree = Math.round(event.values[0])
        val rotateAnimation = RotateAnimation(
            currentDegree,
            -degree.toFloat(),
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 200
        rotateAnimation.fillAfter = true
        compassImage.startAnimation(rotateAnimation)
        currentDegree = -degree.toFloat()

        val qiblaDirection = getQiblaDirection(currentLatitude, currentLongitude)
        val tolerance = 5 // Toleransi sudut untuk dianggap mengarah ke kiblat

        if (Math.abs(degree - qiblaDirection) <= tolerance) {
            directionStatusTextView.text = "Sudah mengarah ke kiblat"
        } else {
            directionStatusTextView.text = "Belum mengarah ke kiblat"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Tidak perlu diimplementasikan
    }

    override fun onLocationChanged(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
    }

    private fun getQiblaDirection(latitude: Double, longitude: Double): Float {
        val kaabaLatitude = 21.4225 // Latitude Kaaba
        val kaabaLongitude = 39.8262 // Longitude Kaaba

        val userLatitudeRad = Math.toRadians(latitude)
        val kaabaLatitudeRad = Math.toRadians(kaabaLatitude)
        val deltaLongitudeRad = Math.toRadians(kaabaLongitude - longitude)

        val y = Math.sin(deltaLongitudeRad) * Math.cos(kaabaLatitudeRad)
        val x = Math.cos(userLatitudeRad) * Math.sin(kaabaLatitudeRad) - Math.sin(userLatitudeRad) * Math.cos(kaabaLatitudeRad) * Math.cos(deltaLongitudeRad)

        var qiblaDirectionRad = Math.atan2(y, x)
        qiblaDirectionRad = (Math.toDegrees(qiblaDirectionRad) + 360) % 360

        return qiblaDirectionRad.toFloat()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCompass()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
    }

    override fun onProviderEnabled(provider: String) {
        // Tidak perlu diimplementasikan
    }

    override fun onProviderDisabled(provider: String) {
        // Tidak perlu diimplementasikan
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Tidak perlu diimplementasikan
    }
}