package com.fares.gpssharinglocation.ui.activities.profiles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fares.gpssharinglocation.data.AppPreferences
import com.fares.gpssharinglocation.model.Profile
import com.fares.gpssharinglocation.model.User
import com.fares.gpssharinglocation.utils.Constants.PROFILES_COLLECTION
import com.fares.gpssharinglocation.utils.Constants.USERS_COLLECTION
import com.fares.gpssharinglocation.utils.Constants.USER_ID_FIELD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import javax.inject.Inject


class ProfilesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val pref: AppPreferences,
    private val auth: FirebaseAuth
) : ViewModel() {
    val currentUser = auth.currentUser
    private val usersRef = firestore.collection(USERS_COLLECTION)




    /*
    ------------------------------------------------------ Users ---------------------------------------------------
     */

    var prefUserId: String?
        get() = pref.userId
        set(value) {
            pref.userId = value
        }

    var prefPhoneNumber: String?
        get() = pref.phoneNumber
        set(value) {
            pref.phoneNumber = value
        }

    var prefUsername: String?
        get() = pref.username
        set(value) {
            pref.username = value
        }

    fun removeKeys(vararg variableKey: String) = pref.clearVariables(*variableKey)

    private var onUserExists: ((Boolean) -> Unit)? = null
    fun setOnUserExists(listener: (Boolean) -> Unit) {
        onUserExists = listener
    }


    fun checkIsUserExist(user: User, isUserExists: (Boolean) -> Unit) {
        usersRef.get().addOnSuccessListener {
            val docs = it.documents
            for (doc in docs) {
                if (doc.get(USER_ID_FIELD) == user.user_id) {
                    onUserExists?.let { it(true) }
                    isUserExists(true)
                } else {
                    onUserExists?.let { it(false) }
                    isUserExists(false)
                }
            }
        }.addOnFailureListener {
            onUserExists?.let { it(false) }
            isUserExists(false)
        }

    }

    fun insertUser(
        user: User?,
        onSuccess: () -> Unit = { Timber.d("User added successfully") },
        onFailure: (Exception) -> Unit = { Timber.e(it) }
    ) {
        user!!.user_code = user.username
        usersRef.document(user.phoneNumber).set(user).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }
    }


    fun getUsers(isUsersEmpty: (Boolean) -> Unit): LiveData<List<User>> {
        val usersLiveData = MutableLiveData<List<User>>()
        usersRef.get().addOnCompleteListener {
            val users = ArrayList<User>()
            for (doc in it.result!!) {
                val user = doc.toObject(User::class.java)
                users.add(user)
            }
            isUsersEmpty(users.isEmpty())
            usersLiveData.postValue(users)
        }
        return usersLiveData
    }


    fun signOut() {
        auth.signOut()
    }

    /*
    ------------------------------------------------------ Profiles ---------------------------------------------------
     */

    fun insertProfile(
        profile: Profile,
        user: User,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = { Timber.e(it) }
    ) {
        val profilesRef =
            firestore.collection(USERS_COLLECTION).document(user.phoneNumber)
                .collection(PROFILES_COLLECTION)
        profilesRef.document(profile.name).set(profile).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }
    }


    fun getProfiles(user: User): LiveData<List<Profile>> {
        val profilesLiveData = MutableLiveData<List<Profile>>()
        val profilesRef = firestore.collection(USERS_COLLECTION).document(user.phoneNumber)
            .collection(PROFILES_COLLECTION)
        profilesRef.addSnapshotListener { snapshot, _ ->
            snapshot?.let { snap ->

                val profiles = ArrayList<Profile>()
                val docs = snap.documents
                for (doc in docs) {
                    val profile = doc.toObject(Profile::class.java)!!
                    profiles.add(profile)
                }
                profilesLiveData.postValue(profiles)

            }
        }
        return profilesLiveData
    }

}




