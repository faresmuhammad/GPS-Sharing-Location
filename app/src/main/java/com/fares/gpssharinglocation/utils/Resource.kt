package com.fares.gpssharinglocation.utils

sealed class Resource<T> {
    open var data: T? = null
    open var message: String = ""

    data class Success<T>(override var data: T?, override var message: String) : Resource<T>()
    data class Loading<T>(override var data: T?, override var message: String) : Resource<T>()
    data class Error<T>(override var data: T?, override var message: String) : Resource<T>()
}