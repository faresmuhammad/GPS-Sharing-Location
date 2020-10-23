package com.fares.gpssharinglocation.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var user_id: String,
    val phoneNumber: String,
    val username: String
) {




    var user_code: String = ""

    constructor() : this("", "", "")




    override fun toString(): String {
        return "User(user_id= $user_id, phoneNumber= $phoneNumber, username= $username)"
    }
}