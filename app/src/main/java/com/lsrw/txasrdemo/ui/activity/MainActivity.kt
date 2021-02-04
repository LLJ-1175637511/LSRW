package com.lsrw.txasrdemo.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.lsrw.txasrdemo.R
import com.lsrw.txasrdemo.config.CommonConfig
import com.lsrw.txasrdemo.constant.NetParams
import com.lsrw.txasrdemo.enum.RequestType
import com.lsrw.txasrdemo.net.RetrofitCreator
import com.lsrw.txasrdemo.utils.LogUtils
import com.lsrw.txasrdemo.utils.SignUtils
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedInputStream
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val requestBuilder = Request.Builder()
    private var base64 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var byteArray: ByteArray? = null

        thread {
            try {
                val fd = assets.open("test1.m4a")
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

        tv.setOnClickListener {
            base64 = SignUtils.toBase64(byteArray)
            requestHttp()
        }
    }

    private fun requestHttp() {
        try {
            val client = OkHttpClient()
            val call = requestBuilder.url("https://${RetrofitCreator.baseUrl}")
                .post(buildBody()).build()
            client.newCall(call).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LogUtils.d("MainActivity", "err:${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        LogUtils.d("MainActivity", responseData)
                    }
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildBody(): FormBody {
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
        LogUtils.d("MainActivity", "treeMap:\n")
        tm.keys.forEach { key ->
            fb.add(key, tm[key]!!)
            LogUtils.d("MainActivity", "$key=${tm[key]}")
        }
        val sign = SignUtils.buildSign(RequestType.POST, tm)
        LogUtils.d("MainActivity", "sign:$sign")
        fb.add(NetParams.Signature, sign)
        return fb.build()
    }
}