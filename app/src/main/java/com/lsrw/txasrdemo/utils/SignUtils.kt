package com.lsrw.txasrdemo.utils

import android.util.Base64
import com.lsrw.txasrdemo.net.RetrofitCreator
import com.lsrw.txasrdemo.config.CommonConfig
import com.lsrw.txasrdemo.constant.NetParams
import com.lsrw.txasrdemo.enum.RequestType
import java.lang.StringBuilder
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object SignUtils {

    private const val MAC_NAME = "HmacSHA1"
    private const val ENCODING = "utf-8"

    /**
     * 构建签名
     */
    fun buildSign(rt: RequestType, tm: TreeMap<String, String>): String {
        //生成原生url
        val commonUrl = getUrl(rt, tm)
        LogUtils.d("MainActivity", "commonUrl:$commonUrl")
        val hse = hMacSHA1Encrypt(
            commonUrl as java.lang.String,
            CommonConfig.paramsMap.getValue(NetParams.SecretKey) as java.lang.String
        )
        return toBase64(hse)
    }

    private fun getUrl(rt: RequestType, tm: TreeMap<String, String>): String {
        val url = StringBuilder()
        url.append(rt.name)
        url.append("${RetrofitCreator.baseUrl}/?")
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

    /* fun signStr(requestType: RequestType, t: Any): String {
         val tm = TreeMap<String, String>()
         val keyNameList = mutableListOf<String>()
         val str = StringBuilder()
         //定义属性名列表
         str.append(requestType.name)
         str.append("${RetrofitCreator.baseUrl}/?")
         val instance = t::class.java
         val obj = instance.newInstance()
         //获取当前类里的所有属性
         val fields = instance.declaredFields
 //    if (fields.size != value.size) {
 //        throw ParamsCountDiffException("--- (value's count) != (fields' count) ---")
 //    }
         //遍历所有属性并把属性名称写入到List<String>中
         fields.forEach { field ->
             //判断是自定义的属性(而非jdk中)写入
             if (!field.isSynthetic) {
                 field.isAccessible = true
                 val key = field.name
                 val value = field.get(obj).toString()
                 keyNameList.add(key)
                 tm[key] = value
             }
         }
         keyNameList.forEachIndexed { index, key ->
             if (index != 0) str.append("&")
             val value = tm[key]
             str.append("${key}=${value}")
         }
         return str.toString()
     }*/

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
