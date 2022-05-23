package com.example.picturediary

import android.graphics.Bitmap
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.util.*


class ClassifyClient {
    private var recvData: ByteArray = ByteArray(20)
    private lateinit var sendData: ByteArray
    private lateinit var result: String

    // 비트맵 --> 바이트 --> 문자열
    fun setClassifyImage(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
        val data = stream.toByteArray()
        val end = "END"
        sendData = data + end.toByteArray()
    }

    @DelicateCoroutinesApi
    suspend fun client(): String {
        val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
            .connect(InetSocketAddress("192.168.0.2", 9000))

        val output = socket.openWriteChannel(autoFlush = true)
        output.writeAvailable(sendData)

        var i = 0
        val input = socket.openReadChannel()
        while (true) {
            i += 1
            val down = "SERVER DOWN"
            input.readAvailable(recvData)

            if (recvData.isNotEmpty()) {
                result = String(recvData)
                break
            }
            else if (i == 5 && recvData.isEmpty()) {
                result = down
                break
            }
        }

        socket.close()
        return result
    }
}