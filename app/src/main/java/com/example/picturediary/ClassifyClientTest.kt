package com.example.picturediary

import android.graphics.Bitmap
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress


class ClassifyClientTest {
//    private val hostname = "192.168.0.13"
    private val hostname = "192.168.100.131"     // TUK 와이파이

    private var last = false
    private var recvData: ByteArray = ByteArray(16384)
    private var sendData = arrayListOf<ByteArray>()
    private lateinit var dataSlice: ByteArray
    private lateinit var result: String

    // 비트맵 --> 바이트 --> 문자열
    fun setClassifyImage(bitmap: Bitmap) {
        // 바이트를 쪼개서 전송하기 위한 변수들
        var i = 0
        var start = 2 * i
        var end = start + 1023

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        var data = stream.toByteArray()
        data += "END".toByteArray()
        val dataSize = data.size - 1

        // 데이터를 쪼개어 배열에 저장
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
    suspend fun client(): ArrayList<String> {
        // 소켓 객체 생성
        val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
            .connect(InetSocketAddress(hostname, 9000))

        // 배열 안의 데이터를 차례대로 서버에 전송
        val output = socket.openWriteChannel(autoFlush = true)
        for (i in sendData) {
            output.writeAvailable(i)
        }

        // 이미지 링크 추가할 리스트
        val images = arrayListOf<String>()

        val input = socket.openReadChannel()
        while (true) {
            val down = "NO CONNECTION"
            input.readAvailable(recvData)

            if (recvData.isNotEmpty()) {
                val re = Regex("[^A-Za-z0-9-:.?~=/ ]")
                result = recvData.decodeToString()
                result = re.replace(result, "")

//                result = recvData.decodeToString()
                images.addAll(result.split("URL"))
                break
            }
        }

        socket.close()
        return images
    }
}