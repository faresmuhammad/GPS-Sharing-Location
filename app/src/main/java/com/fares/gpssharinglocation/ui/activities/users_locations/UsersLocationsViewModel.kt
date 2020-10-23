package com.fares.gpssharinglocation.ui.activities.users_locations

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fares.gpssharinglocation.model.User
import com.fares.gpssharinglocation.model.UserLocation
import com.fares.gpssharinglocation.services.LocationService
import com.fares.gpssharinglocation.utils.Constants
import com.fares.gpssharinglocation.utils.Constants.PROFILES_COLLECTION
import com.fares.gpssharinglocation.utils.Constants.USERS_COLLECTION
import com.fares.gpssharinglocation.utils.Constants.USERS_LOCATIONS_COLLECTION
import com.fares.gpssharinglocation.utils.Constants.USER_INVITATION_CODE
import com.fares.gpssharinglocation.utils.map_markers.ClusterMarker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.ClusterManager
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "UsersLocationsViewModel"


class UsersLocationsViewModel @Inject constructor(
    activity: UsersLocationsActivity,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {


    val clusterMarkers = MutableLiveData<List<ClusterMarker>>()

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(activity)

    val binder = MutableLiveData<LocationService.LocationBinder>()
    val serviceConnection: ServiceConnection
        get() {
            return object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    binder.postValue(service as LocationService.LocationBinder)
                    Timber.d("Service Bounded")
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    binder.postValue(null)
                    Timber.d("Service Unbounded")
                }
            }
        }


    fun insertUserToProfile(
        user: User,
        userCode: String,
        profileName: String,
        onSuccess: (DocumentSnapshot) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {

        val usersRef = firestore.collection(USERS_COLLECTION)
        val profileUsersRef = firestore.collection(USERS_COLLECTION).document(user.phoneNumber)
            .collection(PROFILES_COLLECTION).document(profileName).collection(USERS_COLLECTION)

        usersRef.get().addOnSuccessListener { query ->

            val docs = query.documents
            for (doc in docs) {
                if (doc.get(USER_INVITATION_CODE) == userCode) {
                    val userObject = doc.toObject(User::class.java)
                    usersRef.document(userObject!!.phoneNumber).get()
                        .addOnSuccessListener { snapshot ->
                            addUserToProfile(profileUsersRef, userObject)
                            onSuccess(snapshot)
                        }.addOnFailureListener {
                            onFailure(it)
                        }
                }
            }
        }.addOnFailureListener {
            onFailure(it)
        }

    }

    private fun addUserToProfile(
        profileUsersRef: CollectionReference,
        userObject: User,
        onSuccess: () -> Unit = { Timber.d("User Location saved") },
        onFailure: (Exception) -> Unit = { Timber.e(it) }
    ) {
        profileUsersRef.document(userObject.phoneNumber).set(userObject)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }


    fun checkIsUserExist(
        userClient: User,
        profileUser: User,
        profileName: String,
        isUserExists: (Boolean) -> Unit
    ) {

        val profileUsersRef =
            firestore.collection(USERS_COLLECTION).document(userClient.phoneNumber)
                .collection(PROFILES_COLLECTION).document(profileName).collection(USERS_COLLECTION)
        profileUsersRef.get().addOnSuccessListener {
            val docs = it.documents
            if (!docs.isNullOrEmpty()) {
                for (doc in docs) {
                    if (doc.get(USER_INVITATION_CODE) == profileUser.user_code) {
                        isUserExists(true)

                    } else {
                        isUserExists(false)
                    }
                }
            } else {
                isUserExists(false)
            }
        }.addOnFailureListener {
            isUserExists(false)
        }

    }


    fun getUserLocationFromUser(user: User, location: (UserLocation) -> Unit) {
        val usersLocationRef =
            firestore.collection(Constants.USERS_LOCATIONS_COLLECTION)
        usersLocationRef.get().addOnSuccessListener {
            val docs = it.documents
            for (doc in docs) {
                val userLocationObject = doc.toObject(UserLocation::class.java)
                if (userLocationObject!!.user == user) {
                    location(userLocationObject)
                }
            }
        }.addOnFailureListener {
            Timber.e(it)
        }
    }


    fun getUserByCode(userCode: String, getUser: (User) -> Unit) {
        val usersRef = firestore.collection(USERS_COLLECTION)
        usersRef.get().addOnSuccessListener {
            val docs = it.documents
            for (doc in docs) {
                val userObject = doc.toObject(User::class.java)!!
                if (userObject.user_code == userCode) {
                    getUser(userObject)
                    return@addOnSuccessListener
                }
            }
        }
    }

    fun getUsers(
        userClient: User,
        profileName: String,
        isUserEmpty: (Boolean) -> Unit = {}
    ): LiveData<List<User>> {
        val usersLiveData = MutableLiveData<List<User>>()
        val profileUsersRef =
            firestore.collection(USERS_COLLECTION).document(userClient.phoneNumber)
                .collection(PROFILES_COLLECTION).document(profileName).collection(USERS_COLLECTION)

        profileUsersRef.get().addOnSuccessListener {
            val docs = it.documents
            val users = ArrayList<User>()
            for (doc in docs) {
                val userObject = doc.toObject(User::class.java)!!
                users.add(userObject)
            }
            isUserEmpty(users.isEmpty())
            usersLiveData.postValue(users)
        }
        return usersLiveData
    }

    fun getUsersLocations(locations: (List<UserLocation?>) -> Unit) {
        val usersLocationsRef = firestore.collection(USERS_LOCATIONS_COLLECTION)
        usersLocationsRef.get().addOnSuccessListener { query ->
            val usersLocations = query.documents.map { it.toObject(UserLocation::class.java) }
            locations(usersLocations)
        }.addOnFailureListener {
            Timber.e(it)
        }
    }

    fun deleteUser(
        userClient: User,
        user: User,
        profileName: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = { Timber.e(it) }
    ) {
        val profileUsersRef =
            firestore.collection(USERS_COLLECTION).document(userClient.phoneNumber)
                .collection(PROFILES_COLLECTION).document(profileName).collection(USERS_COLLECTION)
        profileUsersRef.document(user.phoneNumber).delete().addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }
    }


    fun addMarkers(
        userClient: User,
        profileName: String,
        map: GoogleMap
    ) {
        map.clear()
        val profileUsersRef =
            firestore.collection(USERS_COLLECTION).document(userClient.phoneNumber)
                .collection(PROFILES_COLLECTION).document(profileName).collection(USERS_COLLECTION)
        profileUsersRef.get().addOnSuccessListener {
            val docs = it.documents
            for (doc in docs) {
                val userObject = doc.toObject(User::class.java)
                getUserLocationFromUser(userObject!!) { userLocation ->
                    val marker = MarkerOptions()
                    val geoPoint = userLocation.geo_point
                    marker.position(LatLng(geoPoint.latitude, geoPoint.longitude))
                    marker.title(userObject.username)
                    map.addMarker(marker)
                }
            }
        }
    }

    fun addClusterMarkers(
        userClient: User,
        profileName: String,
        clusterManager: ClusterManager<ClusterMarker>
    ) {
        val profileUsersRef =
            firestore.collection(USERS_COLLECTION).document(userClient.phoneNumber)
                .collection(PROFILES_COLLECTION).document(profileName).collection(USERS_COLLECTION)


        val clusterMarkersList = mutableListOf<ClusterMarker>()
        profileUsersRef.get().addOnSuccessListener { query ->
            val users = query.documents.map { it.toObject(User::class.java) }
            for (userObject in users) {
                getUserLocationFromUser(userObject!!) { userLocation ->

                    val geoPoint = userLocation.geo_point
                    val clusterMarker = ClusterMarker(
                        LatLng(geoPoint.latitude, geoPoint.longitude),
                        userObject.username,
                        "",
                        userObject
                    )
                    clusterManager.addItem(clusterMarker)
                    clusterMarkersList.add(clusterMarker)
                }
            }
            clusterMarkers.postValue(clusterMarkersList)
            clusterManager.cluster()
        }
    }

}

