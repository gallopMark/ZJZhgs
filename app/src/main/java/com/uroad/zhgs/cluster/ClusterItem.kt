package com.uroad.zhgs.cluster

import com.amap.api.maps.model.LatLng

interface ClusterItem {
    fun getPosition(): LatLng
    fun getMarkerSmallIcon(): Int
    fun getMarkerBigIcon(): Int
}