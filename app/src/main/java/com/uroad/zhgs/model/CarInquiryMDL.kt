package com.uroad.zhgs.model

/**
 * @author MFB
 * @create 2018/11/20
 * @describe archivesNumber	车辆档案编号
license	车牌
licenseColor	车牌颜色
ascriptionCity	车牌归属地市
category	客货车类型
categotyNum	客货车类型（0客车，1货车）
vehicleClass	车辆类别
vehicleClassNum	车辆类别	1-1类型；2-2类型；3-3类型；4-4类型；5-5类型；6-6类型；7-7类型；0 未识别
axialType	轴型
axleLimit	轴限
creditRank	车辆诚信等级
sinceScore	车辆诚信分值
 */
class CarInquiryMDL {
    var archivesNumber: String? = null
    var license: String? = null
    var licenseColor: String? = null
    var ascriptionCity: String? = null
    var category: String? = null
    var categotyNum: String? = null
    var vehicleClass: String? = null
    var vehicleClassNum: String? = null
    var axialType: String? = null
    var axleLimit: String? = null
    var creditRank: String? = null
    var sinceScore: String? = null
}