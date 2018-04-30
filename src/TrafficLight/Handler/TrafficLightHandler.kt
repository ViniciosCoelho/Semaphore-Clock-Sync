package TrafficLight.Handler

import Utils.ClockHelper
import Utils.Constants
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class TrafficLightHandler {
    private var p = 0
    private val semP = Semaphore(1)
    private var q = 0
    private val semQ = Semaphore(1)
    private var clock: Long = 0
    private val helper = ClockHelper()
    private val syncMins: Int

    fun runTraffic(port: Int) {
        var syncCounter = 0
        var modeCounter = 0

        val socket = DatagramSocket(port)

        thread(true) { receiveParameters(socket) }

        while (true) {
            sleep(Constants.minute)

            clock += Constants.minute + 3 * Constants.second
            val time = helper.getRealTime(clock)
            println("Clock updated = $time")

            syncCounter++
            modeCounter++

            if (syncCounter == syncMins) {
                syncClock(socket)
                syncCounter = 0
            }

            if (modeCounter == 6) {
                changeMode()
                modeCounter = 0
            }
        }
    }

    private fun receiveParameters(socket: DatagramSocket) {
        while (true) {
            val buffer = ByteArray(5)
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)

            val data = String(packet.data)
            val endInd = data.indexOf('\n')

            if (endInd < 0) {
                continue
            }

            val parmVal = data.substring(1..endInd).toInt()

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

    private fun syncClock(socket: DatagramSocket) {
        var buffer = Constants.clockRequest.toByteArray()
        var packet = DatagramPacket(buffer, buffer.size)

        val requestTime = measureTimeMillis {
            socket.send(packet)

            buffer = ByteArray(100)
            packet = DatagramPacket(buffer, buffer.size)

            socket.receive(packet)
        }

        val data = String(packet.data)
        val endInd = data.indexOf('\n')

        if (endInd < 0) {
            return
        }

        val realTime = data.substring(0..endInd).toLong()

        // Something is wrong here...
        val d = clock + requestTime - clock - Constants.I
        val newClock = realTime + d
        clock = newClock
    }

    private fun changeMode() {
        semP.acquire()
        val auxP = p
        semP.release()

        semQ.acquire()
        val auxQ = q
        semQ.release()

        val x = if (auxP + auxQ > 0) {
            auxP / (auxP + auxQ)
        } else {
            2
        }

        val time = helper.getRealTime(clock)
        val parms = "Clock = $time P = $auxP Q = $auxQ"

        when {
            x <= 0.2 -> println(parms + " Mode 1")
            0.2 < x && x <= 0.4 -> println(parms + " Mode 2")
            0.4 < x && x <= 0.6 -> println(parms + " Mode 3")
            0.6 < x && x <= 0.8 -> println(parms + " Mode 4")
            0.8 < x && x <= 1 -> println(parms + " Mode 5")
            else -> println("There are no parameters!")
        }
    }
}