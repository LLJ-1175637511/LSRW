package com.lsrw.txasrdemo.config

import com.lsrw.txasrdemo.constant.NetParams

object CommonConfig {
    val paramsMap = mutableMapOf<String,String>()
    init {
        paramsMap[NetParams.EngineModelType]="16k_zh"
        paramsMap[NetParams.ChannelNum] = "1"
        paramsMap[NetParams.ResTextFormat] = "0"
        paramsMap[NetParams.SourceType] = "1"
        paramsMap[NetParams.Action] = "CreateRecTask"
        paramsMap[NetParams.Version] = "2019-06-14"
        paramsMap[NetParams.SecretId] = "AKIDb7PffOS96lBJBothnFQ5WifgT0O35282"
        paramsMap[NetParams.SecretKey] = "oSAeqndCDzUTCzCPLEr76rLYdU5VdZGg"
        paramsMap[NetParams.ChannelNum] = "1"
        paramsMap[NetParams.Language] = "zh-CN"
        paramsMap[NetParams.Region] = "" //空参数 部分接口不需要使用
    }
}