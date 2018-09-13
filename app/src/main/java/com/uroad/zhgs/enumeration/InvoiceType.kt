package com.uroad.zhgs.enumeration

/**
 * @author MFB
 * @create 2018/9/12
 * @describe 发票类型
 */
enum class InvoiceType(val code: String) {
    INVOICE_COMMON("1150001"),  //普通发票
    INVOICE_SPECIAL("1150002"), //专票
    INVOICE_HEAD_PERSONAL("1160001"), //个人 抬头
    INVOICE_HEAD_COMPANY("1160002 ") //企业抬头
}