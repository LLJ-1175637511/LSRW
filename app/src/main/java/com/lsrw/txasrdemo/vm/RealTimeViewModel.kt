package com.lsrw.txasrdemo.vm

import android.app.Application
import android.media.AudioFormat
import androidx.lifecycle.*
import com.lsrw.txasrdemo.audio.AudioOperaInterf
import com.lsrw.txasrdemo.audio.AudioRecorder
import com.lsrw.txasrdemo.enums.FileFormatType
import com.lsrw.txasrdemo.utils.LogUtils

class RealTimeViewModel(application: Application, private val savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application), LifecycleObserver, AudioOperaInterf {

    private val TAG = this.javaClass.simpleName

    private val audioRecord by lazy {
        AudioRecorder.Builder()
            .setChannel(AudioFormat.CHANNEL_IN_MONO)
            .setFormat(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(16000)
            .create()
    }

    fun getIsRecording(): MutableLiveData<Boolean> {
        if (!savedStateHandle.contains(boolIsRecording)) {
            savedStateHandle.set(boolIsRecording, false)
        }
        return savedStateHandle.getLiveData(boolIsRecording)
    }

    override fun startRecord(filePath: String, fileName: String) {
        if (audioRecord.getIsRecording()) {
            audioRecord.stopRecord()
            LogUtils.d(TAG, "保存成功")
        } else {
            audioRecord.startRecord(filePath,fileName)
        }
        //更新ui 提示当前录音状态 PS:先执行开启、关闭操作之后再更新ui
        getIsRecording().postValue(audioRecord.getIsRecording())
    }

//    override fun startRecordWav(){
//        startRecordWav()
//    }

    /**
     * 停止录音
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun stopRecord() {
        audioRecord.stopRecord()
    }

    /**
     * 释放录音资源
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun releaseRecord() {
        audioRecord.releaseRecord()
        LogUtils.d(TAG, "releaseRecord")
    }


    /**
     * 设置是否转化为wav格式
     */
    override fun setOutPutFileFormat(fft: FileFormatType) {
        audioRecord.setOutPutFileFormat(fft)
    }

    companion object {
        const val boolIsRecording = "RealTimeIsRecording"
    }
}