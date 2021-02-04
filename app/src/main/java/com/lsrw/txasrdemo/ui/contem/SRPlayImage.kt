package com.lsrw.txasrdemo.ui.contem

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.lsrw.txasrdemo.R
import com.lsrw.txasrdemo.enum.MediaState

class SRPlayImage:androidx.appcompat.widget.AppCompatImageView,BaseSRInterface,LifecycleObserver {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var isPlaying = false

    fun getPlayState() = isPlaying

    private fun mediaControl() {
        if (isPlaying)setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        else setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
    }

    override fun startSR() {
        isPlaying = true
        mediaControl()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun pauseSR() {
        isPlaying = false
        mediaControl()
    }

    override fun resumeSR() {
        isPlaying = true
        mediaControl()
    }

    override fun stopSR() {
        isPlaying = false
        mediaControl()
    }
}