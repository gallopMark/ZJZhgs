package com.uroad.zhgs.model

import android.text.TextUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author MFB
 * @create 2018/11/1
 * @describe 通行记录
 *  "c_license": "浙G135UF",
"n_en_station_id": "1153",
"n_en_date": "20150903",
"n_en_time": "124220",
"n_ex_station_id": "1129",
"n_ex_date": "20150903",
"n_ex_time": "142556",
"c_category": "0",
"c_ex_vehicle_class": "1",
"c_ex_vehicle_type": "1",
"n_en_station_name": "窑上",
"n_ex_station_name": "上溪"
 */
class PassRecordMDL {
    var c_license: String? = null
    var n_en_station_id: String? = null
    var n_en_date: String? = null
    var n_en_time: String? = null
    var n_ex_station_id: String? = null
    var n_ex_date: String? = null
    var n_ex_time: String? = null
    var c_category: String? = null
    var c_ex_vehicle_class: String? = null
    var c_ex_vehicle_type: String? = null
    var n_en_station_name: String? = null
    var n_ex_station_name: String? = null
    var money: Double? = null
    var d_fee_length: String? = null

    fun getEnDateTime(): String {
        var text = ""
        n_en_date?.let {
            try {
                val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val date = format.parse(it)
                text += SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            } catch (e: Exception) {
            }
        }
        if (!TextUtils.isEmpty(text)) text += "\u2000"
        n_en_time?.let {
            if (it.length >= 6) {
                text += "${it.substring(0, 2)}:"
                text += it.substring(2, 4)
            } else if (it.length == 5) {
                text += "${it.substring(0, 1)}:"
                text += it.substring(1, 3)
            }
        }
        return text
    }

    fun getExDateTime(): String {
        var text = ""
        n_ex_date?.let {
            try {
                val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val date = format.parse(it)
                text += SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            } catch (e: Exception) {
            }
        }
        if (!TextUtils.isEmpty(text)) text += "\u2000"
        n_ex_time?.let {
            if (it.length >= 6) {
                text += "${it.substring(0, 2)}:"
                text += it.substring(2, 4)
            } else if (it.length == 5) {
                text += "${it.substring(0, 1)}:"
                text += it.substring(1, 3)
            }
        }
        return text
    }
//
//    fun getMoney(): String {
//        money?.let {
//            val df = DecimalFormat(".00")
//            return "${df.format(it)}元"
//        }
//        return "0.00元"
//    }
}