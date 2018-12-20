package com.uroad.zhgs.helper

import android.content.Context

/**
 * @author MFB
 * @create 2018/12/19
 * @describe 保存路径导航页面按钮选中状态
 */
class RoadNaviLayerHelper {
    companion object {
        private const val FILE_ROAD_NAVI = "road_navi"
        /*地图模式按钮状态*/
        private const val MAP_ACCIDENT = "map_accident" //地图事故按钮 默认打开（true）
        private const val MAP_CONSTRUCTION = "map_construction" //地图施工按钮 默认关闭（false）
        private const val MAP_JAM = "map_jam"   //地图拥堵按钮  默认开启（true）
        private const val MAP_CONTROL = "map_control"   //地图道路管制  默认开启(true)
        private const val MAP_BAD_WEATHER = "map_badWeather" //地图恶劣天气  默认开启（true）
        private const val MAP_TRAFFIC_ACCIDENT = "map_trafficAcc"   //地图交通事件  默认关闭（false）
        private const val MAP_MONITOR = "map_monitor"   //地图监控  默认开启（true）
        private const val MAP_WEATHER = "map_weather" //地图天气    默认关闭（false）
        /*------------------------------------*/
        /*简图模式按钮状态*/
        private const val DIAGRAM_ACCIDENT = "diagram_accident" //简图事故按钮 默认开启（true）
        private const val DIAGRAM_CONSTRUCTION = "diagram_construction" //简图施工按钮 默认关闭（false）
        private const val DIAGRAM_JAM = "diagram_jam"   //简图拥堵按钮 默认开启（true）
        private const val DIAGRAM_CONTROL = "diagram_control"   //简图道路管制 默认开启（true）
        private const val DIAGRAM_SITE_CONTROL = "diagram_siteControl"  //简图站点管子 默认开启（true）
        private const val DIAGRAM_BAD_WEATHER = "diagram_badWeather"    //简图恶劣天气 ，默认开启（true）
        private const val DIAGRAM_TRAFFIC_ACCIDENT = "diagram_trafficAcc"   //简图交通事件 默认关闭（false）
        private const val DIAGRAM_PILE = "diagram_pile" //简图桩号按钮 默认关闭（false）
        private const val DIAGRAM_TOLL = "diagram_toll" //简图收费站按钮 默认开启（true）
        private const val DIAGRAM_SERVICE = "diagram_service"   //简图服务区按钮 默认开启（true）
        private const val DIAGRAM_MONITOR = "diagram_monitor"   //简图监控按钮 默认开启（true）

        private fun getSharedPreferences(context: Context) = context.getSharedPreferences(FILE_ROAD_NAVI, Context.MODE_PRIVATE)

        fun isMapAccidentChecked(context: Context) = getSharedPreferences(context).getBoolean(MAP_ACCIDENT, true)
        fun onMapAccidentChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(MAP_ACCIDENT, isChecked).apply()
        fun isMapConstructionChecked(context: Context) = getSharedPreferences(context).getBoolean(MAP_CONSTRUCTION, false)
        fun onMapConstructionChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(MAP_CONSTRUCTION, isChecked).apply()
        fun isMapJamChecked(context: Context) = getSharedPreferences(context).getBoolean(MAP_JAM, true)
        fun onMapJamChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(MAP_JAM, isChecked).apply()
        fun isMapControlChecked(context: Context) = getSharedPreferences(context).getBoolean(MAP_CONTROL, true)
        fun onMapControlChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(MAP_CONTROL, isChecked).apply()
        fun isMapBadWeatherChecked(context: Context) = getSharedPreferences(context).getBoolean(MAP_BAD_WEATHER, true)
        fun onMapBadWeatherChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(MAP_BAD_WEATHER, isChecked).apply()
        fun isMapTrafficAccChecked(context: Context) = getSharedPreferences(context).getBoolean(MAP_TRAFFIC_ACCIDENT, false)
        fun onMapTrafficAccChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(MAP_TRAFFIC_ACCIDENT, isChecked).apply()
        fun isMapMonitorChecked(context: Context) = getSharedPreferences(context).getBoolean(MAP_MONITOR, true)
        fun onMapMonitorChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(MAP_MONITOR, isChecked).apply()
        fun isMapWeatherChecked(context: Context) = getSharedPreferences(context).getBoolean(MAP_WEATHER, false)
        fun onMapWeatherChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(MAP_WEATHER, isChecked).apply()
        /*------------------------------------*/
        fun isDiagramAccidentChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_ACCIDENT, true)
        fun onDiagramAccidentChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_ACCIDENT, isChecked).apply()
        fun isDiagramConstructionChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_CONSTRUCTION, false)
        fun onDiagramConstructionChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_CONSTRUCTION, isChecked).apply()
        fun isDiagramJamChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_JAM, true)
        fun onDiagramJamChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_JAM, isChecked).apply()
        fun isDiagramControlChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_CONTROL, true)
        fun onDiagramControlChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_CONTROL, isChecked).apply()
        fun isDiagramSiteControlChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_SITE_CONTROL, true)
        fun onDiagramSiteControlChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_SITE_CONTROL, isChecked).apply()
        fun isDiagramBadWeatherChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_BAD_WEATHER, true)
        fun onDiagramBadWeatherChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_BAD_WEATHER, isChecked).apply()
        fun isDiagramTrafficAccChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_TRAFFIC_ACCIDENT, false)
        fun onDiagramTrafficAccChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_TRAFFIC_ACCIDENT, isChecked).apply()
        fun isDiagramPileChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_PILE, false)
        fun onDiagramPileChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_PILE, isChecked).apply()
        fun isDiagramTollChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_TOLL, true)
        fun onDiagramTollChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_TOLL, isChecked).apply()
        fun isDiagramServiceChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_SERVICE, true)
        fun onDiagramServiceChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_SERVICE, isChecked).apply()
        fun isDiagramMonitorChecked(context: Context) = getSharedPreferences(context).getBoolean(DIAGRAM_MONITOR, true)
        fun onDiagramMonitorChecked(context: Context, isChecked: Boolean) = getSharedPreferences(context).edit().putBoolean(DIAGRAM_MONITOR, isChecked).apply()
    }
}