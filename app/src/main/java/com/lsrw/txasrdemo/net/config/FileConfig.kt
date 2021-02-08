package com.lsrw.txasrdemo.net.config

import com.lsrw.txasrdemo.config.CommonParams
import com.lsrw.txasrdemo.enums.ActionType
import java.util.*

object FileConfig {

    private val forIdList = mutableListOf<ConfigMap>()
    private val forResultList = mutableListOf<ConfigMap>()

    init {
        BaseConfig.commonConfig.forEach {
            forIdList.add(ConfigMap(it.key,it.value))
            forResultList.add(ConfigMap(it.key,it.value))
        }
        forIdList.apply {
            add(ConfigMap(CommonParams.EngineModelType, "16k_en"))
            add(ConfigMap(CommonParams.ChannelNum, "1"))
            add(ConfigMap(CommonParams.ResTextFormat, "0"))
            add(ConfigMap(CommonParams.SourceType, "1"))
            add(ConfigMap(CommonParams.Action, ActionType.CreateRecTask.name))
            add(ConfigMap(CommonParams.Language, "zh-CN"))
            add(ConfigMap(CommonParams.Region, ""))
        }
        forResultList.apply {
            add(ConfigMap(CommonParams.Action, ActionType.DescribeTaskStatus.name))
        }
    }

    fun buildTreeMapForId(
        timeStamp: Long,
        nonce: Int,
        data: String
    ): TreeMap<String, String> {
        val tm = TreeMap<String, String>()
        forIdList.forEach {
            tm[it.key] = it.value
        }
        tm[CommonParams.Timestamp] = timeStamp.toString()
        tm[CommonParams.Nonce] = nonce.toString()
        tm[CommonParams.Data] = data
        return tm
    }

    fun buildTreeMapForResult(
        timeStamp: Long,
        nonce: Int,
        taskId: String
    ): TreeMap<String, String> {
        val tm = TreeMap<String, String>()
        forResultList.forEach {
            tm[it.key] = it.value
        }
        tm[CommonParams.Timestamp] = timeStamp.toString()
        tm[CommonParams.Nonce] = nonce.toString()
        tm[CommonParams.TaskId] = taskId
        return tm
    }

    data class ConfigMap(val key:String,val value:String)
}