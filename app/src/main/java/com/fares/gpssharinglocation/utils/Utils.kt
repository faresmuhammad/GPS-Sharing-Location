package com.fares.gpssharinglocation.utils

import android.text.TextUtils
import android.widget.EditText

object Utils {

    @JvmStatic
    fun validatePhoneNumber(editPhone: EditText, phoneNumber: String): Boolean {
        if (TextUtils.isEmpty(phoneNumber)) {
            editPhone.error = "Invalid phone number."
            return false
        }

        return true
    }

    @JvmStatic
    fun validateUsername(editText: EditText, username: String): Boolean {
        if (TextUtils.isEmpty(username)) {
            editText.error = "Invalid username"
            return false
        }
        return true
    }
}