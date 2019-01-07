package com.uroad.zhgs.model


class FeedbackTypeMDL {
    enum class Type(var CODE: String) {
        ROAD("1250001"),
        OTHER("1250002")
    }

    var dictcode: String? = null
    var dictname: String? = null

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is FeedbackTypeMDL -> false
            else -> this === other || dictcode == other.dictcode
        }
    }

    override fun hashCode(): Int {
        return 31 + (dictcode?.hashCode() ?: 0)
    }
}