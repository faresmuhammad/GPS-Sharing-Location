package com.fares.gpssharinglocation.utils

object Constants {

    /**
     * Constants for shared preferences
     */
    const val SHARED_PREFERENCES_KEY = "shared_preferences_key"
    const val USER_ID_KEY = "user_id_key"
    const val USER_PHONE_NUMBER_KEY = "user_phone_number_key"
    const val USER_USERNAME_KEY = "user_username_key"


    /**
     * Constants for firestore
     */
    const val USER_ID_FIELD = "user_id"
    const val USER_GEO_POINT_FIELD = "geo_point"
    const val USER_INVITATION_CODE = "user_code"
    const val PROFILES_COLLECTION = "Profiles"
    const val USERS_COLLECTION = "Users"
    const val USERS_LOCATIONS_COLLECTION = "Users Locations"

    /**
     * Constants for permissions
     */
    const val REQUEST_CODE = 123
    const val ACCESS_FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION
    const val ACCESS_COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION

    /**
     * Constants for location service
     */
    const val UPDATE_INTERVAL: Long = 10 * 1000
    const val FASTEST_INTERVAL: Long = 5 * 1000
    const val CHANNEL_ID = "service_channel"
    const val ERROR_DIALOG_REQUEST = 401
    const val PERMISSIONS_REQUEST_ENABLE_GPS = 402

    /**
     * Constants for intents
     */
    const val PROFILE_NAME_INTENT_EXTRA = "profile_name"
    const val PROFILE_BUNDLE_TAG = "ProfileBundle"

    const val LISTENER = "listenmap"
}