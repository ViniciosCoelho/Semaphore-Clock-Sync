package TrafficLight.Handler

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.Semaphore

class TrafficLightHandler {
    private var p = 0
    private val semP = Semaphore(1)
    private var q = 0
    private val semQ = Semaphore(1)

    fun runTraffic(port: Int) {
        val socket = DatagramSocket(port)

        updateParameters(socket)

        Thread.sleep()
    }

    private fun updateParameters(socket: DatagramSocket) {
        while (true) {
            val buffer = ByteArray(5)
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)

            val data = String(packet.data)
            val endInd = data.indexOf('\n')

            if (endInd < 0) {
                continue
            }

            val parmVal = data.substring(1, endInd).toInt()

            if (data.contains("p", true)) {
                semP.acquire()
                p = parmVal
                semP.release()
            } else {
                semQ.acquire()
                q = parmVal
                semQ.release()
            }
        }
    }
}