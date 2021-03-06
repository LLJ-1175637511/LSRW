package com.lsrw.txasrdemo.ui.contem

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.Chronometer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.lsrw.txasrdemo.utils.ARUtils

class SRChronometer:Chronometer,LifecycleObserver,BaseARInterface {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private val rd by lazy {
        ARUtils()
    }

    private var elapseTime = 0L

    fun initSavePath(path: String) {
        rd.initMR(path)
    }

    fun getSavedPath() = rd.getSavedPath()

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun pauseAR(){
        elapseTime = SystemClock.elapsedRealtime() - base
        stop()
        rd.pauseAR()
    }

    override fun resumeAR(){
        base = SystemClock.elapsedRealtime() - elapseTime
        start()
        rd.resumeAR()
    }

    override fun startAR(){
        base = SystemClock.elapsedRealtime()
        start()
        rd.startAR()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun stopAR(){
        base = SystemClock.elapsedRealtime()
        elapseTime = 0
        stop()
        rd.stopAR()
    }
}