package Sensors.Handler

import Utils.ClockHelper
import Utils.Constants
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Random

abstract class Sensors(serverIP: String, serverPort: Int, k: Long) {
    protected var name: Char? = null

    private val serverIP = serverIP
    private val serverPort = serverPort
    private val syncMins = k

    protected var clock: Long = 0
    protected val helper = ClockHelper()
    protected var sendVarTime: Long = 0

    fun monitorTraffic(port: Int, trafficServerIP: String, trafficServerPort: Int) {
        val socket = DatagramSocket(port)

        syncClock(socket)
        var syncCounter = (clock / Constants.minute) % syncMins
        var sendCounter = (clock / Constants.minute) % 6

        val isExact = clock % Constants.minute

        if (isExact != 0.toLong()) {
            val delayToInit = Constants.minute - isExact
            sleep(delayToInit)
            clock += delayToInit

            syncCounter = ++syncCounter % syncMins
            sendCounter = ++sendCounter % 6

            // if (name == 'H') {
            //     println("\t\t\t\t\t\t$name - Initial clock = ${helper.getRealTime(clock)}")
            // }
            // else {
            //     println("$name - Initial clock = ${helper.getRealTime(clock)}")
            // }
        }

        while (true) {
            val timeLapse = updateClock()
        
            sleep(timeLapse)

            clock += timeLapse
            val time = helper.getRealTime(clock)
            // if (name == 'H') {
            //     println("\t\t\t\t\t\t$name - Clock updated = $time")
            // }
            // else {
            //     println("$name - Clock updated = $time")
            // }

            syncCounter++
            sendCounter++

            if (syncCounter == syncMins) {
                syncClock(socket)
                syncCounter = 0
            }

            if (sendCounter == 5.toLong()) {
                sendParm(socket, trafficServerIP, trafficServerPort)
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

        val rnd = Random()
        val secs = rnd.nextInt(46) + 5
        val d = (clock + secs * Constants.second - clock - Constants.I) / 2
        val newClock = realTime + d
        val oldClock = clock
        clock = newClock

        if (name == 'H') {
                println("\t\t\t\t\t\t$name - Old clock: ${helper.getRealTime(oldClock)}")
                println("\t\t\t\t\t\t$name - New clock: ${helper.getRealTime(newClock)}")
        }
        else {
            println("$name - Old clock: ${helper.getRealTime(oldClock)}")
            println("$name - New clock: ${helper.getRealTime(newClock)}")
        }
    }

    internal abstract fun sendParm(socket: DatagramSocket, trafficServerIP: String, trafficServerPort: Int)
    internal abstract fun updateClock(): Long
}