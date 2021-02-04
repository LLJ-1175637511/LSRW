package com.lsrw.txasrdemo.bean

class FileRecognize {
    data class FileRecogBean(
        val Response: Response
    )

    data class Response(
        val Data: Data,
        val RequestId: String
    )

    data class Data(
        val TaskId: Int
    )

}




