package com.lsrw.txasrdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.lsrw.txasrdemo.R

class MainActivity:AppCompatActivity() {

    private lateinit var btToFileRecognize:Button
    private lateinit var btToRealRecognize:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btToFileRecognize = findViewById(R.id.bt_file_recognize_main)
        btToRealRecognize = findViewById(R.id.bt_real_time_recognize_main)

        btToFileRecognize.setOnClickListener {
            val intent = Intent(this,FileRecogActivity::class.java)
            startActivity(intent)
        }

        btToRealRecognize.setOnClickListener {
            startActivity(Intent(this,RealTimeActivity::class.java))
            finish()
        }

    }
}