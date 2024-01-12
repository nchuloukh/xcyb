package com.lkh.circularprogressbar

import android.app.Activity
import android.os.Bundle
import com.lkh.circularrectprogresslayout.widgets.CircularRectProgressView
import java.util.Timer
import java.util.TimerTask

class MainActivity : Activity() {
    private var mTimer: Timer? = null
    private var mProgress = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initCountdownService()
    }
    private fun initCountdownService() {
        try {
            if (mTimer != null) {
                mTimer?.cancel()
                mTimer = null
            }
            mTimer = Timer(true)
            mProgress = 0
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    runOnUiThread{
                        if(mProgress==100){
                            mTimer?.cancel()
                            mTimer = null
                            return@runOnUiThread
                        }
                        mProgress++
                        findViewById<CircularRectProgressView>(R.id.mRecProgress).setProgress(mProgress)
                    }
                }
            }
            mTimer?.schedule(task, 0,200)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
