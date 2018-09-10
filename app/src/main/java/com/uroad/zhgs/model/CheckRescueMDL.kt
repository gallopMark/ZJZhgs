package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/31.
 * 参数项	名称	备注
status	状态	0 可以申请救援；1 存在未支付救援记录；2 存在进行中救援
msg	说明
rescueid	救援ID	status字段为0时，该字段为空！
 */
class CheckRescueMDL {
    var status: Int? = -1
    var msg: String? = null
    var rescueid: String? = null
}