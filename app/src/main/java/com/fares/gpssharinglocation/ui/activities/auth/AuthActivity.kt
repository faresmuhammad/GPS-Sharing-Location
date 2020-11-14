package com.fares.gpssharinglocation.ui.activities.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.fares.gpssharinglocation.BR
import com.fares.gpssharinglocation.GPSSharingApp
import com.fares.gpssharinglocation.ViewModelProviderFactory
import com.fares.gpssharinglocation.databinding.ActivityAuthBinding
import com.fares.gpssharinglocation.ui.activities.auth.AuthState.*
import com.fares.gpssharinglocation.ui.activities.profiles.ProfilesActivity
import com.fares.gpssharinglocation.ui.base.BaseActivity
import com.fares.gpssharinglocation.ui.base.layout
import com.fares.gpssharinglocation.ui.base.string
import com.fares.gpssharinglocation.utils.Utils.validatePhoneNumber
import com.fares.gpssharinglocation.utils.Utils.validateUsername
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.android.AndroidInjection
import javax.inject.Inject



private const val TAG = "AuthActivity"

private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"

enum class AuthState {
    INITIALIZED,
    VERIFY_FAILED,
    VERIFY_SUCCESS,
    CODE_SENT,
    SIGN_IN_FAILED,
    SIGN_IN_SUCCESS
}

class AuthActivity() : BaseActivity(), View.OnClickListener {

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var storedVerificationId: String? = null



    private lateinit var vm: AuthViewModel
    private lateinit var binding: ActivityAuthBinding

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var verificationInProgress: Boolean = false

    @Inject
    lateinit var factory: ViewModelProviderFactory

    private fun performDataBinding(factory: ViewModelProviderFactory) {
        vm = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, layout.activity_auth)
        binding.apply {
            setVariable(BR.authViewModel, vm)
            invalidateAll()
            executePendingBindings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        (application as GPSSharingApp).appComponent.authComponentFactory.create()
        super.onCreate(savedInstanceState)
        performDataBinding(this.factory)

        binding.codePicker.registerCarrierNumberEditText(binding.editPhone)

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        //Clicks
        binding.btnRegister.setOnClickListener(this)
        binding.btnResend.setOnClickListener(this)
        binding.btnVerify.setOnClickListener(this)

        updateUI(INITIALIZED)

    }

    private val taskCompleted: (Task<AuthResult>) -> Unit = { task ->
        if (task.isSuccessful) {
            toast("Signed in successfully")
            Log.d(TAG, "signInWithCredential:success")
            updateUI(SIGN_IN_SUCCESS)


        } else {
            Log.w(TAG, "signInWithCredential:failure", task.exception)
            if (task.exception is FirebaseAuthInvalidCredentialsException) {

                binding.editVcode.error = "Invalid code."
                updateUI(SIGN_IN_FAILED)

            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    override fun onStart() {
        super.onStart()
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                verificationInProgress = false
                updateUI(VERIFY_SUCCESS)
                Log.d(TAG, "onVerificationCompleted:$credential")
//                Log.d(TAG, "user id in callbacks: ${vm.user!!.uid}")

                vm.signInWithAuthPhoneCredential(
                    credential,
                    taskCompleted
                )

            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                verificationInProgress = false
                updateUI(VERIFY_FAILED)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)

                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                updateUI(CODE_SENT)
            }
        }

        /*if (vm.prefPhoneNumber == null || vm.prefUsername == null) {
            Log.w("User", "AuthActivity: User Phone --> null")
            Log.w("User", "AuthActivity: User Username --> null")
        }else{
            Log.w("User", "AuthActivity: User Phone --> ${vm.prefPhoneNumber}")
            Log.w("User", "AuthActivity: User Username --> ${vm.prefUsername}")
        }*/

        if (vm.user != null) {
            intentProfiles()
        }
    }

    private fun intentProfiles() {
        val profiles = Intent(this, ProfilesActivity::class.java)
        profiles.apply {
            putExtra("username", vm.username.get())
            putExtra("phoneNumber", vm.phoneEdit.get())
        }
        startActivity(profiles)
        finish()
    }

    private fun updateUI(state: AuthState) {
        when (state) {
            INITIALIZED -> {
                enableViews(binding.btnRegister, binding.editPhone, binding.codePicker)
                disableViews(
                    binding.btnResend,
                    binding.editVcode,
                    binding.btnVerify
                )
            }
            VERIFY_FAILED -> {
                enableViews(
                    binding.btnResend,
                    binding.editPhone,
                    binding.editVcode
                )
                disableViews(
                    binding.btnRegister
                )

            }
            VERIFY_SUCCESS -> {
                disableViews(
                    binding.btnRegister,
                    binding.btnResend,
                    binding.editPhone,
                    binding.editVcode
                )


            }
            CODE_SENT -> {
                enableViews(
                    binding.btnResend,
                    binding.codePicker,
                    binding.editPhone,
                    binding.editVcode,
                    binding.btnVerify
                )
                disableViews(binding.btnRegister)
                binding.btnRegister.setText(string.status_code_sent)
            }
            SIGN_IN_FAILED -> {
            }
            SIGN_IN_SUCCESS -> {
                intentProfiles()

                vm.prefPhoneNumber = vm.phoneEdit.get()
                vm.prefUsername = vm.username.get()

                Log.w("User", "AuthActivity: User Phone --> ${vm.prefPhoneNumber}")
                Log.w("User", "AuthActivity: User Username --> ${vm.prefUsername}")
                /*vm.userInPreferences = User(
                    vm.phoneEdit.get()!!,
                    vm.username.get()!!
                )
                Log.w("User", "AuthActivity: User in pref--> ${vm.userInPreferences}")
                (application as GPSTrackerApp).setUser(vm.userInPreferences!!)
                Log.w(
                    "User",
                    "AuthActivity: User in app class --> ${(application as GPSTrackerApp).getUser()}"
                )*/
            }

        }
    }


    private fun enableViews(vararg views: View) {
        for (view in views) {
            view.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (view in views) {
            view.isEnabled = false
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnRegister -> {
                if (validatePhoneNumber(
                        binding.editPhone,
                        vm.phoneEdit.get()!!
                    ) && validateUsername(binding.editUsername, vm.username.get()!!)
                ) {
                    vm.startVerifyingPhoneNumber(
                        this,
                        binding.codePicker.selectedCountryCodeWithPlus + vm.phoneEdit.get()!!,
                        callbacks
                    )
                    verificationInProgress = true
                    Log.d(
                        TAG,
                        "onClick: ${binding.codePicker.selectedCountryCodeWithPlus + vm.phoneEdit.get()!!}"
                    )
                }
            }
            binding.btnVerify -> {
                if (TextUtils.isEmpty(vm.verificationCode.get())) {
                    binding.editVcode.error = "Cannot be empty."
                    return
                }
                vm.verifyPhoneNumberWithCode(
                    storedVerificationId,
                    vm.verificationCode.get()!!,
                    taskCompleted
                )

            }
            binding.btnResend -> {
                vm.resendVerificationCode(
                    this,
                    binding.codePicker.selectedCountryCodeWithPlus + vm.phoneEdit.get()!!,
                    token = {
                        if (::resendToken.isInitialized)
                            resendToken
                        else
                            null
                    },
                    callbacks = callbacks
                )
            }

        }
    }

}