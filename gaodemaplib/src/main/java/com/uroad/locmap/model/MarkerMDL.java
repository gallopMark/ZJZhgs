package com.uroad.locmap.model;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;

import java.io.Serializable;

public class MarkerMDL implements Serializable {

    /**
     * id
     */
    private String id;
    /**
     * 类型
     */
    private String type;
    /**
     * Marker 覆盖物的标题
     */
    private String title;
    /**
     * 纬度坐标
     */
    private double latitude;
    /**
     * 经度坐标
     */
    private double longitude;
    /**
     * 覆盖物的图标
     */
    private int icon;
    /**
     * 附加文本，显示在标题下方
     */
    private String snippet;
    /**
     * 设置当前标记的InfoWindow相对marker的偏移
     */
    private int offsetX;
    /**
     * 设置当前标记的InfoWindow相对marker的偏移
     */
    private int offsetY;
    /**
     * marker等级
     */
    private float zIndex;

    private Object tag;

    private Marker marker;

    public MarkerMDL() {

    }

    public void setMarker(Marker mk) {
        this.marker = mk;
    }

    public Marker getMarker() {
        return marker;
    }

    public MarkerMDL(int icon, double latitude, double longitude, String title,
                     String snippet, int offsetX, int offsetY) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.icon = icon;
        this.title = title;
        this.snippet = snippet;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public MarkerMDL(int icon, double latitude, double longitude, String title,
                     String snippet, String id, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.icon = icon;
        this.title = title;
        this.snippet = snippet;
        this.id = id;
        this.type = type;
    }


    public Object getTag() {
        return marker.getObject();
    }

    public void setTag(Object tag) {
        marker.setObject(tag);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
        marker.setIcon(BitmapDescriptorFactory.fromResource(icon));
    }

    public boolean isVisible() {
        return marker.isVisible();
    }

    public void setVisible(boolean visible) {
        this.marker.setVisible(visible);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getzIndex() {
        return zIndex;
    }

    public void setzIndex(float zIndex) {
        this.zIndex = zIndex;
        if (marker != null) {
            marker.setZIndex(zIndex);
        }
    }

    public void showInfoWindow() {
        if (!marker.isInfoWindowShown())
            this.marker.showInfoWindow();
    }

    public boolean isShowInfoWindow() {
        return marker.isInfoWindowShown();
    }

    public void hideInfoWindow() {
        if (marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
        }
    }

    public void destroy() {
        marker.destroy();
    }

    public void remove() {
        marker.remove();
    }
}
