package com.uroad.zhgs.cluster

import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import kotlin.collections.ArrayList

class Cluster(private val latLng: LatLng) {
    private var icon: Int = 0
    private var bigIcon: Int = 0
    private var mClusterItems = ArrayList<ClusterItem>()
    private var mMarker: Marker? = null
    private var `object`: Any? = null

    fun addClusterItem(clusterItem: ClusterItem) {
        mClusterItems.add(clusterItem)
    }

    fun getClusterCount(): Int {
        return mClusterItems.size
    }

    fun getCenterLatLng(): LatLng {
        return latLng
    }

    fun setMarker(marker: Marker) {
        mMarker = marker
    }

    fun getMarker(): Marker? {
        return mMarker
    }

    fun getClusterItems(): List<ClusterItem> {
        return mClusterItems
    }

    fun getIcon(): Int {
        return icon
    }

    fun setIcon(icon: Int) {
        this.icon = icon
    }

    fun setBigIcon(bigIcon: Int) {
        this.bigIcon = bigIcon
    }

    fun getBigIcon() = bigIcon

    fun setObject(`object`: Any?) {
        this.`object` = `object`
    }

    fun getObject(): Any? {
        return `object`
    }
}