package com.lsrw.txasrdemo.ui.contem

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.Chronometer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class ARChronometer:Chronometer,LifecycleObserver,BaseARInterface {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private var elapseTime = 0L

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun pauseAR(){
        elapseTime = SystemClock.elapsedRealtime() - base
        stop()
    }

    override fun resumeAR(){
        base = SystemClock.elapsedRealtime() - elapseTime
        start()
    }


    override fun startAR() {
        base = SystemClock.elapsedRealtime()
        start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun stopAR() {
        base = SystemClock.elapsedRealtime()
        elapseTime = 0
        stop()
    }
}