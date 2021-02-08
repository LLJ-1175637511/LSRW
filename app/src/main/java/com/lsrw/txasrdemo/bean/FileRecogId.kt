package com.lsrw.txasrdemo.bean

class FileRecogIdSuc {
    data class SucBean(
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

class FileRecogErr {
    data class ErrBean(
        val Response: Response
    )

    data class Response(
        val Error: Error,
        val RequestId: String
    )

    data class Error(
        val Code: String,
        val Message: String
    )
}




