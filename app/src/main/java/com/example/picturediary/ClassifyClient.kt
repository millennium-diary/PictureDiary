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
//    private val hostname = "192.168.100.196"     // TUK 와이파이

    private var recvData: ByteArray = ByteArray(20)
    private lateinit var sendData1: ByteArray
    private lateinit var sendData2: ByteArray
    private lateinit var result: String

    // 비트맵 --> 바이트 --> 문자열
    fun setClassifyImage(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val data = stream.toByteArray()
        val dataSize = data.size - 1

        sendData1 = data.slice(0..dataSize/2).toByteArray()
        sendData2 = data.slice((dataSize/2) + 1..dataSize).toByteArray()

        sendData2 += "END".toByteArray()
    }

    @DelicateCoroutinesApi
    suspend fun client(): String {
        val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
            .connect(InetSocketAddress(hostname, 9000))

        val output = socket.openWriteChannel(autoFlush = true)
        output.writeAvailable(sendData1)
        output.writeAvailable(sendData2)

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