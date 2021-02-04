package com.lsrw.txasrdemo.ui.contem

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.Chronometer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.lsrw.txasrdemo.utils.SRUtils

class SRChronometer:Chronometer,LifecycleObserver,BaseSRInterface {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private val rd by lazy {
        SRUtils()
    }

    private var elapseTime = 0L

    fun initSavePath(path: String) {
        rd.initMR(path)
    }

    fun getSavedPath() = rd.getSavedPath()

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun pauseSR(){
        elapseTime = SystemClock.elapsedRealtime() - base
        stop()
        rd.pauseSR()
    }

    override fun resumeSR(){
        base = SystemClock.elapsedRealtime() - elapseTime
        start()
        rd.resumeSR()
    }

    override fun startSR(){
        base = SystemClock.elapsedRealtime()
        start()
        rd.startSR()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun stopSR(){
        base = SystemClock.elapsedRealtime()
        elapseTime = 0
        stop()
        rd.stopSR()
    }
}