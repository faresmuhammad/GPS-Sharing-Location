package com.fares.gpssharinglocation.utils.map_markers

import com.fares.gpssharinglocation.model.User
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


data class ClusterMarker(
    var point:LatLng,
    private val title: String,
    private val snippet: String,
    val user: User
) : ClusterItem {


    override fun getPosition(): LatLng = point
    override fun getTitle(): String? = title

    override fun getSnippet(): String? = snippet

}

