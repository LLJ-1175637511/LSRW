package com.lsrw.txasrdemo.ui.activity

import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.lsrw.txasrdemo.R
import com.lsrw.txasrdemo.enums.AudioType
import com.lsrw.txasrdemo.enums.FileFormatType
import com.lsrw.txasrdemo.utils.LogUtils
import com.lsrw.txasrdemo.vm.RealTimeViewModel
import java.io.File

class RealTimeActivity : AppCompatActivity() {

    private val TAG = this.javaClass.simpleName

    private lateinit var viewModel: RealTimeViewModel

    private var filePath: String? = null

    private lateinit var btStart: Button
    private lateinit var cbIsOutputWav: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realtime)

        btStart = findViewById(R.id.bt_record_start_real)
        cbIsOutputWav = findViewById(R.id.cb_is_wav_real_time)

        viewModel = ViewModelProvider(
            this,
            SavedStateViewModelFactory(application, this)
        )[RealTimeViewModel::class.java]

        viewModel.getIsRecording().observe(this, Observer { isRecording ->
            if (!isRecording) {
                btStart.text = "开始录音"
                LogUtils.d(TAG, "开始录音")
            } else {
                btStart.text = "停止录音"
                LogUtils.d(TAG, "停止录音")
            }
        })

        lifecycle.addObserver(viewModel)

        btStart.setOnClickListener {
            filePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.path
            if (filePath == null) return@setOnClickListener
            val fileName = "real"
            viewModel.startRecord(filePath.toString(),fileName)
        }

        cbIsOutputWav.setOnClickListener {
            if (cbIsOutputWav.isChecked){
                viewModel.setOutPutFileFormat(FileFormatType.WAV)
            }else{
                viewModel.setOutPutFileFormat(FileFormatType.PCM)
            }
        }
    }
}