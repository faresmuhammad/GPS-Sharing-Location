package com.fares.gpssharinglocation.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.fares.gpssharinglocation.GPSSharingApp
import com.fares.gpssharinglocation.model.UserLocation
import com.fares.gpssharinglocation.utils.Constants.CHANNEL_ID
import com.fares.gpssharinglocation.utils.Constants.FASTEST_INTERVAL
import com.fares.gpssharinglocation.utils.Constants.UPDATE_INTERVAL
import com.fares.gpssharinglocation.utils.Constants.USERS_LOCATIONS_COLLECTION
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import timber.log.Timber
import javax.inject.Inject


private const val TAG = "LocationService"

class LocationService : Service() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var auth: FirebaseAuth


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val binder = LocationBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        (application as GPSSharingApp).appComponent.locationComponentFactory.create().inject(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
                setContentTitle("")
                setContentText("")
            }.build()

            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        getLocation()

        return START_NOT_STICKY
    }


    private var onLocationChanged: ((UserLocation) -> Unit)? = null


    private fun getLocation() {
        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.d("getLocation: stopping the location service.")
            stopSelf()
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult?) {
                    Timber.d("onLocationResult: got location result.")
                    if ((application as GPSSharingApp).isUserNull) {
                        Timber.d("is user null -> ${(application as GPSSharingApp).isUserNull}")
                        stopSelf()
                    } else {
                        setLocationResult(result)
                    }

                }

                private fun setLocationResult(result: LocationResult?) {
                    val location = result?.lastLocation!!
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    val user = (application as GPSSharingApp).user
                    Timber.d("onLocationResult: GeoPoint --> $geoPoint")

                    val userLocation = UserLocation(user!!, geoPoint)
                    saveUserLocation(userLocation)

                    onLocationChanged?.let {
                        it(userLocation)
                        Timber.d("Location Changed")
                    }


                }
            },
            Looper.myLooper()
        )
    }

    private fun saveUserLocation(
        userLocation: UserLocation,
        onSuccess: () -> Unit = { Timber.d("User Location saved") },
        onFailure: (Exception) -> Unit = { Timber.e(it) }
    ) {
        val locationsRef = firestore.collection(USERS_LOCATIONS_COLLECTION)
            .document(auth.currentUser?.uid!!)

        locationsRef.set(userLocation).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }
    }
    fun setOnLocationChanged(listener: (UserLocation) -> Unit) {
        Timber.d("setOnLocationChanged called")
        onLocationChanged = listener
    }




    inner class LocationBinder : Binder() {
        val service: LocationService
            get() {
                return this@LocationService
            }
    }
}

