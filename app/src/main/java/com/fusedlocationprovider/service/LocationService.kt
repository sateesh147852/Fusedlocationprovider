package com.fusedlocationprovider.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.fusedlocationprovider.R
import com.fusedlocationprovider.model.LocationData
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus

const val NotificationChannelId: String = "ForegroundService"

class LocationService : Service() {

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var mLocationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(location: LocationResult) {
            super.onLocationResult(location)
            updateLocation(location)
        }
    }

    private fun updateLocation(location: LocationResult) {
        val locationData =
            LocationData(location.lastLocation.latitude, location.lastLocation.longitude)
        Log.i("TAG", "onUpdateLocation: " + locationData.latitude + " " + locationData.longitude)
        EventBus.getDefault().post(locationData)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1,createNotification())
        locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, mLocationCallback,
                Looper.myLooper()!!
            )
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
    }

    private fun createNotification() : Notification  {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val notificationBuilder = NotificationCompat.Builder(this, NotificationChannelId)
        return notificationBuilder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle("Location").setContentText("Fetching Location in background").build()

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =  NotificationChannel(NotificationChannelId,"My Background Service",NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}