package TimeServer.Handler

import Utils.Constants
import java.lang.Thread.sleep
import java.net.DatagramSocket
import java.net.DatagramPacket
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

class ServerHandler() {
    private var clock: Long = 0
    private val sem = Semaphore(1)

    init { println("Server ready!") }

    fun runServer(port: Int) {
        val socket = DatagramSocket(port)

        thread (true) { updateClock() }

        while (true) {
            var buffer = ByteArray(128)
            var packet = DatagramPacket(buffer, buffer.size)

            socket.receive(packet)
            println("Request received from ${packet.address}:${packet.port}")

            thread (true) { sendActualClock(socket, packet) }
        }
    }

    fun sendActualClock(socket: DatagramSocket, packet: DatagramPacket) {
        val received = String(packet.data)

        if (received.contains("Send clock now!", true)) {
            sem.acquire()
            packet.data = clock.toString().toByteArray()
            sem.release()

            socket.send(packet)
        }
    }

    private fun updateClock() {
        while (true) {
            sleep(Constants.minute)

            sem.acquire()
            clock.plus(Constants.minute)
            println("Clock updated = $clock")
            sem.release()
        }
    }
}