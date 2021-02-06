package com.lsrw.txasrdemo.ui.activity

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.lsrw.txasrdemo.R
import com.lsrw.txasrdemo.audio.AudioRecorder
import com.lsrw.txasrdemo.utils.LogUtils
import kotlinx.android.synthetic.main.activity_audio_record.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

class AudioActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName
    // 音频源：音频输入-麦克风
    private val AUDIO_INPUT = MediaRecorder.AudioSource.MIC

    // 采样率
    // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private val AUDIO_SAMPLE_RATE = 16000

    // 音频通道 单声道
    private val AUDIO_CHANNEL: Int = AudioFormat.CHANNEL_IN_MONO

    // 音频格式：PCM编码
    private val AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT

    private val permissions = arrayOf<String>(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val MY_PERMISSIONS_REQUEST = 1001
    private var pcmFileName: String = ""
    private var wavFileName: String = ""
    private var audioRecord: AudioRecord? = null // 声明 AudioRecord 对象

    private var recordBufSize = 0 // 声明recoordBufffer的大小字段

    private var buffer = byteArrayOf()
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST)

        bt_start_audio.setOnClickListener {
            pcmFileName = "${externalCacheDir}/record.pcm"
            LogUtils.d(TAG,"pcmFileName:$pcmFileName")
            wavFileName = "$externalCacheDir/record1.wav"
            start()
        }
        bt_stop_audio.setOnClickListener {
            stop()
        }

        val audio = AudioRecorder.Builder()
            .setFormat(AudioFormat.ENCODING_PCM_16BIT)
            .setChannel(AudioFormat.CHANNEL_IN_MONO)
            .setSampleRate(16000)
            .create()

    }

    private fun stop() {
        isRecording = false
        if (null != audioRecord) {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    private fun start() {
        //audioRecord能接受的最小的buffer大小
        recordBufSize =
            AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING)
        audioRecord = AudioRecord(
            AUDIO_INPUT,
            AUDIO_SAMPLE_RATE,
            AUDIO_CHANNEL,
            AUDIO_ENCODING,
            recordBufSize
        )
        buffer = ByteArray(recordBufSize)
        audioRecord?.startRecording()
        isRecording = true

        thread(isDaemon = true){
            var os: FileOutputStream? = null

            try {
                if (!File(pcmFileName).exists()) {
                    File(pcmFileName).createNewFile()
                }
                os = FileOutputStream(pcmFileName)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            os?.let{
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, recordBufSize)

                    // 如果读取音频数据没有出现错误，就将数据写入到文件
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            os.write(buffer)
                            LogUtils.d(TAG,"buffer size:${buffer.size}")
                            os.flush()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                }
                try {
                    os.flush()
                    os.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}