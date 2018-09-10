package com.uroad.locmap;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.uroad.locmap.model.LocOptionMDL;
import com.uroad.locmap.model.LocationMDL;
import com.uroad.locmap.model.PoiInfoMDL;
import com.uroad.locmap.model.RegeocodeResultMDL;
import com.uroad.locmap.widget.HintDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class LocationHelper {

    private LocationHelper(Context ct) {
        super();
        this.ct = ct;
    }

    protected boolean enableLocation = false;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private Context ct;
    GeocodeSearch geocoderSearch;

    private static LocationHelper locationhelper;
    Locationlistener llistener;
    OnGeocodeSearchListener geocodeSearchListener;

    public static LocationHelper getInstance(Context ct) {
        if (locationhelper == null) {
            locationhelper = new LocationHelper(ct);
        }
        return locationhelper;
    }

    private AMapLocationListener locationlistener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            // TODO Auto-generated method stub
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                if (llistener != null) {
                    LocationMDL location = new LocationMDL();
                    location.setCity(amapLocation.getCity());
                    location.setCityCode(amapLocation.getCityCode());
                    location.setCountry(amapLocation.getCountry());
                    location.setDistrict(amapLocation.getDistrict());
                    location.setLatitude(amapLocation.getLatitude());
                    location.setLongitude(amapLocation.getLongitude());
                    location.setStreet(amapLocation.getStreet());
                    location.setAddrStr(amapLocation.getAddress());
                    location.setProvince(amapLocation.getProvince());
                    location.setSpeed(amapLocation.getSpeed());
                    llistener.locationComplete(location);
                }

            } else {
                if (llistener != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("定位失败!");
                    if (amapLocation != null) {
                        sb.append("errorCode:" + amapLocation.getErrorCode()).append("\n");
                        sb.append("errorInfo:" + amapLocation.getErrorInfo()).append("\n");
                    }
                    llistener.locationFail(sb.toString());
                }
            }
        }

    };

    /**
     * 关闭定位
     */
    public void closeLocation() {
        if (null != mlocationClient) {
            /*销毁定位,释放定位资源, 当不再需要进行定位时调用此方法 该方法会释放所有定位资源，调用后再进行定位需要重新实例化AMapLocationClient*/
            mlocationClient.onDestroy();
            mlocationClient = null;//将mlocationClient赋值成空，下次调用定位才会重新实例化

            //不用onDestroy的话，可以用stopLocation
//			mlocationClient.stopLocation();
        }
    }

    /**
     * 自定义参数开启定位
     *
     * @param op
     */
    public void openLocation(LocOptionMDL op) {


        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(ct);
            mLocationOption = new AMapLocationClientOption();
            if (op != null) {
                mLocationOption.setInterval(op.getScanSpan());
            }
            //设置定位监听
            mlocationClient.setLocationListener(locationlistener);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        } else {
            mlocationClient.startLocation();
        }


    }


    /**
     * 使用默认配置开启定位
     */
    public void openLocation() {
        openLocation(null);

    }

    /**
     * @param latitude
     * @param longitude
     * @param radius     查找范围。默认值为1000，取值范围1-3000，单位米。
     * @param latLonType 输入参数坐标类型。包含GPS坐标和高德坐标。
     * @return
     */
    public void getRegeocodeAddress(double latitude, double longitude,
                                    float radius, String latLonType, OnGeocodeSearchListener listener) {
        this.geocodeSearchListener = listener;
        geocoderSearch = new GeocodeSearch(ct);
        geocoderSearch
                .setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {

                    @Override
                    public void onRegeocodeSearched(RegeocodeResult arg0,
                                                    int arg1) {
                        // TODO Auto-generated method stub
//						if (arg1 == 0) {
                        if (arg0 != null) {

                            if (arg0.getRegeocodeAddress() != null
                                    && arg0.getRegeocodeAddress()
                                    .getFormatAddress() != null) {
                                List<PoiItem> items = arg0
                                        .getRegeocodeAddress().getPois();
                                String[] names = null;
                                List<PoiInfoMDL> poiInfoMDLs = new ArrayList<PoiInfoMDL>();
                                if (items != null && items.size() > 0) {
                                    names = new String[arg0
                                            .getRegeocodeAddress()
                                            .getPois().size() + 1];
                                    names[0] = arg0.getRegeocodeAddress()
                                            .getFormatAddress();

                                    for (int i = 0; i < items.size(); i++) {
                                        names[i + 1] = items.get(i)
                                                .getTitle();
                                        PoiInfoMDL mdl = new PoiInfoMDL();
                                        mdl.setCity(items.get(i).getCityName());
                                        mdl.setAddress(items.get(i).getSnippet());
                                        mdl.setPhoneNum(items.get(i).getTel());
                                        mdl.setName(items.get(i).getTitle());
                                        mdl.setLatitude(items.get(i).getLatLonPoint().getLatitude());
                                        mdl.setLongitude(items.get(i).getLatLonPoint().getLongitude());
                                        poiInfoMDLs.add(mdl);
                                    }
                                } else {
                                    names = new String[1];
                                    names[0] = arg0.getRegeocodeAddress()
                                            .getFormatAddress();
                                }
                                geocodeSearchListener
                                        .onRegeocodeSearched(names);

                                RegeocodeResultMDL regeocodeResultMDL = new RegeocodeResultMDL();
                                regeocodeResultMDL.setBuilding(arg0.getRegeocodeAddress()
                                        .getBuilding());
                                regeocodeResultMDL.setDistrict(arg0.getRegeocodeAddress()
                                        .getDistrict());
                                regeocodeResultMDL.setFormatAddress(arg0
                                        .getRegeocodeAddress()
                                        .getFormatAddress());
                                regeocodeResultMDL.setPois(poiInfoMDLs);
                                geocodeSearchListener
                                        .onRegeocodeSearched(regeocodeResultMDL);
                            }
                        } else {
                            geocodeSearchListener
                                    .onRegeocodeSearchedFail("其他地址为空");
                        }
//						} else {
//							geocodeSearchListener
//									.onRegeocodeSearchedFail("获取地址失败，请重试");
//						}
                    }

                    @Override
                    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
                        // TODO Auto-generated method stub

                    }
                });
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        LatLonPoint poi = new LatLonPoint(latitude, longitude);
        RegeocodeQuery query = new RegeocodeQuery(poi, radius, latLonType);
        geocoderSearch.getFromLocationAsyn(query);

    }

    public static double getDistance(double startlon, double startlat,
                                     double endlon, double endlat) {
        LatLng start = new LatLng(startlat, startlon);
        LatLng end = new LatLng(endlat, endlon);
        return AMapUtils.calculateLineDistance(start, end);
    }

    public interface Locationlistener {
        void locationComplete(LocationMDL location);

        void locationFail(String msg);
    }

    public interface OnGeocodeSearchListener {
        void onRegeocodeSearched(String[] names);

        void onRegeocodeSearchedFail(String msg);

        void onRegeocodeSearched(RegeocodeResultMDL mdl);
    }

    public void setLocationListener(Locationlistener lolistener) {
        this.llistener = lolistener;
    }
}
