package com.fares.gpssharinglocation.utils.map_markers

import android.content.Context
import androidx.core.content.ContextCompat
import com.fares.gpssharinglocation.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator


class ClusterMarkerRenderer(
    val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<ClusterMarker>
) : DefaultClusterRenderer<ClusterMarker>(context, map, clusterManager) {

    private val iconGenerator= IconGenerator(context.applicationContext)

    override fun onBeforeClusterItemRendered(item: ClusterMarker, markerOptions: MarkerOptions) {
        iconGenerator.setBackground(
            ContextCompat.getDrawable(context, R.drawable.ic_location_pin)
        )
        val icon = iconGenerator.makeIcon()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<ClusterMarker>): Boolean {
        return false
    }

    fun updateMarker(clusterMarker: ClusterMarker) = getMarker(clusterMarker)?.let {
        it.position = clusterMarker.position
    }
}


