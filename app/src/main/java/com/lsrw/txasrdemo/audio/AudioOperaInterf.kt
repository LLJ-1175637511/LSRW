package com.lsrw.txasrdemo.audio

import com.lsrw.txasrdemo.enums.FileFormatType

interface AudioOperaInterf {

//    fun startRecord(fileName:String)
    fun startRecord(filePath:String,fileName:String)
//    fun startRecordWav()

    fun stopRecord()

    fun releaseRecord()

    fun setOutPutFileFormat(fft:FileFormatType)

}