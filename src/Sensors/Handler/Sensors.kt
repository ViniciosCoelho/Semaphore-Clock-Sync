package Sensors.Handler

import Utils.ClockHelper
import Utils.Constants
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Random
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

abstract class Sensors(serverIP: String, serverPort: Int, k: Long) {
    protected var name = ""

    private val serverIP = serverIP
    private val serverPort = serverPort
    private val syncMins = k

    protected var clock: Long = 0
    protected val semClock = Semaphore(1)
    protected val helper = ClockHelper()

    fun monitorTraffic(port: Int, trafficServerIP: String, trafficServerPort: Int) {
        val socket = DatagramSocket(port)

        var syncCounter = syncMins
        var sendCounter: Long = 0

        while (true) {
            if (syncCounter == syncMins) {
                syncClock(socket)

                semClock.acquire()
                val isExact = clock % Constants.minute
                semClock.release()

                if (isExact != 0.toLong()) {
                    val delayToInit =  Constants.minute - isExact
                    sleep(delayToInit)
                    semClock.acquire()
                    clock += delayToInit
                    semClock.release()
                }

                semClock.acquire()
                syncCounter = (clock / Constants.minute) % syncMins
                sendCounter = (clock / Constants.minute) % 6
                semClock.release()
            } else {
                val timeLapse = updateClock()
                sleep(timeLapse)

                semClock.acquire()
                clock += Constants.minute
                semClock.release()

                syncCounter++
                sendCounter++
            }

            semClock.acquire()
            val time = helper.getRealTime(clock)
            semClock.release()
            println("$name - Clock = $time")

            if (sendCounter == 5.toLong()) {
                thread(true) { sendParm(socket, trafficServerIP, trafficServerPort) }
            }

            if (sendCounter == 6.toLong()) {
                sendCounter = 0
            }
        }
    }

    private fun syncClock(socket: DatagramSocket) {
        var buffer = Constants.clockRequest.toByteArray()
        var packet = DatagramPacket(buffer, buffer.size, InetAddress.getByName(serverIP), serverPort)

        socket.send(packet)

        buffer = ByteArray(100)
        packet = DatagramPacket(buffer, buffer.size)

        socket.receive(packet)

        val data = String(packet.data)
        val endInd = data.indexOf('\n')

        if (endInd < 0) {
            return
        }

        val realTime = data.substring(0, endInd).toLong()

        semClock.acquire()
        val rnd = Random()
        val secs = rnd.nextInt(46) + 5
        val d = (clock + secs * Constants.second - clock - Constants.I) / 2
        val newClock = realTime + d
        val oldClock = clock
        clock = newClock
        semClock.release()

        println("$name - Sending sync request in ${helper.getRealTime(oldClock)} + $secs sec")
        println("$name - Old clock: ${helper.getRealTime(oldClock + secs * Constants.second)}")
        println("$name - New clock: ${helper.getRealTime(newClock)}")
    }

    internal abstract fun sendParm(socket: DatagramSocket, trafficServerIP: String, trafficServerPort: Int)
    internal abstract fun updateClock(): Long
}