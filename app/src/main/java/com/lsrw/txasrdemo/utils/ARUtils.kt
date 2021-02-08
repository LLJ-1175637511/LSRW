package com.lsrw.txasrdemo.utils

import android.media.MediaRecorder
import com.lsrw.txasrdemo.ui.contem.BaseARInterface
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ARUtils :BaseARInterface{
    companion object {
        private const val TAG = "SRUtils"
        private const val MAX_RECORD_TIME = 1000*60 //设置最大录音时长60s
    }

    private var mediaRecorder: MediaRecorder? = null
    private var fileName = ""
    private var fileDir: File? = null

    private fun initConfig() {
        if (mediaRecorder != null) return
        mediaRecorder = MediaRecorder()
        // 设置声音来源
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        // 设置文件的输出格式
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        // 设置编码方式
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
    }

    fun initMR(filePath: String) {
        fileDir = File(filePath)
        fileDir?.let {
            if (!it.exists()) it.mkdir()
        }
    }

    fun getSavedPath() = fileName

    override fun startAR() {
        initConfig()
        fileDir?.let { fd ->
            if (fd.exists() && fd.canWrite()) {
                val curdate = SimpleDateFormat("yyyyMMddHHmmss").format(Date()) //获取当前时间
                LogUtils.d(TAG, "日期：$curdate")
                fileName = "${fileDir?.absolutePath}/$curdate.amr"
                LogUtils.d(TAG, fileName)
                val file = File(fileName)
                if (file.exists()) file.delete()
                try {
                    file.createNewFile()
                    if (file.exists() && file.canWrite()) {
                        mediaRecorder?.setOutputFile(fileName)
                        mediaRecorder?.setMaxDuration(MAX_RECORD_TIME)
                        mediaRecorder?.prepare()
                        mediaRecorder?.start()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                LogUtils.d(TAG, "文件夹不存在或不能读写")
            }
        }
    }

    override fun pauseAR() {
        mediaRecorder?.pause()
    }

    override fun resumeAR() {
        mediaRecorder?.resume()
    }

    override fun stopAR() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}