package com.lkh.corner.init

import android.app.Application

object Global {

    var appOrNull : Application? = null

    fun init(application: Application) {
        Global.application = application
        appOrNull = application
    }

    lateinit var application: Application


    fun getApp() : Application? {
        return appOrNull
    }

}