package com.fares.gpssharinglocation.ui.activities.auth

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.fares.gpssharinglocation.data.AppPreferences
import com.fares.gpssharinglocation.di.annotations.AuthScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "AuthViewModel"

@AuthScope
open class AuthViewModel @Inject constructor(
    private val pref: AppPreferences,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    val user = auth.currentUser


    //Sign up fields
    val username = ObservableField<String>()
    val phoneEdit = ObservableField<String>()
    val verificationCode = ObservableField<String>()


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


    /*
    ------------------------------------------------ Authentication -----------------------------------------
     */
    fun startVerifyingPhoneNumber(
        activity: AuthActivity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder().apply {
            setPhoneNumber(phoneNumber)
            setTimeout(60,TimeUnit.SECONDS)
            setActivity(activity)
            setCallbacks(callbacks)
        }.build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    fun verifyPhoneNumberWithCode(
        verificationId: String?,
        code: String,
        taskCompleted: (Task<AuthResult>) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithAuthPhoneCredential(credential, taskCompleted)
    }

    fun resendVerificationCode(
        activity: AuthActivity,
        phoneNumber: String,
        token: () -> PhoneAuthProvider.ForceResendingToken?,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder().apply {
            setPhoneNumber(phoneNumber)
            setTimeout(60,TimeUnit.SECONDS)
            setActivity(activity)
            setCallbacks(callbacks)
            setForceResendingToken(token()!!)
        }.build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithAuthPhoneCredential(
        credential: PhoneAuthCredential,
        taskCompleted: (Task<AuthResult>) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                taskCompleted(task)
            }
    }

    fun signOut() {
        auth.signOut()
    }



}