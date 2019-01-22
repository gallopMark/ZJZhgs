package com.uroad.zhgs.model

import android.content.Context
import android.support.v4.content.ContextCompat
import com.uroad.zhgs.R

/**
 * @author MFB
 * @create 2019/1/19
 * @describe 申诉data
 * 返回参数说明
参数项	名称	备注
license	车牌号码
licenseColor	车牌颜色code
licenseColorName	车牌颜色
appear	申诉人
appealTime	申诉时间
illegalTime	违规时间
illegalLocale	违规地点
illeaglType	违规类型code
illeaglTypeName	违规类型
auditTime	审核时间
auditStatus	审核状态code
auditStatusName	审核状态
auditDescription	审核描述
licenseColor参数说明

车牌颜色（对应关系如下
0 蓝
1 黄
2 黑
3 白
4 渐变绿
5 黄绿双拼
6 蓝白渐变
9 手工输入）
illeaglType参数说明
违规类型（对应关系如下
101 车牌不符
102 车型不符
103 超载超限
104 轴型不符
105 交通违章
106 车辆甩挂
201 路径异常
202 抛洒滴漏
203 U、J型车
204 方向异常
205 短时掉头
206 短途重载
207 长途轻载
208 时间超短
209 时间超长
210 时间重叠
211 车辆冲卡
212 中途换卡
301 卡不可读
302 磁卡损坏
303 磁卡丢失
304 长期静默
305 异常启用
306 卡无标识
307 非免通行
401 假行驶证
402 军警车辆
403 免优车辆
404 集卡车辆
405 绿通车辆
501 违规办理
502 违规使用
601 肇事逃离
602 盗窃逃离）

auditStatus参数说明
审核状态（对应关系如下
1 待审核
2 审核中
3 成功
4 失败）
 */
class AppealMDL {
    var license: String? = null
    var licenseColor: String? = null
    var licenseColorName: String? = null
    var appear: String? = null
    var appealTime: String? = null
    var illegalTime: String? = null
    var illegalLocale: String? = null
    var illeaglType: String? = null
    var illeaglTypeName: String? = null
    var auditTime: String? = null
    var auditStatus: Int? = 0
    var auditStatusName: String? = null
    var auditDescription: String? = null
    //待审核fab41e
    //审核中049bed
    //完成12d464
    //失败eb1f1f
    fun color(context: Context): Int {
        if (auditStatus == 1) return ContextCompat.getColor(context, R.color.ToBeAudited)
        if (auditStatus == 2) return ContextCompat.getColor(context, R.color.InAudit)
        if (auditStatus == 3) return ContextCompat.getColor(context, R.color.AuditFinish)
        if (auditStatus == 4) return ContextCompat.getColor(context, R.color.AuditFailure)
        return ContextCompat.getColor(context, R.color.transparent)
    }
}