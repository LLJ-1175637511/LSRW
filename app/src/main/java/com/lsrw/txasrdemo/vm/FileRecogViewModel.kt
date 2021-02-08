package com.lsrw.txasrdemo.vm

import android.app.Application
import android.media.AudioFormat
import android.os.Environment
import androidx.lifecycle.*
import com.lsrw.txasrdemo.audio.AudioRecorder
import com.lsrw.txasrdemo.enums.AudioType
import com.lsrw.txasrdemo.enums.FileFormatType
import com.lsrw.txasrdemo.ui.contem.BaseARInterface
import com.lsrw.txasrdemo.utils.LogUtils
import com.lsrw.txasrdemo.utils.SignUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FileRecogViewModel(application: Application, private val savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application), BaseARInterface, LifecycleObserver {

    companion object {
        private const val taskId = "fr_task_id"
        private const val isRecording = "fr_is_recording"
        private const val contentText = "fr_content"
        private const val base64 = "fr_base64"
        private val str = "识别内容："
    }

    private val TAG = this.javaClass.simpleName

    private val mAudioRecorder by lazy {
        AudioRecorder.Builder()
            .setChannel(AudioFormat.CHANNEL_IN_MONO)
            .setFormat(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(16000)
            .create()
    }

    private var mFilePath =
        application.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.path.toString()
    private var mFileName = ""

    fun getContentLiveData(): MutableLiveData<String> {
        if (!savedStateHandle.contains(contentText)) {
            savedStateHandle.set(contentText, str)
        }
        return savedStateHandle.getLiveData<String>(contentText)
    }

    fun getAudioStateLiveData(): MutableLiveData<AudioType> {
        if (!savedStateHandle.contains(isRecording)) {
            savedStateHandle.set(isRecording, AudioType.PREPARE)
        }
        return savedStateHandle.getLiveData<AudioType>(isRecording)
    }

    fun getBase64LiveData(): MutableLiveData<String> {
        if (!savedStateHandle.contains(base64)) {
            savedStateHandle.set(base64, "")
        }
        return savedStateHandle.getLiveData<String>(base64)
    }

    fun getTaskIdLiveData(): MutableLiveData<Boolean> {
        if (!savedStateHandle.contains(taskId)) {
            savedStateHandle.set(taskId, false)
        }
        return savedStateHandle.getLiveData<Boolean>(taskId)
    }

    override fun startAR() {
        if (getAudioStateLiveData().value == AudioType.PREPARE || getAudioStateLiveData().value == AudioType.STOP) {
            mAudioRecorder.startRecord(filePath = mFilePath, fileName = mFileName)
            changeAudioState(AudioType.START)
            LogUtils.d(TAG, "开始录音")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun pauseAR() {
        if (getAudioStateLiveData().value == AudioType.START || getAudioStateLiveData().value == AudioType.RESUME) {
            changeAudioState(AudioType.PAUSE)
            mAudioRecorder.pauseAR()
            LogUtils.d(TAG, "暂停录音")
        }
    }

    override fun resumeAR() {
        if (getAudioStateLiveData().value == AudioType.PAUSE) {
            changeAudioState(AudioType.RESUME)
            mAudioRecorder.resumeAR()
            LogUtils.d(TAG, "恢复录音")
        }
    }

    override fun stopAR() {
        if (getAudioStateLiveData().value == AudioType.START || getAudioStateLiveData().value == AudioType.RESUME) {
            mAudioRecorder.stopRecord()
            changeAudioState(AudioType.STOP)
            LogUtils.d(TAG, "停止录音")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun releaseAR() {
        mAudioRecorder.releaseRecord()
    }

    private fun changeAudioState(newState: AudioType) {
        getAudioStateLiveData().postValue(newState)
    }

    fun setFileName(fileName: String) {
        this.mFileName = fileName
    }

    fun setFileFormat(fft: FileFormatType) {
        mAudioRecorder.setOutPutFileFormat(fft)
    }

    private fun getVoiceBase64() {
        thread {
            try {
                val fd = FileInputStream(getSavedFileName())
                val buffInputStream = BufferedInputStream(fd)
                val byteArray = buffInputStream.readBytes()
                buffInputStream.close()
                val base64 = SignUtils.toBase64(byteArray)
                getBase64LiveData().postValue(base64)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getSavedFileName() = mAudioRecorder.getFileName()

    fun sendRequest() {
        /*val result = viewModelScope.launch {
             suspendCoroutine<String> { cotinuation ->
                OkHttpClient().newCall(Request.Builder().url("https:www.baidu.com").build())
                    .enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            cotinuation.resume(e.message.toString())
                        }

                        override fun onResponse(call: Call, response: Response) {
                            cotinuation.resume(response.body.toString())
                        }
                    })
            }
        }*/
    }
}