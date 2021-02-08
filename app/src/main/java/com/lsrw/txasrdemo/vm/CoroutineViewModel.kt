package com.lsrw.txasrdemo.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

class CoroutineViewModel(application: Application) : AndroidViewModel(application) {
    fun test() {
        viewModelScope.launch() {
            writeFile()
        }
    }

    private suspend fun writeFile(){

        withContext(Dispatchers.IO) {

        }
    }

}