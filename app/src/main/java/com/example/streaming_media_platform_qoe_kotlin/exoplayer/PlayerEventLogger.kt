package com.example.streaming_media_platform_qoe_kotlin.exoplayer
//
//import android.content.Context
//import android.os.Environment
//import com.example.streaming_media_platform_qoe_kotlin.data_models.DecoderCountersData
//import com.example.streaming_media_platform_qoe_kotlin.data_models.Utils
//import com.google.android.exoplayer2.SimpleExoPlayer
//import java.io.File
//import java.io.PrintWriter
//import java.lang.Exception
//import android.util.Log
//import com.google.android.exoplayer2.Player
//import java.io.FileOutputStream
//import kotlin.io.path.Path
//import kotlin.io.path.absolutePathString
//
//
public class PlayerEventLogger {
//
//    val logFileName: String = "decoder_logs.csv"
//
//    public fun createNewLog(player: SimpleExoPlayer, context: Context) {
//        val decoderCountersStr: String = Utils.getGeneralDecoderCountersBufferCountData(player)!!.toString()
//        val playWhenReady: Boolean = player.playWhenReady
//        val playbackState: Int = player.playbackState
//
//        val currentTimeStamp: Long = java.util.Date().time
//        val logStr = "${currentTimeStamp},${playWhenReady},${playbackState},${decoderCountersStr}\n"
//
////        WriteToFile(logStr)
//        WriteToCSVFile(logStr, context)
//
//    }
//
//    private fun WriteToCSVFile(entry: String, context: Context): Boolean {
//        Log.d("EventLogger", "Attempting to write to file: ${Path(logFileName).absolutePathString()}")
//        var fileOutputStream: FileOutputStream
//        try {
//            fileOutputStream = context.openFileOutput(logFileName, Context.MODE_PRIVATE)
//            fileOutputStream.write(entry.toByteArray())
//            fileOutputStream.close()
//        } catch(e: Exception) {
//            e.printStackTrace()
//            Log.d("EventLogger", "Write file (${logFileName} failed.")
//            return false
//        }
//        Log.d("EventLogger", "Write to file (${logFileName}) successful.")
//        return true
//    }
//
//    private fun WriteToFile(entry: String) {
//        Log.d("EventLogger", "Attempting to write to file: ${Environment.getDataDirectory()}")
//        val sd_main = File(Environment.getExternalStorageDirectory().toString(), "/kotlin_project")
//        var success = true
//        if (!sd_main.exists())
//            success = sd_main.mkdir()
//
//        if (success) {
//            val sd = File(Environment.getExternalStorageDirectory().toString() + "/kotlin_project")
//
//            if (!sd.exists())
//                success = sd.mkdir()
//
//            if (success) {
//                // directory exists or already created
//                val dest = File(sd, logFileName)
//                try {
////                    PrintWriter(dest).use {out -> out.println(entry)}
//                    dest.appendText(entry)
//                } catch (e: Exception) {
//                    Log.d("EventLogger", "Write to file failed.")
//                }
//            }
//        }
//        else {
//            Log.d("EventLogger", "Directory creation unsuccessful")
//        }
//    }
}