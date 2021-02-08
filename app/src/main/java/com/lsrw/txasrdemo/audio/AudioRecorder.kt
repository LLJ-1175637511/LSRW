package com.lsrw.txasrdemo.audio

import android.media.AudioRecord
import android.media.MediaRecorder
import com.lsrw.txasrdemo.enums.AudioType
import com.lsrw.txasrdemo.enums.FileFormatType
import com.lsrw.txasrdemo.utils.LogUtils
import com.lsrw.txasrdemo.utils.PcmToWavUtil
import java.io.*
import kotlin.concurrent.thread

class AudioRecorder private constructor(builder: Builder) : AudioOperaInterf {
    // 音频源：音频输入-麦克风
    private var audioInput = MediaRecorder.AudioSource.MIC
    private var mFilePathName = ""
    private var mFileName = ""
    private var mFilePath = ""
    private var mIsFinish = false
    private var mAudioState = AudioType.PREPARE
    private var mAudioRecord: AudioRecord? = null
    private var mChannel: Int
    private var mFormat: Int
    private var mSampleRate: Int
    private var mBufferSize: Int = 6400
    private var mOutputFileFormat = FileFormatType.PCM


    private val TAG = this.javaClass.simpleName

    init {
        builder.getConfig().let {
            mChannel = it.channel
            mFormat = it.format
            mSampleRate = it.sampleRate
        }
        //最小可采集音频数据大小 方便对接腾讯云 故自定义设置
//        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mFormat)
        mAudioRecord = AudioRecord(audioInput, mSampleRate, mChannel, mFormat, mBufferSize)
    }

    override fun startRecord(filePath: String, fileName: String) {
        //默认输出标准pcm模式
//            if (mOutputFileFormat == FileFormatType.PCM) "${fileName}.pcm"
//        else "${fileName}.wav"
        this.mFilePath = filePath
        this.mFileName = fileName
        mFilePathName = "${this.mFilePath}/${this.mFileName}.pcm"
        LogUtils.d(TAG, "mFilePathName：${mFilePathName}")
        if (!checkFileIsExist()) return //如果文件夹 或文件
        mIsFinish = false
        mAudioState = AudioType.START
        startRecordThread()
    }

    private fun checkFileIsExist(): Boolean {
        val checkFileDir = File(mFilePath)
        val checkDirBool = if (!checkFileDir.exists()) {
            checkFileDir.mkdirs()
        }else true

        val checkFile = File(mFilePathName)
        val checkFileBool = if (!checkFileDir.exists()) {
             checkFile.createNewFile()
        }else true
        return checkDirBool && checkFileBool
    }

    fun pauseAR() {
        mAudioState = AudioType.PAUSE
    }

    fun resumeAR() {
        mAudioState = AudioType.RESUME
    }

    private fun startRecordThread() {
        thread {
            val mFileOutputStream = FileOutputStream(mFilePathName)
            mAudioRecord?.startRecording()
            LogUtils.d(TAG, "startRecording。。。。")
            var logTime = 0
            try {
                val byteArray = ByteArray(mBufferSize)
                while (mAudioState != AudioType.STOP) {
                    if (mAudioState != AudioType.PAUSE) {
                        val read = mAudioRecord?.read(byteArray, 0, mBufferSize)
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            mFileOutputStream.write(byteArray)
                            mFileOutputStream.flush()
                        }
                        if (byteArray.size > 200) {
                            logTime++
                            LogUtils.d(TAG, "$logTime 次：${byteArray.size}")
                        }
                    }
                }
                /*while (mIsFinish) {
                    val read = mAudioRecord?.read(byteArray, 0, mBufferSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        mFileOutputStream.write(byteArray)
                        mFileOutputStream.flush()
                        if (byteArray.size > 200) {
                            logTime++
                            LogUtils.d(TAG, "$logTime 次：${byteArray.size}")
                        }
                    }
                }*/
                mAudioRecord?.stop()
                LogUtils.d(TAG, "stopRecording。。。。")
                mFileOutputStream.flush()
                mFileOutputStream.close()
                //如果设置输出wav 则该线程在格式转换后再终止
                if (mOutputFileFormat == FileFormatType.WAV) {
                    convertToWav()
                }
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
     * 注意：该方法尽量在子线程中使用
     */
    private fun convertToWav() {
        val fileName = "${this.mFilePath}/${this.mFileName}.wav"
        LogUtils.d(TAG, "mWavFileName:$fileName")
        try {
            val wavFile = File(fileName)
            if (!wavFile.exists()) {
                wavFile.createNewFile()
            }
            PcmToWavUtil.pcmToWav(
                mSampleRate.toLong(),
                mChannel,
                mBufferSize,
                mFilePathName,
                fileName
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun stopRecord() {
        mAudioState = AudioType.STOP
    }

    fun getIsRecording() = mIsFinish

    override fun setOutPutFileFormat(fft: FileFormatType) {
        this.mOutputFileFormat = fft
    }

    override fun releaseRecord() {
        mAudioRecord?.stop()
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

    fun getFileName() = mFilePathName
}