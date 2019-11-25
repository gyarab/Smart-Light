package com.cubanstudio.smartlight

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.StringBuilder

class CommunicationThread(
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
    private val context: Context
) : Thread() {
    var reader: BufferedReader = BufferedReader(InputStreamReader(inputStream))
    override fun run() {
        var start = false;
        val builder = StringBuilder()

        while (true) {
            // for ()


            val dat = reader.readLine()
            Log.e("DATA", dat.toString())

            if (dat.contains("\u0001") && dat.contains("\u0004")) {
                val head =
                    dat.substring(dat.indexOf("\u0001") + 1, dat.indexOf("\u0002"))
                val body =
                    dat.substring(dat.indexOf("\u0002") + 1, dat.indexOf("\u0003"))
                val chSum =
                    dat.substring(dat.indexOf("\u0003") + 1, dat.indexOf("\u0004"))
                Log.e("DATA PARSING", head)
                Log.e("DATA PARSING", body)
                Log.e("DATA PARSING", chSum)
                if (fletcherCheckSum(dat.substring(0, dat.indexOf("\u0003") + 1)) == chSum) {

                    val intent = Intent(head)
                    intent.putExtra("DATA", body)
                    // LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    context.sendBroadcast(intent)
                }
            }




        }
    }

    fun listenForData() {
        this.start()
    }

    fun sendData(head: String, body: String) {
        var data = "\u0001${head}\u0002${body}\u0003"
        data += fletcherCheckSum(data) + "\u0004\r"
        outputStream.write(data.toByteArray())
    }

    private fun fletcherCheckSum(data: String): String {
        var c1: Long = 0
        var c2: Long = 0
        for (char: Char in data.toCharArray()) {
            c1+= char.toByte()
            c2+=c1
        }
        val checkSum = intToHexString(c1.rem(255).toInt()) + intToHexString(c2.rem(255).toInt())
        return checkSum
    }


    private fun intToHexString(num: Int): String {
        return Integer.toHexString(num)
    }


}