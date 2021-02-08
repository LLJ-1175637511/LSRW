package com.lsrw.txasrdemo.ui.activity

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.lsrw.txasrdemo.R
import com.lsrw.txasrdemo.bean.FileRecogResult
import com.lsrw.txasrdemo.bean.FileRecogErr
import com.lsrw.txasrdemo.bean.FileRecogIdSuc
import com.lsrw.txasrdemo.config.CommonParams
import com.lsrw.txasrdemo.enums.RequestType
import com.lsrw.txasrdemo.net.RetrofitCreator
import com.lsrw.txasrdemo.net.config.FileConfig
import com.lsrw.txasrdemo.ui.contem.SRChronometer
import com.lsrw.txasrdemo.ui.contem.ARPlayImage
import com.lsrw.txasrdemo.utils.LogUtils
import com.lsrw.txasrdemo.utils.SignUtils
import okhttp3.*
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.lang.StringBuilder
import kotlin.concurrent.thread

class SrActivity : AppCompatActivity() {

    private val TAG = this.javaClass.simpleName

    companion object {
        private const val SAVED_PATH = "speak"
    }

    private lateinit var srPlayImg: ARPlayImage
    private lateinit var srChronometer: SRChronometer
    private lateinit var btStartSR: Button
    private lateinit var btStopSR: Button
    private lateinit var btRequestId: Button
    private lateinit var btRecognizeContent: Button
    private lateinit var tvRecogContent: TextView

    private var base64 = ""
    private val requestBuilder = Request.Builder()
    private var taskId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_recong)

        initView()
        initOther()
        initListener()
        myRequestPermission()
    }

    private fun initListener() {
        btStartSR.setOnClickListener {
            srPlayImg.startAR()
            srChronometer.startAR()
            it.visibility = View.INVISIBLE
            srPlayImg.visibility = View.VISIBLE
            btStopSR.visibility = View.VISIBLE
        }

        srPlayImg.setOnClickListener {
            srPlayImg.apply {
                val isPlaying = getPlayState()
                if (isPlaying) {
                    pauseAR()
                    srChronometer.pauseAR()
                } else {
                    resumeAR()
                    srChronometer.resumeAR()
                }
            }
        }

        btStopSR.setOnClickListener {
            srPlayImg.stopAR()
            srChronometer.stopAR()
            it.visibility = View.INVISIBLE
            srPlayImg.visibility = View.INVISIBLE
            btStartSR.visibility = View.VISIBLE
            Toast.makeText(this, "文件保存至：${srChronometer.getSavedPath()}", Toast.LENGTH_SHORT).show()
            getVoiceBase64()
        }

        btRequestId.setOnClickListener {
            if (srPlayImg.getPlayState()) {
                Toast.makeText(this, "请先保存录音", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendRequest(true)
        }

        btRecognizeContent.setOnClickListener {
            if (taskId.isEmpty()){
                Toast.makeText(this, "请先获取taskId", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendRequest(false)
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
        btStartSR = findViewById(R.id.bt_sr_start)
        btStopSR = findViewById(R.id.bt_sr_stop)
        btRequestId = findViewById(R.id.bt_request_id_sr)
        tvRecogContent = findViewById(R.id.tv_recong_content_sr)
        btRecognizeContent = findViewById(R.id.bt_recognize_content_sr)
    }

    private fun getVoiceBase64() {
        thread {
            try {
                val fd = FileInputStream(srChronometer.getSavedPath())
                val buffInputStream = BufferedInputStream(fd)
                val byteArray = buffInputStream.readBytes()
                buffInputStream.close()
                base64 = SignUtils.toBase64(byteArray)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                runOnUiThread {
                    Toast.makeText(this, "load suc", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendRequest(isId: Boolean) {
        try {
            val client = OkHttpClient()
            val call = if (isId) {
                requestBuilder.url("https://${RetrofitCreator.tencentBaseUrl}")
                    .post(buildBodyForId()).build()
            } else {
                requestBuilder.url("https://${RetrofitCreator.tencentBaseUrl}")
                    .post(buildBodyForResult()).build()
            }
            client.newCall(call).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LogUtils.d(TAG, "err:${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
//                    Log.d(TAG,responseData.toString())
                    if (responseData != null) {
                        val gson = Gson()
                        if (!responseData.contains("TaskId")){ //错误的请求
                            val result = gson.fromJson(responseData,FileRecogErr.ErrBean::class.java)
                            val errInfo = result.Response.Error
                            displayResult(errInfo.toString())
                            LogUtils.d(TAG, "err:${errInfo}")
                        }else{
                            val jsonResponse =
                                gson.fromJson(responseData, FileRecogIdSuc.SucBean::class.java)
                            taskId = jsonResponse.Response.Data.TaskId.toString()
                            LogUtils.d(TAG, "requestId:$taskId")
                            if (isId) {
                                displayResult("请求成功\ntaskId:$taskId")
                            } else {
                                LogUtils.d(TAG, "all result:${responseData}")
                                val result =
                                    gson.fromJson(responseData, FileRecogResult.RecogResult::class.java)
                                val tempResult = result.Response.Data.Result
                                val final = buildResultString(tempResult)
                                displayResult(final)
                            }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayResult(requestInfo: String) {
        runOnUiThread {
            if (requestInfo.isNotEmpty()) {
                tvRecogContent.text = requestInfo
            } else {
                tvRecogContent.text = "识别信息有误"
                Toast.makeText(this@SrActivity, "请求失败", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun buildResultString(strResult: String): String {
        val builder = StringBuilder()
        builder.append("识别结果：")
        if (strResult.contains('\n')) {
            val allWord = strResult.split('\n')
            allWord.forEach {
                if (it.contains(']')) {
                    val content = it.split(']')[1].trimStart()
                    builder.append('\n')
                    builder.append(content)
                }
            }
        }
        return builder.toString()
    }

    private fun buildBodyForResult(): FormBody {
        val timeTemp = System.currentTimeMillis() / 1000
        val random = (0..10000).random()

        val tm = FileConfig.buildTreeMapForResult(timeTemp,random,taskId)
        val fb = FormBody.Builder()
        LogUtils.d(TAG, "treeMap:\n")
        tm.keys.forEach { key ->
            fb.add(key, tm[key]!!)
            LogUtils.d(TAG, "$key=${tm[key]}")
        }
        val sign = SignUtils.buildSign(RequestType.POST, tm)
        LogUtils.d(TAG, "sign:$sign")
        fb.add(CommonParams.Signature, sign)
        return fb.build()
    }

    private fun buildBodyForId(): FormBody {
        val timeTemp = System.currentTimeMillis() / 1000
        val random = (0..10000).random()
        val tm = FileConfig.buildTreeMapForId(timeTemp,random,base64)

        val fb = FormBody.Builder()
        LogUtils.d(TAG, "treeMap:\n")
        tm.keys.forEach { key ->
                fb.add(key, tm[key]!!)
            if (key!=CommonParams.Data) LogUtils.d(TAG, "$key=${tm[key]}")
        }
        val sign = SignUtils.buildSign(RequestType.POST, tm)
        LogUtils.d(TAG, "sign:$sign")
        fb.add(CommonParams.Signature, sign)
        return fb.build()
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