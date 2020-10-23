package com.fares.gpssharinglocation.model

import com.google.firebase.firestore.GeoPoint

data class UserLocation(
    val user: User,
    val geo_point: GeoPoint
) {
    constructor() : this(User(), GeoPoint(0.0, 0.0))

    override fun toString(): String {
        return "UserLocation( user = $user, geo_point= $geo_point)"
    }
}