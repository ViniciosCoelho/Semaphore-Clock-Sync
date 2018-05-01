package TrafficLight.Handler

import Utils.ClockHelper
import Utils.Constants
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Random
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

class TrafficLightHandler(serverIP: String, serverPort: Int, clientPort: Int, k: Long) {
    private val serverIP = serverIP
    private val serverPort = serverPort
    private val clockSyncSocket = DatagramSocket(clientPort)
    private val syncMins: Long = k

    private var p = 0
    private val semP = Semaphore(1)
    private var q = 0
    private val semQ = Semaphore(1)
    private var clock: Long = 0
    private val helper = ClockHelper()

    /*
    private var syncCounter: Long = 0
    private var modeCounter: Long = 0
    */

    fun runTraffic(port: Int) {
        val socket = DatagramSocket(port)

        syncClock()
        var syncCounter = (clock / Constants.minute) % syncMins
        var modeCounter = (clock / Constants.minute) % 6

        val isExact = clock % Constants.minute

        if (isExact != 0.toLong()) {
            val delayToInit = Constants.minute - isExact
            sleep(delayToInit)
            clock += delayToInit

            syncCounter = ++syncCounter % syncMins
            modeCounter = ++modeCounter % 6

            println("Initial clock = ${helper.getRealTime(clock)}")
        }

        thread(true) { receiveParameters(socket) }

        while (true) {
            // Maybe I misunderstood this part
            val timeLapse = Constants.minute + 3 * Constants.second

            sleep(timeLapse)

            clock += timeLapse
            val time = helper.getRealTime(clock)
            // println("Clock updated = $time")

            syncCounter++
            modeCounter++

            if (syncCounter == syncMins) {
                syncClock()
                syncCounter = 0
            }

            if (modeCounter == 6.toLong()) {
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

            val parmVal = data.substring(1, endInd).toInt()

            if (data.contains("p", true)) {
                semP.acquire()
                p = if (p != 0) {
                    if (p < 0) {
                        p
                    } else {
                        -1
                    }
                } else {
                    parmVal
                }
                semP.release()
            } else {
                semQ.acquire()
                q = if (q != 0) {
                    if (q < 0) {
                        q
                    } else {
                        -1
                    }
                } else {
                    parmVal
                }
                semQ.release()
            }
        }
    }

    private fun syncClock() {
        var buffer = Constants.clockRequest.toByteArray()
        var packet = DatagramPacket(buffer, buffer.size, InetAddress.getByName(serverIP), serverPort)

        clockSyncSocket.send(packet)

        buffer = ByteArray(100)
        packet = DatagramPacket(buffer, buffer.size)

        clockSyncSocket.receive(packet)

        val data = String(packet.data)
        val endInd = data.indexOf('\n')

        if (endInd < 0) {
            return
        }

        val realTime = data.substring(0, endInd).toLong()

        println("Old clock: ${helper.getRealTime(clock)}")


        val rnd = Random()
        val secs = rnd.nextInt(46) + 5
        val d = (clock + secs * Constants.second - clock - Constants.I) / 2
        val newClock = realTime + d
        clock = newClock

        println("New clock: ${helper.getRealTime(newClock)}")
    }

    private fun changeMode() {
        semP.acquire()
        val auxP = p
        p = 0
        semP.release()

        semQ.acquire()
        val auxQ = q
        q = 0
        semQ.release()

        val x = if (auxP > 0 && auxQ > 0) {
            auxP.toDouble() / (auxP + auxQ).toDouble()
        } else {
            0.0
        }

        val time = helper.getRealTime(clock)
        val parms = "Clock = $time P = $auxP Q = $auxQ"

        when {
            auxQ == 0 || auxP == 0 -> println("There aren't sufficient parameters!")
            auxQ < 0 || auxP < 0 -> println("Parameters sent more then one time!")
            x <= 0.2 -> println(parms + " Mode 1")
            0.2 < x && x <= 0.4 -> println(parms + " Mode 2")
            0.4 < x && x <= 0.6 -> println(parms + " Mode 3")
            0.6 < x && x <= 0.8 -> println(parms + " Mode 4")
            0.8 < x && x <= 1 -> println(parms + " Mode 5")
        }
    }
}