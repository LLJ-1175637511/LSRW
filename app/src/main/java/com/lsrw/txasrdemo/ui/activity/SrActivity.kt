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
import com.lsrw.txasrdemo.bean.FileRecognize
import com.lsrw.txasrdemo.config.CommonConfig
import com.lsrw.txasrdemo.constant.NetParams
import com.lsrw.txasrdemo.enum.RequestType
import com.lsrw.txasrdemo.net.RetrofitCreator
import com.lsrw.txasrdemo.ui.contem.SRChronometer
import com.lsrw.txasrdemo.ui.contem.SRPlayImage
import com.lsrw.txasrdemo.utils.LogUtils
import com.lsrw.txasrdemo.utils.SignUtils
import okhttp3.*
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.lang.StringBuilder
import java.util.*
import kotlin.concurrent.thread

class SrActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SrActivity"
        private const val SAVED_PATH = "speak"
    }

    private lateinit var srPlayImg: SRPlayImage
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
        setContentView(R.layout.activity_speak)

        initView()
        initOther()
        initListener()
        myRequestPermission()
    }

    private fun initListener() {
        btStartSR.setOnClickListener {
            srPlayImg.startSR()
            srChronometer.startSR()
            it.visibility = View.INVISIBLE
            srPlayImg.visibility = View.VISIBLE
            btStopSR.visibility = View.VISIBLE
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

        btStopSR.setOnClickListener {
            srPlayImg.stopSR()
            srChronometer.stopSR()
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
//                runOnUiThread {
//                    Toast.makeText(this, "load suc", Toast.LENGTH_SHORT).show()
//                }
            }
        }
    }

    private fun sendRequest(isId: Boolean) {
        try {
            val client = OkHttpClient()
            val call = if (isId) {
                requestBuilder.url("https://${RetrofitCreator.baseUrl}")
                    .post(buildBodyForId()).build()
            } else {
                requestBuilder.url("https://${RetrofitCreator.baseUrl}")
                    .post(buildBodyForResult()).build()
            }
            client.newCall(call).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LogUtils.d(TAG, "err:${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val gson = Gson()
                        if (isId) {
                            val result =
                                gson.fromJson(responseData, FileRecognize.FileRecogBean::class.java)
                            taskId = result.Response.Data.TaskId.toString()
                            LogUtils.d(TAG, "requestId:$taskId")
                            runOnUiThread {
                                tvRecogContent.text = "请求成功\ntaskId:$taskId"
                            }
                        } else {
                            LogUtils.d(TAG, "all result:${responseData}")
                            val result =
                                gson.fromJson(responseData, FileRecogResult.RecogResult::class.java)
                            val tempTest = result.Response.Data.Result
                            val final = buildResultString(tempTest)
                            runOnUiThread {
                                if (final.isNotEmpty()) {
                                    tvRecogContent.text = final
                                } else {
                                    tvRecogContent.text = "识别信息有误\n$tempTest"
                                    Toast.makeText(this@SrActivity, "请求失败", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
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
        val tm = TreeMap<String, String>()
        val timeTemp = System.currentTimeMillis() / 1000
        val random = (0..10000).random()
//        tm[NetParams.EngineModelType] = CommonConfig.paramsMap.getValue(NetParams.EngineModelType)
//        tm[NetParams.ChannelNum] = CommonConfig.paramsMap.getValue(NetParams.ChannelNum)
//        tm[NetParams.ResTextFormat] = CommonConfig.paramsMap.getValue(NetParams.ResTextFormat)
//        tm[NetParams.SourceType] = CommonConfig.paramsMap.getValue(NetParams.SourceType)
//        tm[NetParams.Data] = base64
        tm[NetParams.Action] = "DescribeTaskStatus"
        tm[NetParams.Version] = CommonConfig.paramsMap.getValue(NetParams.Version)
        tm[NetParams.Timestamp] = timeTemp.toString()
        tm[NetParams.Nonce] = random.toString()
        tm[NetParams.SecretId] = CommonConfig.paramsMap.getValue(NetParams.SecretId)
        tm[NetParams.Language] = CommonConfig.paramsMap.getValue(NetParams.Language)
        tm[NetParams.TaskId] = taskId
        tm[NetParams.Region] = ""

        val fb = FormBody.Builder()
        LogUtils.d(TAG, "treeMap:\n")
        tm.keys.forEach { key ->
            fb.add(key, tm[key]!!)
//            LogUtils.d(TAG, "$key=${tm[key]}")
        }
        val sign = SignUtils.buildSign(RequestType.POST, tm)
        LogUtils.d(TAG, "sign:$sign")
        fb.add(NetParams.Signature, sign)
        return fb.build()
    }

    private fun buildBodyForId(): FormBody {
        val tm = TreeMap<String, String>()
        val timeTemp = System.currentTimeMillis() / 1000
        val random = (0..10000).random()
        tm[NetParams.EngineModelType] = CommonConfig.paramsMap.getValue(NetParams.EngineModelType)
        tm[NetParams.ChannelNum] = CommonConfig.paramsMap.getValue(NetParams.ChannelNum)
        tm[NetParams.ResTextFormat] = CommonConfig.paramsMap.getValue(NetParams.ResTextFormat)
        tm[NetParams.SourceType] = CommonConfig.paramsMap.getValue(NetParams.SourceType)
        tm[NetParams.Action] = CommonConfig.paramsMap.getValue(NetParams.Action)
        tm[NetParams.Version] = CommonConfig.paramsMap.getValue(NetParams.Version)
        tm[NetParams.Timestamp] = timeTemp.toString()
        tm[NetParams.Nonce] = random.toString()
        tm[NetParams.SecretId] = CommonConfig.paramsMap.getValue(NetParams.SecretId)
        tm[NetParams.Data] = base64
        tm[NetParams.Language] = CommonConfig.paramsMap.getValue(NetParams.Language)
        tm[NetParams.Region] = ""

        val fb = FormBody.Builder()
        LogUtils.d(TAG, "treeMap:\n")
        tm.keys.forEach { key ->
            fb.add(key, tm[key]!!)
//            LogUtils.d(TAG, "$key=${tm[key]}")
        }
        val sign = SignUtils.buildSign(RequestType.POST, tm)
        LogUtils.d(TAG, "sign:$sign")
        fb.add(NetParams.Signature, sign)
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