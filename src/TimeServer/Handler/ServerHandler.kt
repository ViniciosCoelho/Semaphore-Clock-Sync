package TimeServer.Handler

import Utils.ClockHelper
import Utils.Constants
import java.lang.Thread.sleep
import java.net.DatagramSocket
import java.net.DatagramPacket
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

class ServerHandler {
    private var clock: Long = 0
    private val sem = Semaphore(1)
    private val helper = ClockHelper()

    init { println("Server ready!") }

    fun runServer(port: Int) {
        val socket = DatagramSocket(port)

        thread (true) { updateClock() }

        while (true) {
            val buffer = ByteArray(128)
            val packet = DatagramPacket(buffer, buffer.size)

            socket.receive(packet)
            println("Request received from ${packet.address}:${packet.port}")

            thread(true) { sendActualClock(socket, packet) }
        }
    }

    fun sendActualClock(socket: DatagramSocket, packet: DatagramPacket) {
        val received = String(packet.data)

        if (received.contains(Constants.clockRequest, true)) {
            sem.acquire()
            val clockAux = clock
            packet.data = (clock.toString() + '\n').toByteArray()
            sem.release()

            socket.send(packet)
            println("Clock sent = ${helper.getRealTime(clockAux)}")
        }
    }

    private fun updateClock() {
        val timeLapse = Constants.second

        while (true) {
            sleep(timeLapse)

            sem.acquire()
            clock += timeLapse
            val time = ClockHelper().getRealTime(clock)
            if (clock % (20 * Constants.second) == 0.toLong()) {
                println("Clock updated = $time")
            }
            sem.release()
        }
    }
}