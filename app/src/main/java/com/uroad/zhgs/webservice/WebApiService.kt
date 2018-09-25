package com.uroad.zhgs.webservice


/**
 *Created by MFB on 2018/7/27.
 */
class WebApiService {
    companion object {
        fun getBaseParams(): HashMap<String, String?> {
            return HashMap()
        }

        const val WELCOME_JPG = "getWelcomeJpg"
        //1.1 定位
        const val LOCATION = "96b3c57561d3399ce4362105c0d33031"

        fun locationParams(longitude: Double, latitude: Double): HashMap<String, String?> {
            return getBaseParams().apply {
                put("longitude", longitude.toString())
                put("latitude", latitude.toString())
            }
        }

        //1.13 帮助信息页面链接
        const val HELP_NEWS = "getHelpNews"

        //1.2 提交救援信息
        const val SUBMIT_RESCUE_INFO = "660bbaa3e20d2ba84e24b4eabf5823b9"

        //1.3 获取救援资费获取页面数据
        const val ACCESS_RESCUE_CHARGES = "98b38e232d4a702d4086538593763a75"

        //1.4 获取资费内容
        const val GET_FEE_CONTENT = "bf9d9ec00d84bf081f51ee0a3eee8c37"

        /**
         * type	服务类型	否
        cartype	车辆类别	否
        carweight	车辆类型	否
         */
        fun feeContentParams(type: String?, cartype: String?, carweight: String?): HashMap<String, String?> = getBaseParams().apply {
            put("type", type)
            put("cartype", cartype)
            put("carweight", carweight)
        }

        //1.6 救援请求表单
        const val RESCUE_REQUEST_FORM = "b58ebe6a130c9215297fa4d47b0e9172"

        fun requestForm(userid: String?, roadid: String?): HashMap<String, String?> = getBaseParams().apply {
            put("userid", userid)
            put("roadid", roadid)
        }

        const val CANCEL_RESCUE = "cancelRescue"
        fun cancelRescueParams(rescueid: String?): HashMap<String, String?> = getBaseParams().apply { put("rescueid", rescueid) }
        //1.17 检查救援情况
        const val CHECK_RESCUE = "checkRescue"

        fun checkRescueParams(userid: String?): HashMap<String, String?> = getBaseParams().apply { put("userid", userid) }

        const val RESCUE_DETAIL = "894ec681477d3ef154676920148a5798"

        fun rescueDetailParams(rescueid: String?): HashMap<String, String?> = getBaseParams().apply { put("rescueid", rescueid) }

        //1.9 获取评价跟对应的文本
        const val EVALUATE_TEXT = "bf9d9ec00d84bf081f51ee0a3eeas12"

        fun evaluateTextParams(codetype: String?): HashMap<String, String?> = getBaseParams().apply { put("codetype", codetype) }
        //1.10 提交评价
        const val COMMIT_EVALUATE = "bf9d9ec00d84bf081f51ee0a3elkj12"

        fun commitEvaluateParams(rescueid: String?, evaluate: String?, evaluatetag: String?, evaluateother: String?)
                : HashMap<String, String?> = getBaseParams().apply {
            put("rescueid", rescueid)
            put("evaluate", evaluate)
            put("evaluatetag", evaluatetag)
            put("evaluateother", evaluateother)
        }

        //1.11 救援记录列表
        const val RESCUE_RECORD = "bf9d9ec00d84bf081f51ee0a3elll98"

        fun rescueRecordParams(userid: String?): HashMap<String, String?> = getBaseParams().apply { put("userid", userid) }
        //1.12 支付信息
        const val RESCUE_PAY_INFO = "bf9d9ec00d84bf081f51ee0a96sasf5"

        fun rescuePayParams(rescueid: String?): HashMap<String, String?> = getBaseParams().apply { put("rescueid", rescueid) }

        //1.14 下单支付
        const val SIGN_PAY = "placeAnOrder"

        //1030001 微信 ； 1030002 支付宝
        fun signPayParams(rescueid: String?, paymethod: String?): HashMap<String, String?> = getBaseParams().apply {
            put("rescueid", rescueid)
            put("paymethod", paymethod)
        }

        //1.14 获取附近3条高速
        const val NEARBY_LOAD = "getNearbyRoad"

        fun nearbyLoadParams(longitude: Double, latitude: Double): HashMap<String, String?> = getBaseParams().apply {
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
        }

        //1.14填写发票
        const val SAVE_INVOICE = "saveInvoice"
        //1.15发票类型
        const val INVOICE_TYPE = "bf9d9ec00d84bf081f51ee0a3eeas12"

        fun invoiceTypeParams(codetype: String?): HashMap<String, String?> = getBaseParams().apply {
            put("codetype", codetype)
        }

        //1.15 保存报料信息
        const val SAVE_USER_EVENT = "saveUserEvent"

        fun saveUserEventParams(userid: String?, remark: String?, roadoldid: String?,
                                eventtype: String?, longitude: Double, latitude: Double, img_urls: String?)
                : HashMap<String, String?> = getBaseParams().apply {
            put("userid", userid)
            put("remark", remark)
            put("roadoldid", roadoldid)
            put("eventtype", eventtype)
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
            put("img_urls", img_urls)
        }

        // 1.16 点赞
        const val SUPPORT = "support"

        fun supportParams(eventuserid: String?, userid: String?, status: String?): HashMap<String, String?> = getBaseParams().apply {
            put("eventuserid", eventuserid)   //eventuserid	报料ID	否
            put("userid", userid) // userid	用户ID	否
            put("status", status) //状态	否	0 取消点赞 ， 1 点赞
        }

        //1.17 用户报料-评论
        const val EVENT_COMMENT = "userEventComment"

        fun eventCommentParams(eventid: String?, userid: String?, username: String?,
                               usercomment: String?, parentid: String?, touserid: String?,
                               tousername: String?) = getBaseParams().apply {
            put("eventid", eventid)
            put("userid", userid)
            put("username", username)
            put("usercomment", usercomment)
            if (parentid == null) put("parentid", "")
            else put("parentid", parentid)
            if (touserid == null) put("touserid", "")
            else put("touserid", touserid)
            if (tousername == null) put("tousername", "")
            else put("tousername", tousername)
        }

        //1.18 用户报料-列表
        const val USER_EVELT_LIST = "getUserEventList"
        const val REPORT_TYPE_MY = "my"
        /**
         * userid	用户ID
        index	当前页		1 开始
        size	每页显示的数量
        type	类型		默认全部；传 my 获取我的报料
         */
        fun userEventListParams(userid: String?, type: String?, index: Int, size: Int) = getBaseParams().apply {
            put("userid", userid)
            type?.let { put("type", it) }
            put("index", index.toString())
            put("size", size.toString())
        }

        //1.19 发送验证码
        const val PUSH_CODE = "pushCode"

        /**
         * type	验证类型	否	1 登录 ; 2 注册 ; 3 其它
        phone	手机号	否
         */
        fun pushCodeParams(phone: String?, type: String?) = getBaseParams().apply {
            put("phone", phone)
            put("type", type)
        }

        //1.20 注册
        const val USER_REGISTER = "userRegister"

        fun userRegisterParams(phone: String?, password: String?, code: String?) = getBaseParams().apply {
            put("phone", phone)
            put("password", password)
            put("code", code)
        }

        //1.21 登录
        const val USER_LOGIN = "userLogin"
        //1.22 找回密码
        const val RETRIEVE_PW = "retrievedPassWord"

        fun retrievedPwParams(phone: String?, password: String?, code: String?) = getBaseParams().apply {
            put("phone", phone)
            put("password", password)
            put("code", code)
        }

        /**
         * phone	手机号	否
        type	登录方式	否	1 密码 ； 2 验证码
        password	密码&验证码	否	密码登录就传md5后的内容；验证码登录就直接传验证码
         */
        fun userLoginParams(phone: String?, type: String?, password: String?) = getBaseParams().apply {
            put("phone", phone)
            put("type", type)
            put("password", password)
        }

        //1.22 新闻列表
        const val NEWS_LIST = "getNewsList"

        fun newsListParams(newstype: String?, size: Int, index: Int) = getBaseParams().apply {
            put("newstype", newstype)
            put("size", size.toString())
            put("index", index.toString())
        }

        //1.23 获取新闻列表上的新闻类型
        const val NEWS_TAB = "bf9d9ec00d84bf081f51ee0a3eeas12"

        fun newsTabParams() = getBaseParams().apply { put("codetype", "110") }

        //1.24 地图图层
        const val MAP_DATA = "getMapDataByType"

        /**
         * type	插点类型	否	1 收费站 ； 2 服务区 ； 3 景点 ； 4 加油站 ； 5 维修店 ; 6 天气 ； 7快拍 ； 8拥堵 ； 1006001 事故 ； 1006002 施工 ； 1006003 管制
        latitude	纬度	是
        longitude	经度	是
        visit	范围	是	默认是5KM
        entrance	入口	是	首页调用传 home ;其它传空
         */
        fun mapDataByTypeParams(type: String?, longitude: Double, latitude: Double, visit: String?, entrance: String?) = getBaseParams().apply {
            put("type", type)
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
            put("visit", visit)
            put("entrance", entrance)
        }

        // 1.25 高速列表
        const val HIGHWAY_LIST = "getRoadList"

        /**
         * longitude	经度	否
        latitude	纬度	否
        type	类型	否	1 附近高速 ； 2 全部高速
        keyword	关键字	是
        index	页码	否	初始值 1
        size	每页显示数	否
         */
        fun highwayListParams(longitude: Double, latitude: Double, type: Int, keyword: String?, index: Int, size: Int) = getBaseParams().apply {
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
            put("type", type.toString())
            put("keyword", keyword)
            put("index", index.toString())
            put("size", size.toString())
        }

        //1.26 事件列表
        const val HIGHWAY_EVENT_LIST = "getEventList"

        /**
         * eventid	事件ID	是
        roadid	路段ID	是
        eventtype	事件类型	是	1006001 事故 ；1006002 施工
        index	页码	否	初始值 1
        size	每页显示数量	否
         */
        fun eventListParams(roadid: String?, eventid: String?, eventtype: String?, index: String?, size: String?) = getBaseParams().apply {
            put("roadid", roadid)
            put("eventid", eventid)
            put("eventtype", eventtype)
            put("index", index)
            put("size", size)
        }

        //1.28 获取简图
        const val DIAGRAM = "getDiagram"
        //1.29 高速快览
        const val HIGHWAY_PREVIEW = "getRoadTraffic"

        /**
         * roadid	路段ID	否
        direction	方向	否	1 上行 ； 2 下行
         */
        fun roadTrafficParams(roadid: String?, direction: Int) = getBaseParams().apply {
            put("roadid", roadid)
            put("direction", direction.toString())
        }

        //1.30 绑定车辆
        const val BINDCAR = "bindingCarData"

        //1.31 我的车辆
        const val MYCAR = "myCar"

        fun myCarParams(userid: String?, carcategory: String?) = getBaseParams().apply {
            put("userid", userid)
            put("carcategory", carcategory)
        }

        //1.32 车辆详情
        const val CAR_DETAILS = "carDetails"

        fun carDetailsParams(carid: String?) = getBaseParams().apply { put("carid", carid) }

        //1.33 删除绑定车辆
        const val UPDATE_CAR_STATUS = "updateCarStatus"

        //0 无效 ； 1 有效
        fun deleteCarParams(carid: String?, status: String?) = getBaseParams().apply {
            put("carid", carid)
            put("status", status)
        }

        //1.34 获取个人信息
        const val USER_DATA = "getUserData"

        fun userDataParams(userid: String?) = getBaseParams().apply { put("userid", userid) }
        //1.35 修改个人信息
        const val UPDATE_USER_DATA = "updateUserData"

        /**
         * userid	用户ID	否
        username	用户名称	否
        iconfile	头像	否
         */
        fun updateUserData(userid: String?, username: String?, iconfile: String?) = getBaseParams().apply {
            put("userid", userid)
            put("username", username)
            put("iconfile", iconfile)
        }

        //1.36 完善信息
        const val PERFECT_DATA = "perfectData"

        fun perfectDataParams(userid: String?, name: String?, cardno: String?) = getBaseParams().apply {
            put("userid", userid)
            put("name", name)
            put("cardno", cardno)
        }

        //1.37 高速热线
        const val HOT_LINE = "roadHotPhone"

        /**
         * phonetype	电话类型	否	1130001 交警 ； 1130002 救援 ； 1130003 保险
        keyword	关键字	否
         */
        fun hotLineParams(phonetype: String?, keyword: String?) = getBaseParams().apply {
            put("phonetype", phonetype)
            put("keyword", keyword)
        }

        //1.38 修改密码
        const val CHANGE_PASSWORD = "updatePassword"

        fun updatePasswordParams(userid: String?, oldpassword: String?, newpassword: String?) = getBaseParams().apply {
            put("userid", userid)
            put("oldpassword", oldpassword)
            put("newpassword", newpassword)
        }

        //1.39 意见反馈
        const val FEEDBACK = "saveFeedback"

        /**
         * userid	用户ID	否
        content	内容	否
        picurls	图片链接	是
         */
        fun feedbackParams(userid: String?, content: String?, picurls: String?) = getBaseParams().apply {
            put("userid", userid)
            put("content", content)
            put("picurls", picurls)
        }

        //1.40 获取轴数
        const val AXIS_NUM = "bf9d9ec00d84bf081f51ee0a3eeas12"

        //codetype	类型	否	传 111
        fun axisNumParams(codetype: String?) = getBaseParams().apply { put("codetype", codetype) }

        //1.41 资讯详情
        const val NEWS_DETAIL = "getNewsDetails"

        fun newsDetailsParams(newsid: String?) = getBaseParams().apply {
            put("newsid", newsid)
        }

        //1.45 我的订阅
        const val USER_SUBSCRIBES = "getSubscribe"

        fun subscribeParams(userid: String?) = getBaseParams().apply { put("userid", userid) }
        //1.46 保存订阅
        const val SAVE_SUBSCRIBE = "saveSubscribe"

        /**
         * subtype	订阅类型
         * 1170001 突发事件 ： 1170002 计划施工 ； 1170003 管制事件 ； 1170004 拥堵
         */
        fun saveSubscribeParams(userid: String?, subtype: String?, dataid: String?) = getBaseParams().apply {
            put("userid", userid)
            put("subtype", subtype)
            put("dataid", dataid)
        }

        //1.47 删除订阅
        const val DELETE_SUBSCRIBE = "updateSubscribeStatus"

        fun deleteSubscribeParams(subscribeid: String?, status: String?) = getBaseParams().apply {
            put("subscribeid", subscribeid)
            put("status", status)
        }

        //1.48 消息中心
        const val USER_MSG = "getUserMsg"

        fun userMsgParams(userid: String?, index: Int, size: Int) = getBaseParams().apply {
            put("userid", userid)
            put("index", index.toString())
            put("size", size.toString())
        }

        //事件详情
        const val EVENT_DETAIL = "getEventDetailsById"

        fun eventDetailsByIdParams(eventids: String?, userid: String?) = getBaseParams().apply {
            put("eventids", eventids)
            put("userid", userid)
        }

        //快拍详情
        const val CCTV_DETAIL = "getCCTVDetailsById"

        fun cctvDetailParams(cctvids: String?) = getBaseParams().apply { put("cctvids", cctvids) }

        //1.49 快拍详情
        const val CCTV_DATA = "getCCTVData"

        fun cctvDataParams(roadid: String?, cctvids: String?) = getBaseParams().apply {
            put("roadid", roadid)
            put("cctvids", cctvids)
        }

        //服务区列表
        const val SERVICE_LIST = "getServiceList"

        fun serviceListParams(index: Int, size: Int, keyword: String?) = getBaseParams().apply {
            put("index", index.toString())
            put("size", size.toString())
            put("keyword", keyword)
        }

        //1.46 获取对应类型资讯
        const val NEWS_BY_TYPE = "getNewsByType"

        fun newsByTypeParams(newstype: String?) = getBaseParams().apply { put("newstype", newstype) }

        //首页资讯
        const val HOME_NEWS = "getHomeNews"
        //路径路费
        const val TOLL_GATE_LIST = "getTollGateList"

        fun tollGateListParams(keyword: String) = getBaseParams().apply { put("keyword", keyword) }

        const val QUERY_ROAD_TOLL = "getRouteFee"
        //路径路费查询
        /**
        startpoiid	开始站点ID
        endpoiid	结束站点ID
         */
        fun queryRoadTollParmas(startpoiid: String?, endpoiid: String?) = getBaseParams().apply {
            put("startpoiid", startpoiid)
            put("endpoiid", endpoiid)
        }

        //1.50 获取最新版本号
        const val APP_VERSION = "getVersionByType"

        fun appVersionParams(ver: String) = getBaseParams().apply {
            put("type", "android")
            put("ver", ver)
        }

        //获取请求流地址
        const val ROAD_VIDEO = "getRoadVideo"

        //resid	视频的resid
        fun roadVideoParams(resid: String?) = getBaseParams().apply { put("resid", resid) }
    }

}