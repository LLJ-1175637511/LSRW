package com.lsrw.txasrdemo.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.lsrw.txasrdemo.R
import com.lsrw.txasrdemo.databinding.ActivityFileRecongBinding
import com.lsrw.txasrdemo.enums.AudioType
import com.lsrw.txasrdemo.enums.FileFormatType
import com.lsrw.txasrdemo.ui.contem.ARChronometer
import com.lsrw.txasrdemo.ui.contem.ARPlayImage
import com.lsrw.txasrdemo.vm.FileRecogViewModel
import kotlinx.android.synthetic.main.activity_file_recong.*

class FileRecogActivity : AppCompatActivity() {

    private lateinit var chronometer: ARChronometer
    private lateinit var viewModel: FileRecogViewModel
    private lateinit var playImage: ARPlayImage
    private lateinit var isOutPutWav:CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityFileRecongBinding>(
            this,
            R.layout.activity_file_recong
        )
        viewModel = ViewModelProvider(
            this,
            SavedStateViewModelFactory(application, this)
        )[FileRecogViewModel::class.java]

        chronometer = binding.srChronometer
        playImage = binding.ivOperaSr
        isOutPutWav = binding.cbOutWavFr

        binding.lifecycleOwner = this
        binding.vm = viewModel

        lifecycle.addObserver(chronometer)
        lifecycle.addObserver(viewModel)
        lifecycle.addObserver(playImage)

        bt_sr_start.setOnClickListener {
            viewModel.startAR()
        }

        bt_sr_stop.setOnClickListener {
            viewModel.stopAR()
        }

        viewModel.getContentLiveData().observe(this, Observer {
            tv_recong_content_sr.text = it
        })

        viewModel.getAudioStateLiveData().observe(this, Observer { state ->
            changeState(state)
        })

        viewModel.getBase64LiveData().observe(this, Observer {
            Toast.makeText(this,"保存成功：\n${viewModel.getSavedFileName()}",Toast.LENGTH_SHORT).show()
        })

        playImage.setOnClickListener {
            when(viewModel.getAudioStateLiveData().value){
                AudioType.RESUME -> viewModel.pauseAR()
                AudioType.PAUSE -> viewModel.resumeAR()
                AudioType.START -> viewModel.pauseAR()
                else -> { }
            }
        }

        isOutPutWav.setOnClickListener {
            if (isOutPutWav.isChecked){
                viewModel.setFileFormat(FileFormatType.WAV)
            }else{
                viewModel.setFileFormat(FileFormatType.PCM)
            }
        }
        viewModel.setFileName("fr")
    }

    private fun changeState(state: AudioType) {
        when(state){
            AudioType.START ->{
                chronometer.startAR()
                playImage.startAR()
                bt_sr_start.visibility = View.INVISIBLE
                bt_sr_stop.visibility = View.VISIBLE
                playImage.visibility = View.VISIBLE
            }
            AudioType.PAUSE ->{
                playImage.pauseAR()
                chronometer.pauseAR()
            }
            AudioType.RESUME ->{
                playImage.resumeAR()
                chronometer.resumeAR()
            }
            AudioType.STOP ->{
                chronometer.stopAR()
                playImage.stopAR()
                bt_sr_start.visibility = View.VISIBLE
                bt_sr_stop.visibility = View.INVISIBLE
                playImage.visibility = View.INVISIBLE
            }
            AudioType.DESTROY ->{
                chronometer.stopAR()
            }
            else->{
                chronometer.stopAR()
                playImage.stopAR()
                bt_sr_start.visibility = View.VISIBLE
                bt_sr_stop.visibility = View.INVISIBLE
                playImage.visibility = View.INVISIBLE
            }
        }
    }


}