package com.fares.gpssharinglocation.ui.activities.users_locations

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.fares.gpssharinglocation.BR
import com.fares.gpssharinglocation.GPSTrackerApp
import com.fares.gpssharinglocation.R
import com.fares.gpssharinglocation.ViewModelProviderFactory
import com.fares.gpssharinglocation.databinding.ActivityUsersLocationsBinding
import com.fares.gpssharinglocation.model.User
import com.fares.gpssharinglocation.services.LocationService
import com.fares.gpssharinglocation.ui.activities.profiles.ids
import com.fares.gpssharinglocation.ui.adapter.UserAdapter
import com.fares.gpssharinglocation.ui.base.BaseActivity
import com.fares.gpssharinglocation.ui.base.layout
import com.fares.gpssharinglocation.utils.Constants.ACCESS_COARSE_LOCATION
import com.fares.gpssharinglocation.utils.Constants.ACCESS_FINE_LOCATION
import com.fares.gpssharinglocation.utils.Constants.PROFILE_NAME_INTENT_EXTRA
import com.fares.gpssharinglocation.utils.Constants.REQUEST_CODE
import com.fares.gpssharinglocation.utils.PermissionsManager
import com.fares.gpssharinglocation.utils.map_markers.ClusterMarker
import com.fares.gpssharinglocation.utils.map_markers.ClusterMarkerRenderer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.clustering.ClusterManager
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "UsersLocationsActivity"


class UsersLocationsActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var vm: UsersLocationsViewModel
    private lateinit var binding: ActivityUsersLocationsBinding

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var permissionsManager: PermissionsManager

    private lateinit var extras: Bundle

    private lateinit var mapView: MapView

    private lateinit var userAdapter: UserAdapter

    private lateinit var userCode: String
    private lateinit var userClient: User
    private lateinit var profileName: String


    private lateinit var mGoogleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<ClusterMarker>
    private lateinit var clusterRenderer: ClusterMarkerRenderer


    private var locationService: LocationService? = null
    private var isServiceBound = false
    private val isServiceBounded = MutableLiveData<Boolean>(false)

    private fun performDataBinding() {
        vm = ViewModelProvider(this, factory).get(UsersLocationsViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, layout.activity_users_locations)
        binding.apply {
            setVariable(BR.locationsViewModel, vm)
            invalidateAll()
            executePendingBindings()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()

        setBoundService()

        init()


        setRecyclerView(
            binding.recUsers,
            userAdapter,
            LinearLayoutManager(
                this,
                RecyclerView.VERTICAL,
                false
            )
        )




        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

    }

    private fun init() {
        mapView = binding.mapView
        userClient = (application as GPSTrackerApp).user!!
        extras = intent.extras!!
        profileName = extras.get(PROFILE_NAME_INTENT_EXTRA) as String
        userAdapter = UserAdapter()

    }


    private fun setBoundService() {

        bindService(
            Intent(this, LocationService::class.java),
            vm.serviceConnection,
            Context.BIND_AUTO_CREATE
        )

    }

    private fun makeServiceUnbound() {
        if (!isServiceBound) {
            unbindService(vm.serviceConnection)
            isServiceBounded.postValue(false)
        }
    }

    private fun requestPermissions() {
        if (!permissionsManager.isPermissionGranted(this, ACCESS_FINE_LOCATION)) {
            permissionsManager.handleRequestPermissionsCases(
                this,
                ACCESS_FINE_LOCATION,
                REQUEST_CODE
            ) {
                toast("Location Permission is required to open maps")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toast("Permission Granted")
            } else {
                requestPermissions()
                onMapReady(mGoogleMap)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
        } else {
            mGoogleMap = googleMap!!
            clusterManager = ClusterManager(applicationContext, mGoogleMap)
            clusterRenderer = ClusterMarkerRenderer(
                this,
                mGoogleMap,
                clusterManager
            )

            with(clusterManager) {
                renderer = clusterRenderer
                setOnClusterItemClickListener {
                    Timber.d("Cluster Clicked")
                    toast("Cluster Clicked")
                    return@setOnClusterItemClickListener true
                }
            }


            with(mGoogleMap) {
                isMyLocationEnabled = true
                setOnCameraIdleListener(clusterManager)
                setOnMarkerClickListener(clusterManager)
                vm.addClusterMarkers(userClient, profileName, clusterManager)

            }
        }


    }


    private fun subscribeToObservers() {
        vm.binder.observe(this, Observer { binder ->
            if (binder != null) {
                locationService = binder.service
                isServiceBounded.postValue(true)
            } else {
                isServiceBounded.postValue(false)
            }
        })
        isServiceBounded.observe(this, Observer {
            isServiceBound = it
        })
    }

    private fun setCameraView(location: GeoPoint) {
        var mapBoundary: LatLngBounds
        location.let {
            val boundPadding = 0.01
            val bottomLeftBound = LatLng(it.latitude - boundPadding, it.longitude - boundPadding)
            val topRightBound = LatLng(it.latitude + boundPadding, it.longitude + boundPadding)
            mapBoundary = LatLngBounds(bottomLeftBound, topRightBound)
        }
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBoundary, 0))

    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        subscribeToObservers()
        getUsers(userClient, profileName)
        userAdapter.setOnItemClickListener { user, _ ->
            vm.getUserLocationFromUser(user) {
                Timber.d("${user.phoneNumber} -> ${it.geo_point}")
                setCameraView(it.geo_point)
            }
        }

        userAdapter.setOnItemLongClickListener { user, _ ->
            vm.deleteUser(userClient, user, profileName)
        }


        Timber.d("Service before -> ${locationService == null}")

        if (isServiceBound) {
            Timber.d("Service after -> ${locationService == null}")
            locationService?.setOnLocationChanged {
                vm.clusterMarkers.observe(this, Observer { markers ->
                    for (marker in markers) {
                        vm.getUserLocationFromUser(marker.user) { userLocation ->

                            val geoPoint = userLocation.geo_point
                            Timber.d("marker user location -> $geoPoint")
                            marker.point = LatLng(geoPoint.latitude, geoPoint.longitude)
                            clusterRenderer.updateMarker(marker)
                        }
                    }

                })

                /*val geoPoint = userLocation.geo_point
                vm.clusterMarkers.observe(this, Observer { markers ->
                    Timber.d("clusters called to be updated")
                    for (marker in markers) {
                        if (marker.user == userLocation.user) {
                            marker.point = LatLng(geoPoint.latitude, geoPoint.longitude)
                            clusterRenderer.updateMarker(marker)
                        }

                    }
                })*/
            }
        }


    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()


    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
        makeServiceUnbound()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_users_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            ids.action_add_user -> {
                showAddDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun showAddDialog() {
        val dialog = MaterialDialog(this)
        dialog.customView(layout.dialog_add_user)
        val dialogView = dialog.getCustomView()

        dialogView.findViewById<MaterialButton>(ids.btn_add_user).setOnClickListener {
            userCode =
                dialogView.findViewById<TextInputEditText>(ids.edit_user).text.toString().trim()
            vm.getUserByCode(userCode) {
                Timber.d("User Code -> $userCode")
                vm.checkIsUserExist(userClient, it, profileName) { isExists ->
                    Timber.d("isUserExists -> $isExists")
                    if (!isExists) {
                        vm.insertUserToProfile(
                            userClient,
                            userCode,
                            profileName,
                            onSuccess = {
                                getUsers(userClient, profileName)
                            }
                        )
                    } else {
                        Timber.d("User is existing")
                    }
                }
            }

            dialog.dismiss()

        }
        dialogView.findViewById<MaterialButton>(ids.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun getUsers(userClient: User, profileName: String) {
        vm.getUsers(userClient, profileName).observe(this, Observer {
            Timber.d("users size -> ${it.size}")
            userAdapter.swapData(it)
        })
    }

    /*private fun updateClusterMarkers(userLocation: UserLocation, markers: List<ClusterMarker>) {

        for (marker in markers) {
            try {
                if (marker.user == userLocation.user) {
                    marker.positionPoint =
                        LatLng(userLocation.geo_point.latitude, userLocation.geo_point.longitude)
                    clusterRenderer.updateMarker(marker)

                }
            } catch (e: NullPointerException) {
                Timber.d(e)
            }


        }
    }*/


}

