package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/24.
 */
class InvoiceTypeMDL {
    var type: MutableList<Type>? = null

    class Type {
        var dictcode: String? = null
        var dictname: String? = null
        var sontype: MutableList<SonType>? = null

        class SonType {
            var dictcode: String? = null
            var dictname: String? = null
        }
    }
}