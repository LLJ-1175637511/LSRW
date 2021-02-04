package com.lsrw.txasrdemo.ui.activity

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lsrw.txasrdemo.R
import com.lsrw.txasrdemo.ui.contem.SRChronometer
import com.lsrw.txasrdemo.ui.contem.SRPlayImage
import com.lsrw.txasrdemo.utils.SignUtils
import java.io.BufferedInputStream
import kotlin.concurrent.thread

class SrActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SrActivity"
        private const val SAVED_PATH = "speak"
    }

    private lateinit var srPlayImg: SRPlayImage
    private lateinit var srChronometer: SRChronometer
    private lateinit var startSR: Button
    private lateinit var stopSR: Button
    private lateinit var requestId:Button
    private lateinit var recognizeContent:Button

    private var base64 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        initView()
        initOther()
        initListener()
        myRequestPermission()
    }

    private fun initListener() {
        startSR.setOnClickListener {
            srPlayImg.startSR()
            srChronometer.startSR()
            it.visibility = View.INVISIBLE
            srPlayImg.visibility = View.VISIBLE
            stopSR.visibility = View.VISIBLE
        }

        srPlayImg.setOnClickListener {
            srPlayImg.apply {
                val isPlaying = getPlayState()
                if (isPlaying) {
                    pauseSR()
                    srChronometer.pauseSR()
                } else {
                    resumeSR()
                    srChronometer.resumeSR()
                }
            }
        }

        stopSR.setOnClickListener {
            srPlayImg.stopSR()
            srChronometer.stopSR()
            it.visibility = View.INVISIBLE
            srPlayImg.visibility = View.INVISIBLE
            startSR.visibility = View.VISIBLE
            Toast.makeText(this, "文件保存至：${srChronometer.getSavedPath()}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initOther() {
        //必须先初始化录音保存路径
        srChronometer.initSavePath("${externalCacheDir?.path}/$SAVED_PATH")
        lifecycle.apply {
            addObserver(srChronometer)
            addObserver(srPlayImg)
        }
    }

    private fun initView() {
        srPlayImg = findViewById(R.id.iv_opera_sr)
        srChronometer = findViewById(R.id.sr_chronometer)
        startSR = findViewById(R.id.bt_sr_start)
        stopSR = findViewById(R.id.bt_sr_stop)
        requestId = findViewById(R.id.bt_request_id_sr)
        recognizeContent = findViewById(R.id.bt_recognize_content_sr)
    }

    fun getVoiceBase64(){
        var byteArray: ByteArray? = null
        thread {
            try {
                val fd = assets.open(srChronometer.getSavedPath())
                val buffInputStream = BufferedInputStream(fd)
                byteArray = buffInputStream.readBytes()
                buffInputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                runOnUiThread {
                    Toast.makeText(this, "load suc", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun sendRequestForId(){

    }
    private fun myRequestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var isSuc = true
        for (i in grantResults) {
            if (i != PERMISSION_GRANTED) {
                isSuc = false
            }
        }
        if (!isSuc) Toast.makeText(this, "suc", Toast.LENGTH_SHORT).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}