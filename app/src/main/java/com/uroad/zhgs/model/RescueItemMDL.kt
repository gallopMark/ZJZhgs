package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/4.
 * 救援记录
 * "rescueno": "201808040332428",
"place": "",
"rescuetype": "故障",
"statusname": "接警",
"status": "1070001",
"paymoney": null,
"evaluatestatus": null,
"invoiceid": null,
"paystatus": null,
"ispay": 1,
"iscomment": 1,
"isinvoice": 0
 * rescueno	救援编号
rescuetype	救援类型
statusname	工单状态名称
status	工单状态	1070001 接警 ； 1070002 已派遣；1070003 进行中；1070004 已结束；1070005 已取消
paymoney	金额
place	救援地址
ispay	是否已支付	0 否 ； 1 是
iscomment	是否已评论	0 否 ； 1 是
isinvoice	是否已开票	0 否 ； 1 是
 */
class RescueItemMDL {
    var rescueid: String? = null
    var rescueno: String? = null
    var place: String? = null
    var rescuetype: String? = null
    var statusname: String? = null
    var status: String? = null
    var paymoney: String? = null
    var evaluatestatus: String? = null
    var invoiceid: String? = null
    var paystatus: String? = null
    var rescue_address: String? = null
    var ispay: Int = 0
    var iscomment: Int = 0
    var isinvoice: Int = 0

    enum class Type(val code: String) {
        //1070001 接警 ； 1070002 已派遣；1070003 进行中；1070004 已结束；1070005 已取消
        ALARM("1070001"),
        DISPATCHED("1070002"),
        ONGOING("1070003"),
        FINISHED("1070004"),
        CALCELLED("1070005")
    }
}