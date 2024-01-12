package com.lkh.circularrectprogresslayout.consts

import android.app.Application
import android.content.Context

/**
 * Created by XC.Li
 * desc:
 */
class AppContext : Application() {
    companion object {
        var app: Application? = null
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        app = this
        Global.init(this)
    }

}