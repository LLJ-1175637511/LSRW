package com.lsrw.txasrdemo.net.service

import retrofit2.http.GET
import java.util.*

interface FileRecogService {



    @GET()
    fun getFileResult(){

    }

    companion object{
        val tms = TreeMap<String,String>()
        fun initParams(tm:TreeMap<String,String>){
            tm.forEach{
                it.key
            }
        }
    }
}