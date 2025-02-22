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

        /**
         * userid	用户ID	否
        remark	爆料内容	否
        roadoldid	路段ID	否
        eventtype	爆料类型	否	1015001 拥堵，1015002 事故，1015003 施工，1015004 遗洒，1015005 积水，1015006 管制
        longitude	经度	否
        latitude	纬度	否
        img_urls	图片、视频、语音的URL	是	多张 逗号分隔
        category	报料类型	否	1-图片，2-视频，3-语音
        remark1	备注	是	视频的时候保留第一帧图片，语音的时候保留时间，图片传空字符串
         */
        fun saveUserEventParams(userid: String?, remark: String?, roadoldid: String?,
                                eventtype: String?, longitude: Double, latitude: Double,
                                img_urls: String?, category: Int, remark1: String?)
                : HashMap<String, String?> = getBaseParams().apply {
            put("userid", userid)
            put("remark", remark)
            put("roadoldid", roadoldid)
            put("eventtype", eventtype)
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
            put("img_urls", img_urls)
            put("category", category.toString())
            put("remark1", remark1)
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

        /**
         * userid	用户ID
        index	当前页		1 开始
        size	每页显示的数量
        type	类型		默认全部；传 my 获取我的报料 ;传 myfollow 获取我的关注
         */
        fun userEventListParams(userid: String?, longitude: Double, latitude: Double, type: String?, index: Int, size: Int) = getBaseParams().apply {
            put("userid", userid)
            type?.let { put("type", it) }
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
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

        fun userRegisterParams(phone: String?, password: String?, code: String?, regrequestcode: String?) = getBaseParams().apply {
            put("phone", phone)
            put("password", password)
            put("code", code)
            put("regrequestcode", regrequestcode)
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
            put("lastmobilever", 2.toString())
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
        fun feedbackParams(userid: String?, content: String?, picurls: String?, fdtype: String?) = getBaseParams().apply {
            put("userid", userid)
            put("content", content)
            put("picurls", picurls)
            put("fdtype", fdtype)
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

        //是否有用
        const val SAVE_IS_USEFUL = "isUseful"

        /**
         * 参数项	名称	是否可为空	备注
        eventid	事件ID	否
        userid	用户ID	否
        isuseful	是否有用	否	1-有用，2-无用，3-两者都没选择
         */
        fun isUsefulParams(eventid: String?, userid: String?, isuseful: Int) = getBaseParams().apply {
            put("eventid", eventid)
            put("userid", userid)
            put("isuseful", isuseful.toString())
        }

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

        const val TRAFFIC_JAM_DETAIL = "getJamEventDetailsById"

        fun trafficJamDetailsByIdParams(jamids: String?, userid: String?) = getBaseParams().apply {
            put("jamids", jamids)
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

        fun serviceListParams(index: Int, size: Int, keyword: String?, longitude: Double, latitude: Double) = getBaseParams().apply {
            put("index", index.toString())
            put("size", size.toString())
            put("keyword", keyword)
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
        }

        //1.46 获取对应类型资讯
        const val NEWS_BY_TYPE = "getNewsByType"

        fun newsByTypeParams(newstype: String?) = getBaseParams().apply { put("newstype", newstype) }

        //首页资讯
        const val HOME_NEWS = "getHomeNews"
        //路径路费
        const val TOLL_GATE_LIST = "getTollGateList"

        fun tollGateListParams(keyword: String, longitude: Double, latitude: Double) = getBaseParams().apply {
            put("keyword", keyword)
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
        }

        const val QUERY_ROAD_TOLL = "getRouteFee"
        //路径路费查询
        /**
        startpoiid	开始站点ID
        endpoiid	结束站点ID
         */
        fun queryRoadTollParams(startpoiid: String?, endpoiid: String?) = getBaseParams().apply {
            put("startpoiid", startpoiid)
            put("endpoiid", endpoiid)
        }

        //1.50 获取最新版本号
        @Deprecated("use getAppConfig")
        const val APP_VERSION = "getVersionByType"

        fun appVersionParams(ver: String) = getBaseParams().apply {
            put("type", "android")
            put("ver", ver)
        }

        //获取请求流地址
        const val ROAD_VIDEO = "getRoadVideo"

        //resid	视频的resid videotype hls；rtmp 默认 rtmp
        fun roadVideoParams(resid: String?) = getBaseParams().apply {
            put("resid", resid)
            put("videotype", "hls")
        }

        const val CLOSE_VIDEO = "closeVideo"

        fun closeVideoParams(rtmpIp: String?) = getBaseParams().apply { put("rtmpIp", rtmpIp) }


        /******************************第二期开发***************************************/
        /*车队接口*/
        //1、是否有车队或者邀请
        const val CHECK_RIDERS = "checkCarTeamSituation"

        fun checkRidersParams(userid: String?) = getBaseParams().apply { put("userid", userid) }
        //2、创建&修改车队
        const val CREATE_CAR_TEAM = "creatCarTeam"

        /**
         * teamid	车队ID	是	新增不用传
        toplace	目的地	否
        teamname	车队名	否
        longitude	目的地的经度	否
        latitude	目的地的维度	否
        teamleader	创建人ID	否
        user_longitude	用户的经度	否
        user_latitude	用户的维度	否
         */
        fun createCarTeamParams(teamid: String?, toplace: String?, teamname: String?,
                                longitude: Double, latitude: Double, teamleader: String?) = getBaseParams().apply {
            put("teamid", teamid)
            put("toplace", toplace)
            put("teamname", teamname)
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
            put("teamleader", teamleader)
        }

        //3、加入车队
        const val JOIN_CAR_TEAM = "joinCarTeam"

        /**
         * 参数项	名称	是否可为空	备注
        intoken	进入口令	否
        userid	请求加入的用户ID	否
        longitude	用户的经度	否
        latitude	用户的维度	否
         */
        fun joinCarTeamParams(intoken: String?, userid: String?, longitude: Double, latitude: Double) = getBaseParams().apply {
            put("intoken", intoken)
            put("userid", userid)
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
        }

        //4、车队详情
        const val CAR_TEAM_DETAIL = "getCarTeamData"

        fun getCarTeamDataParams(teamid: String?) = getBaseParams().apply { put("teamid", teamid) }
        fun getCarTeamDataParams2(intoken: String?) = getBaseParams().apply { put("intoken", intoken) }
        //5、车队列表
        const val CAR_TEAM_LIST = "inviteList"

        fun carTeamListParams(userid: String?, keyword: String?) = getBaseParams().apply {
            put("userid", userid)
            put("keyword", keyword)
        }
        //6、用户关注

        const val UPDATE_FOLLOW_STATUS = "updateFollowStatus"
        fun updateFollowParams(userid: String?, followuserid: String?, status: Int) = getBaseParams().apply {
            put("userid", userid)
            put("followuserid", followuserid)
            put("status", status.toString())
        }

        //8、解散车队
        const val DISSOLUTION_RIDERS = "dissolutionCarTeam"

        fun dissolutionCarTeamParams(teamid: String?) = getBaseParams().apply { put("teamid", teamid) }

        //9、拒绝邀请
        const val REFUSE_INVITE = "refuseInvitation"

        fun refuseInviteParams(userid: String?) = getBaseParams().apply { put("userid", userid) }

        //10、邀请用户
        const val INVITE_RIDERS = "inviteUser"

        /**
         * teamid	车队ID	否
        rquserid	被邀请用户ID	否	多个逗号分隔
        userid	用户ID	否
         */
        fun inviteRidersParams(teamid: String?, rquserid: String?, userid: String?) = getBaseParams().apply {
            put("teamid", teamid)
            put("rquserid", rquserid)
            put("userid", userid)
        }

        /*足迹接口*/
        //1、保存足迹
        const val SAVE_TRACKS = "saveMyFootprint"

        /**
         * userid	用户ID	否
        province	省份	否
        city	城市	否
        district	城区	否
        street	街道	否
        citycode	城市编号	否
        adcode	区域编号	否
        address	地址	否
         */
        fun saveTracksParams(userid: String?, province: String?, city: String?
                             , district: String?, street: String?, citycode: String?
                             , adcode: String?, address: String?) = getBaseParams().apply {
            put("userid", userid)
            put("province", province)
            put("city", city)
            put("district", district)
            put("street", street)
            put("citycode", citycode)
            put("adcode", adcode)
            put("address", address)
        }

        //2.我的足迹
        const val MY_FOOTPRINT = "getMyFootprint"

        fun getMyFootprintParams(userid: String?) = getBaseParams().apply { put("userid", userid) }

        //用户设置
        const val USER_SETUP = "userSetUp"

        //isfollow	是否能被关注	否	0 关闭 ； 1 开启
        fun userSetupParams(userid: String?, isfollow: Int) = getBaseParams().apply {
            put("userid", userid)
            put("isfollow", isfollow.toString())
        }

        //获取我的邀请码
        const val MY_REQUEST_CODE = "getRequestCode"

        fun getMyCodeParams(userid: String?) = getBaseParams().apply { put("userid", userid) }

        //基础数据
        const val SYS_CONFIG = "getSysConfig"

        //APP配置版本
        const val APP_CONFIG = "getAppConfig"

        //获取菜单
        const val MAIN_MENU = "getMainMenu"

        //通行记录
        const val PASS_RECORD = "getCurrentRecordData"

        /**
         * 参数项	名称	是否可为空	备注
        carno	车牌号	否
        startdate	开始日期	否	格式 yyyymm
        enddate	结束日期	否	格式 yyyymm
        type	车辆类型	否	1000002 客车 ； 1000003 货车
         */
        fun passRecordParams(carno: String?, startdate: String?, enddate: String?, type: String?) = getBaseParams().apply {
            put("carno", carno)
            put("startdate", startdate)
            put("enddate", enddate)
            put("type", type)
        }

        /*分享接口*/
        const val SHARE_LIST = "getShareList"

        fun shareListParams(userid: String?) = getBaseParams().apply { put("userid", userid) }

        /*诚信信息*/
        const val CAR_INQUIRY = "getSincerity"

        fun sincerityParams(license: String?, licenseColor: String?) = getBaseParams().apply {
            put("license", license)
            put("licenseColor", licenseColor)
        }

        /*有赞登录接口*/
        const val PRAISE_LOGIN = "loginYZ"

        fun praiseLoginParams(useruuid: String?, device_id: String?, extra: String?) = getBaseParams().apply {
            put("useruuid", useruuid)
            put("device_id", device_id)
            put("extra", extra)
        }

        const val PRAISE_INIT = "initTokenYZ"

        /*进行中活动*/
        const val MAIN_ACTIVITY = "getActivity"

        /*活动详情*/
        const val ACTIVITY_DETAIL = "getActivityDetails"

        fun activityDetailParams(activityid: String?, useruuid: String?) = getBaseParams().apply {
            put("activityid", activityid)
            put("useruuid", useruuid)
        }

        /*我的成果*/
        const val MY_HARVEST = "getMyHarvest"

        fun myHarvestParams(useruuid: String?, activityid: String?) = getBaseParams().apply {
            put("useruuid", useruuid)
            put("activityid", activityid)
        }

        /*站点管制详情*/
        const val POI_SITE_CONTROL = "getPoiControlDetails"

        fun poiSiteControlParams(stationcode: String?, longitude: Double, latitude: Double) = getBaseParams().apply {
            put("stationcode", stationcode)
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
        }

        const val AUTH_VER = "funcAuthentication"

        const val NEW_FUNCTION_CONTENT = "getNewFunctionContent"

        fun newFuncContentParams(con_ver: String?) = getBaseParams().apply { put("con_ver", con_ver) }

        const val FEEDBACK_TYPE = "getfbtype"

        const val APPEAL_LIST = "getComplaintData"

        fun appealListParams(phone: String?) = getBaseParams().apply { put("phone", phone) }

        const val SERVICE_ICON = "getServiceAreaIcon"
    }

}