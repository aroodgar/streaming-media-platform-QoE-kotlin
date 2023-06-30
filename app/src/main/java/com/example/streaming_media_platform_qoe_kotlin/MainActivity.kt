package com.example.streaming_media_platform_qoe_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.streaming_media_platform_qoe_kotlin.Constants.BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_KEY
import com.example.streaming_media_platform_qoe_kotlin.Constants.BUFFER_FOR_PLAYBACK_MS
import com.example.streaming_media_platform_qoe_kotlin.Constants.CONNECT_TIMEOUT_KEY
import com.example.streaming_media_platform_qoe_kotlin.Constants.DEFAULT_BUFFER_SEGMENT_SIZE_KEY
import com.example.streaming_media_platform_qoe_kotlin.Constants.MAX_BUFFER_MS_KEY
import com.example.streaming_media_platform_qoe_kotlin.Constants.MIN_BUFFER_MS_KEY
import com.example.streaming_media_platform_qoe_kotlin.Constants.READ_TIMEOUT_KEY
import com.example.streaming_media_platform_qoe_kotlin.Constants.RTL_STREAM_URL
import com.example.streaming_media_platform_qoe_kotlin.Constants.STREAM_URL_KEY
import com.example.streaming_media_platform_qoe_kotlin.databinding.ActivityMainBinding

//import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
//        setContentView(R.layout.activity_main)
        setContentView(view)

        setInitialValues()

        binding.startStreamingButton.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra(STREAM_URL_KEY, binding.streamUrlEdittext.text.toString())
            intent.putExtra(CONNECT_TIMEOUT_KEY, binding.connectTimeoutMillisEditTextNumber.text.toString().toInt())
            intent.putExtra(READ_TIMEOUT_KEY, binding.readTimeoutMillisEditTextNumber.text.toString().toInt())

            intent.putExtra(DEFAULT_BUFFER_SEGMENT_SIZE_KEY, binding.defaultBufferSegmentSizeEditTextNumber.text.toString().toInt())
            intent.putExtra(MIN_BUFFER_MS_KEY, binding.minBufferMsEditTextNumber.text.toString().toInt())
            intent.putExtra(MAX_BUFFER_MS_KEY, binding.maxBufferMsEditTextNumber.text.toString().toInt())
            intent.putExtra(BUFFER_FOR_PLAYBACK_MS, binding.bufferForPlaybackMsEditTextNumber.text.toString().toInt())
            intent.putExtra(BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_KEY, binding.bufferForPlaybackAfterRebufferMsEditTextNumber.text.toString().toInt())

            startActivity(intent)
        }
    }

    fun setInitialValues(){
        binding.streamUrlEdittext.setText(RTL_STREAM_URL)
        binding.streamUrlEdittext.setSelection(0)


    }
}