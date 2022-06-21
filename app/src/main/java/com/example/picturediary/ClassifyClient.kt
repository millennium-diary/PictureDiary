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
    private val hostname = "172.30.1.52"
//    private val hostname = "192.168.0.44"     // TUK 와이파이

    private var last = false
    private var recvData: ByteArray = ByteArray(20)
    private var sendData = arrayListOf<ByteArray>()
    private lateinit var dataSlice: ByteArray
    private lateinit var result: String

    // 비트맵 --> 바이트 --> 문자열
    fun setClassifyImage(bitmap: Bitmap) {
        var i = 0
        var start = 2 * i
        var end = start + 1023

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        var data = stream.toByteArray()
        data += "END".toByteArray()
        val dataSize = data.size - 1

        while (true) {
            dataSlice = data.slice(start..end).toByteArray()
            sendData.add(dataSlice)

            if (last) break

            i += 1
            start = end + 1
            end = start + 1023

            if (end > dataSize) {
                end = dataSize
                last = true
            }
        }
    }

    @DelicateCoroutinesApi
    suspend fun client(): String {
        val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
            .connect(InetSocketAddress(hostname, 9000))

        val output = socket.openWriteChannel(autoFlush = true)
        for (i in sendData) {
            output.writeAvailable(i)
        }

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
        println("결과 $result")
        return result
    }
}