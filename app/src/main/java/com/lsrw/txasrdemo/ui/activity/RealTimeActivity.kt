package com.lsrw.txasrdemo.ui.activity

import android.media.AudioFormat
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.lsrw.txasrdemo.R
import com.lsrw.txasrdemo.audio.AudioRecorder
import com.lsrw.txasrdemo.utils.LogUtils
import java.io.File

class RealTimeActivity:AppCompatActivity() {

    private val TAG = this.javaClass.simpleName

    private val audioRecord by lazy { AudioRecorder.Builder()
        .setChannel(AudioFormat.CHANNEL_IN_MONO)
        .setFormat(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(16000)
        .create()
    }

    private var filePath:File ? =null

    private lateinit var btStart:Button
    private lateinit var cbIsOutputWav:CheckBox


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realtime)

        btStart = findViewById(R.id.bt_record_start_real)
        cbIsOutputWav = findViewById(R.id.cb_is_wav_real_time)

        btStart.setOnClickListener {
            startRecord()
        }

        cbIsOutputWav.setOnClickListener {
            audioRecord.setIsConvertWav(cbIsOutputWav.isChecked)
        }
    }

    private fun startRecord(){
        filePath = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (filePath==null) return
        val fileName = "real.pcm"
        LogUtils.d(TAG,"$filePath/$fileName")
        if (audioRecord.getIsRecording()){
            btStart.text ="开始录音"
            audioRecord.stopRecord()
            LogUtils.d(TAG,"保存成功")
        } else {
            btStart.text = "停止录音"
            audioRecord.startRecordPcm("$filePath/$fileName")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (audioRecord.getIsRecording()) audioRecord.stopRecord()
        audioRecord.releaseRecord()
    }

}