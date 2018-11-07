package com.uroad.zhgs.cluster

import com.amap.api.maps.model.LatLng

class CustomClusterItem(private val latLng: LatLng,
                        private val smallIcon: Int,
                        private val bigIcon: Int) : ClusterItem {
    private var `object`: Any? = null

    constructor(latLng: LatLng,
                smallIcon: Int,
                bigIcon: Int,
                `object`: Any) : this(latLng, smallIcon, bigIcon) {
        this.`object` = `object`
    }

    override fun getPosition(): LatLng = latLng

    override fun getMarkerSmallIcon(): Int = smallIcon

    override fun getMarkerBigIcon(): Int = bigIcon

    fun getObject() = `object`
}