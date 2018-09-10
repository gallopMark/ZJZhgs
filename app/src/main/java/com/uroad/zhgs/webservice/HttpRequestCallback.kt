package com.uroad.zhgs.webservice

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by MFB on 2018/6/5.
 */
abstract class HttpRequestCallback<T> {
    var mType: Type

    init {
        val superclass = javaClass.genericSuperclass
        mType = if (superclass is ParameterizedType) superclass.actualTypeArguments[0] else Any::class.java
    }

    open fun onPreExecute() {}
    abstract fun onSuccess(data: T?)
    abstract fun onFailure(e: Throwable, errorMsg: String?)
    open fun onComplete() {}
}
