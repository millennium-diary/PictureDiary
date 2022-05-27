package com.example.picturediary

import android.graphics.Bitmap
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress


class ClassifyClient {
    private val hostname = "192.168.0.2"
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
            .connect(InetSocketAddress(hostname, 9000))

        val output = socket.openWriteChannel(autoFlush = true)
        output.writeAvailable(sendData)

        val input = socket.openReadChannel()
        while (true) {
            val down = "NO CONNECTION"
            input.readAvailable(recvData)

            if (recvData.isNotEmpty()) {
                val re = Regex("[^A-Za-z0-9 ]")
                result = recvData.decodeToString()
                result = re.replace(result, "")
                break
            }
        }

        socket.close()
        return result
    }
}