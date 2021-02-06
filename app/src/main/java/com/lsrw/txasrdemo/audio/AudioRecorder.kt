package com.lsrw.txasrdemo.audio

import android.media.AudioRecord
import android.media.MediaRecorder
import com.lsrw.txasrdemo.utils.LogUtils
import com.lsrw.txasrdemo.utils.PcmToWavUtil
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

class AudioRecorder(builder: Builder) {
    // 音频源：音频输入-麦克风
    private var audioInput = MediaRecorder.AudioSource.MIC
    private var mPcmFileName = ""
    private var mWavFileName = ""
    private var isRecord = false
    private var mAudioRecord: AudioRecord? = null
    private var mChannel: Int
    private var mFormat: Int
    private var mSampleRate: Int
    private var mBufferSize: Int = 6400 //腾讯云接口 一次传输6400字节
    private var isConvertToWav = false

    private val TAG = this.javaClass.simpleName

    init {
        builder.getConfig().let {
            mChannel = it.channel
            mFormat = it.format
            mSampleRate = it.sampleRate
        }
//        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mFormat)
        mAudioRecord = AudioRecord(audioInput, mSampleRate, mChannel, mFormat, mBufferSize)
    }

    fun startRecordPcm(pcmFileName: String) {
        if (!pcmFileName.contains(".pcm")) return //输出标准pcm模式 检查文件名
        if (pcmFileName.length <= 4) return //错误的路径
        this.mPcmFileName = pcmFileName
        isRecord = true
        startRecordThread()
    }

    private fun startRecordThread() {
        thread(isDaemon = true) {
            mAudioRecord?.startRecording()
            val checkFile = File(mPcmFileName)
            if (!checkFile.exists()) {
                checkFile.createNewFile()
            }
            var logTime = 0
            val fos = FileOutputStream(mPcmFileName)
            try {
                val byteArray = ByteArray(mBufferSize)
                while (isRecord) {
                    val read = mAudioRecord?.read(byteArray, 0, mBufferSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        fos.write(byteArray)
                        LogUtils.d(
                            TAG,
                            "buffer size:${byteArray.size}"
                        )
                        fos.flush()
                        if (byteArray.size > 200) {
                            logTime++
                            LogUtils.d(
                                TAG,
                                "$logTime 次：${byteArray.size}"
                            )
                        }
                    }
                }
                mAudioRecord?.stop()
                fos.flush()
                fos.close()
                //如果设置输出wav 则该线程在格式转换后终止
                if (isConvertToWav) convertToWav()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } finally {

            }
        }
    }

    /**
     * pcm格式转wav格式
     * 注意：该方法必须在线程中使用
     */
    private fun convertToWav() {
        if (!mPcmFileName.contains(".pcm")) return
        mWavFileName = "${mPcmFileName.substring(0, mPcmFileName.lastIndex - 3)}.wav"
        LogUtils.d(TAG, "mWavFileName:$mWavFileName")
        try {
            val wavFile = File(mWavFileName)
            if (!wavFile.exists()) {
                wavFile.createNewFile()
            }
            PcmToWavUtil.pcmToWav(
                mSampleRate.toLong(),
                mChannel,
                mBufferSize,
                mPcmFileName,
                mWavFileName
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecord() {
        isRecord = false
    }

    fun getIsRecording() = isRecord

    fun setIsConvertWav(isConvertToWav:Boolean){
        this.isConvertToWav = isConvertToWav
    }

    fun releaseRecord() {
        mAudioRecord?.release()
        mAudioRecord = null
    }

    class Builder {
        data class AutoConfig(var sampleRate: Int, var channel: Int, var format: Int)
        // 采样率
        // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
        // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
        private var audioSampleRate = 0

        // 音频通道 单声道
        private var audioChannel: Int = 0

        // 音频格式：PCM编码
        private var audioFormat: Int = 0

        fun setSampleRate(audioSampleRate: Int): Builder {
            this.audioSampleRate = audioSampleRate
            return this
        }

        fun setChannel(audioChannel: Int): Builder {
            this.audioChannel = audioChannel
            return this
        }

        fun setFormat(audioFormat: Int): Builder {
            this.audioFormat = audioFormat
            return this
        }

        fun getConfig() =
            AutoConfig(
                sampleRate = audioSampleRate,
                channel = audioChannel,
                format = audioFormat
            )

        fun create(): AudioRecorder {
            return AudioRecorder(this)
        }
    }

}