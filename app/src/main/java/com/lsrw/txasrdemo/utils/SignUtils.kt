package com.lsrw.txasrdemo.utils

import android.util.Base64
import com.lsrw.txasrdemo.net.RetrofitCreator
import com.lsrw.txasrdemo.enums.RequestType
import com.lsrw.txasrdemo.net.config.BaseConfig
import java.lang.StringBuilder
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object SignUtils {

    private const val MAC_NAME = "HmacSHA1"
    private const val ENCODING = "utf-8"
    private val TAG = this.javaClass.simpleName
    /**
     * 构建签名
     */
    fun buildSign(rt: RequestType, tm: TreeMap<String, String>): String {
        //生成原生url
        val commonUrl = getUrl(rt, tm)
        LogUtils.d(TAG, "commonUrl:$commonUrl")
        LogUtils.d(TAG, "SecretKey:${BaseConfig.secretKey}")
        val hse = hMacSHA1Encrypt(
            commonUrl as java.lang.String,
            BaseConfig.secretKey as java.lang.String
        )
        return toBase64(hse)
    }

    private fun getUrl(rt: RequestType, tm: TreeMap<String, String>): String {
        val url = StringBuilder()
        url.append(rt.name)
        url.append("${RetrofitCreator.tencentBaseUrl}/?")
        var index = 0
        tm.keys.forEach { key ->
            //除第一个参数外 均加 & 符号
            if (index != 0) url.append("&")
            index++
            val value = tm[key]
            url.append("${key}=${value}")

        }
        return url.toString()
    }

    private fun hMacSHA1Encrypt(
        encryptText: java.lang.String,
        encryptKey: java.lang.String
    ): ByteArray {
        val data: ByteArray = encryptKey.getBytes(ENCODING)
        val secretKey: SecretKey = SecretKeySpec(data, MAC_NAME)
        val mac: Mac = Mac.getInstance(MAC_NAME)
        mac.init(secretKey)
        val text: ByteArray = encryptText.getBytes(ENCODING)
        return mac.doFinal(text)
    }

    fun toBase64(byteArray: ByteArray?): String {
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
