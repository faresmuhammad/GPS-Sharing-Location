package com.fares.gpssharinglocation.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.fares.gpssharinglocation.utils.Constants.SHARED_PREFERENCES_KEY
import com.fares.gpssharinglocation.utils.Constants.USER_ID_KEY
import com.fares.gpssharinglocation.utils.Constants.USER_PHONE_NUMBER_KEY
import com.fares.gpssharinglocation.utils.Constants.USER_USERNAME_KEY
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "SharedPreferences"

@Singleton
class AppPreferences @Inject constructor(
    context: Context
) {
    private val pref = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE)
    private val editor = pref.edit()


    fun setIsPermissionFirstAsked(isFirstAsked: Boolean, permission: String) {
        editor.putBoolean(permission, isFirstAsked).apply()
    }

    fun getIsPermissionFirstAsked(permission: String): Boolean =
        pref.getBoolean(permission, true)


    var userId: String?
        get() = pref.getString(USER_ID_KEY, "")
        set(value) {
            editor.putString(USER_ID_KEY, value).apply()
        }
    var phoneNumber: String?
        get() = pref.getString(USER_PHONE_NUMBER_KEY, "")
        set(value) {
            editor.putString(USER_PHONE_NUMBER_KEY, value).apply()
        }

    var username: String?
        get() = pref.getString(USER_USERNAME_KEY, "")
        set(value) {
            editor.putString(USER_USERNAME_KEY, value).apply()
        }

    fun clearVariables(vararg variableKey: String) {
        for (key in variableKey){
            editor.remove(key).apply()
        }

    }
}