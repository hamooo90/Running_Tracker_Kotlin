package com.gmail.hamedvakhide.runningtracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.gmail.hamedvakhide.runningtracker.R
import com.gmail.hamedvakhide.runningtracker.util.Constants.ACTION_PAUSE_SERVICE
import com.gmail.hamedvakhide.runningtracker.util.Constants.ACTION_START_RESUME_SERVICE
import com.gmail.hamedvakhide.runningtracker.util.Constants.ACTION_STOP_SERVICE
import com.gmail.hamedvakhide.runningtracker.util.Constants.FASTEST_LOCATION_UPDATE_INTERVAL
import com.gmail.hamedvakhide.runningtracker.util.Constants.LOCATION_UPDATE_INTERVAL
import com.gmail.hamedvakhide.runningtracker.util.Constants.LOG_TAG
import com.gmail.hamedvakhide.runningtracker.util.Constants.NOTIFICATION_CHANNEL_ID
import com.gmail.hamedvakhide.runningtracker.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.gmail.hamedvakhide.runningtracker.util.Constants.NOTIFICATION_ID
import com.gmail.hamedvakhide.runningtracker.util.TrackingUtil
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias PolyLine = MutableList<LatLng>
typealias PolyLines = MutableList<PolyLine>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val path = MutableLiveData<PolyLines>()
//        val startingLocation = MutableLiveData<Location>()
    }

    var isFirstRun = true
    var isPaused = false

    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder


    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()

        isTracking.observe(this, {
            updateLocationTracking(it)
            updateNotificationTracking(it)
        })

    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        path.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    private fun addEmptyPolyLine() = path.value?.apply {
        add(mutableListOf())
        path.postValue(this)
    } ?: path.postValue(mutableListOf(mutableListOf()))

    private fun addPath(location: Location?) {
        location?.let {
            val latLng = LatLng(location.latitude, location.longitude)
            path.value?.apply {
                last().add(latLng)
                path.postValue(this)
            }
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (isTracking.value!!) {
                locationResult.locations.let { locationsList ->
                    for (location in locationsList) {
                        if (isPaused) {
                            // fix some bugs
                            isPaused = false
                        } else {
                            addPath(location)
                        }
                        Log.d(
                            LOG_TAG,
                            "onLocationResult: New Location ${location.latitude} ${location.longitude}"
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtil.hasLocationPermission(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }
    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intent.let {
            when (it.action) {
                ACTION_START_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                        Log.d(LOG_TAG, "Service started ")
                    } else {
                        Log.d(LOG_TAG, "Service resumed ")
                        startTimer()

                    }

                }
                ACTION_PAUSE_SERVICE -> {
                    Log.d(LOG_TAG, "Service paused")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Log.d(LOG_TAG, "Service stoped")
                    killService()
                }
                else -> {
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateNotificationTracking(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //clear all previous notifications
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled){
            currentNotificationBuilder = baseNotificationBuilder.addAction(
                R.drawable.ic_pause,
                notificationActionText,
                pendingIntent
            )
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())

        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
        isPaused = true
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer() {
        addEmptyPolyLine()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted

                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(50L)
            }
            timeRun += lapTime
        }
    }

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this,{
            if(!serviceKilled){
                val notification = currentNotificationBuilder
                    .setContentText(TrackingUtil.getFormattedTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID,notification.build())
            }
        })
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                importance
            )
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

}