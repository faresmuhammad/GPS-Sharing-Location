package com.fares.gpssharinglocation.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Profile(
    val name: String,
    val user_id: String
) {
    constructor() : this("", "")
}