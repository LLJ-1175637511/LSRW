package com.lsrw.txasrdemo.net.response

import com.lsrw.txasrdemo.config.CommonParams
import com.lsrw.txasrdemo.enums.RequestType
import com.lsrw.txasrdemo.net.RetrofitCreator
import com.lsrw.txasrdemo.net.config.FileConfig
import com.lsrw.txasrdemo.utils.LogUtils
import com.lsrw.txasrdemo.utils.SignUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*

object FileRecogResponse {

    private val TAG = this.javaClass.simpleName

    suspend fun sendRequest(base64: String, isId: Boolean) {
        if (isId) {
            sendFileRequestId(base64)
        }
    }


    private fun sendFileRequestId(base64: String){

    }

    private fun buildBodyForId(base64: String): FormBody {
        val timeTemp = System.currentTimeMillis() / 1000
        val random = (0..10000).random()
        val tm = FileConfig.buildTreeMapForId(timeTemp, random, base64)

        val fb = FormBody.Builder()
        LogUtils.d(TAG, "treeMap:\n")
        tm.keys.forEach { key ->
            fb.add(key, tm[key]!!)
            if (key != CommonParams.Data) LogUtils.d(TAG, "$key=${tm[key]}")
        }
        val sign = SignUtils.buildSign(RequestType.POST, tm)
        LogUtils.d(TAG, "sign:$sign")
        fb.add(CommonParams.Signature, sign)
        return fb.build()
    }
}