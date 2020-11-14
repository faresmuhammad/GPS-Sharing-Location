package com.fares.gpssharinglocation.ui.activities.profiles

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.fares.gpssharinglocation.BR
import com.fares.gpssharinglocation.GPSSharingApp
import com.fares.gpssharinglocation.R
import com.fares.gpssharinglocation.ViewModelProviderFactory
import com.fares.gpssharinglocation.databinding.ActivityProfilesBinding
import com.fares.gpssharinglocation.model.Profile
import com.fares.gpssharinglocation.model.User
import com.fares.gpssharinglocation.services.LocationService
import com.fares.gpssharinglocation.ui.activities.auth.AuthActivity
import com.fares.gpssharinglocation.ui.activities.users_locations.UsersLocationsActivity
import com.fares.gpssharinglocation.ui.adapter.ProfileAdapter
import com.fares.gpssharinglocation.ui.base.BaseActivity
import com.fares.gpssharinglocation.ui.base.layout
import com.fares.gpssharinglocation.utils.Constants
import com.fares.gpssharinglocation.utils.Constants.ACCESS_FINE_LOCATION
import com.fares.gpssharinglocation.utils.Constants.ERROR_DIALOG_REQUEST
import com.fares.gpssharinglocation.utils.Constants.PERMISSIONS_REQUEST_ENABLE_GPS
import com.fares.gpssharinglocation.utils.Constants.PROFILE_NAME_INTENT_EXTRA
import com.fares.gpssharinglocation.utils.Constants.USER_ID_KEY
import com.fares.gpssharinglocation.utils.Constants.USER_PHONE_NUMBER_KEY
import com.fares.gpssharinglocation.utils.Constants.USER_USERNAME_KEY
import com.fares.gpssharinglocation.utils.PermissionsManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "ProfilesActivity"
typealias ids = R.id

class ProfilesActivity : BaseActivity(), View.OnClickListener {

    //View Model & Data Binding
    private lateinit var vm: ProfilesViewModel
    private lateinit var binding: ActivityProfilesBinding

    //Recycler View
    lateinit var profileAdapter: ProfileAdapter


    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var permissionsManager: PermissionsManager

    private var isLocationPermissionGranted = false

    private lateinit var profileBundle: Bundle
    private var mUser: User? = null
    private lateinit var userClient: User

    private fun performDataBinding() {
        vm = ViewModelProvider(this, factory).get(ProfilesViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, layout.activity_profiles)
        binding.apply {
            setVariable(BR.profilesViewModel, vm)
            invalidateAll()
            executePendingBindings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
        binding.fabAddProfile.setOnClickListener(this)

        profileBundle = Bundle()

        isLocationPermissionGranted =
            permissionsManager.isPermissionGranted(this, ACCESS_FINE_LOCATION)

        profileAdapter = ProfileAdapter()
        setRecyclerView(
            binding.recyclerProfiles,
            profileAdapter,
            LinearLayoutManager(
                this,
                RecyclerView.VERTICAL,
                false
            )
        )
    }


    override fun onStart() {
        super.onStart()
        setUserClient()


        vm.checkIsUserExist(userClient) { isExists ->
            if (!isExists) {
                insertUser()
            } else {
                Timber.d("User is existing")
            }
        }
        vm.getUsers { isEmpty ->
            if (isEmpty) {
                insertUser()
            }
        }.observe(this, Observer {

        })
        getProfiles()

        requestPermissions(ACCESS_FINE_LOCATION)
        startLocationService()
//        requestPermissions(ACCESS_COARSE_LOCATION)
    }

    private fun setUserClient() {
        vm.prefUserId = vm.currentUser!!.uid
        mUser = User(
            vm.currentUser!!.uid,
            vm.prefPhoneNumber!!,
            vm.prefUsername!!
        )


        (application as GPSSharingApp).user = mUser
        userClient = (application as GPSSharingApp).user!!
    }

    override fun onResume() {
        super.onResume()

        profileAdapter.setOnItemClickListener { profile, _ ->
            val usersLocations = Intent(this, UsersLocationsActivity::class.java)
            val locationService = Intent(this, LocationService::class.java)

            profileBundle.putString(PROFILE_NAME_INTENT_EXTRA, profile.name)

            locationService.putExtras(profileBundle)
            usersLocations.putExtras(profileBundle)
            startService(locationService)
            startActivity(usersLocations)
        }
    }


    private fun showAddDialog() {
        val dialog = MaterialDialog(this)
        dialog.customView(layout.dialog_add_profile)
        val dialogView = dialog.getCustomView()

        dialogView.findViewById<MaterialButton>(ids.btn_add_profile).setOnClickListener {
            val profileName =
                dialogView.findViewById<TextInputEditText>(ids.edit_profile).text.toString()
            val profile = Profile(
                profileName,
                vm.currentUser!!.uid
            )
            vm.insertProfile(
                profile,
                mUser!!,
                onSuccess = {
                    getProfiles()
                }
            )
            dialog.dismiss()
        }
        dialogView.findViewById<MaterialButton>(ids.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getProfiles() {
        vm.getProfiles(mUser!!).observe(this, Observer {
            profileAdapter.swapData(it)
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.fabAddProfile -> {

                showAddDialog()

            }
        }
    }

    private fun isServicesOK(): Boolean {

        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when {
            available == ConnectionResult.SUCCESS -> {
                //everything is fine and the user can make map requests

                return true
            }
            GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
                //an error occured but we can resolve it

                val dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, available, ERROR_DIALOG_REQUEST)
                dialog.show()
            }
            else -> {
                Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }

    private fun isMapsEnabled(): Boolean {
        val manager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val enableGpsIntent =
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
            }
            .create()
            .show()

    }

    private fun checkMapServices(): Boolean {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                if (!isLocationPermissionGranted) {
                    requestPermissions(ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    /**
     * Location Service
     */
    private fun startLocationService() {
        if (!isLocationServiceRunning()) {
            val serviceIntent = Intent(this, LocationService::class.java)
            serviceIntent.putExtras(profileBundle)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mUser != null) {
                this.startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    private fun isLocationServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if ("com.fares.gpssharinglocation.services.LocationService" == service.service.className) {

                return true
            }
        }

        return false
    }


    /**
     * User
     */
    private fun insertUser() {

        vm.prefUserId = vm.currentUser!!.uid


        mUser!!.user_id = vm.prefUserId!!
        vm.insertUser(mUser)

    }


    /**
     * Menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            ids.action_sign_out -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun signOut() {
        stopService(Intent(this, LocationService::class.java))
        vm.signOut()
        val backToAuth = Intent(this, AuthActivity::class.java)
        startActivity(backToAuth)
        clearUserData()
    }

    private fun clearUserData() {
        mUser = null
        vm.removeKeys(USER_ID_KEY, USER_USERNAME_KEY, USER_PHONE_NUMBER_KEY)
        profileBundle.clear()
        (application as GPSSharingApp).user = null
    }

    /**
     * Permissions
     */
    private fun requestPermissions(permission: String) {
        if (!permissionsManager.isPermissionGranted(this, permission)) {
            permissionsManager.handleRequestPermissionsCases(
                this,
                permission,
                Constants.REQUEST_CODE
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
        if (requestCode == Constants.REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toast("Permission Granted")
            } else {
                requestPermissions(ACCESS_FINE_LOCATION)
//                requestPermissions(ACCESS_COARSE_LOCATION)
            }
        }
    }
}