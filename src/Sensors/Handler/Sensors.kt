package Sensors.Handler

import Utils.ClockHelper
import Utils.Constants
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

abstract class Sensors(serverIP: String, serverPort: Int, k: Int) {
    private val serverIP = serverIP
    private val serverPort = serverPort
    private val syncMins = k

    protected var clock: Long = 0
    protected val helper = ClockHelper()
    protected var sendVarTime: Long = 0

    fun monitorTraffic(port: Int, trafficServerIP: String, trafficServerPort: Int) {
        var syncCounter = 0
        var sendCounter = 0

        val socket = DatagramSocket(port)

        while (true) {
            sleep(Constants.minute)

            clock += updateClock()
            val time = helper.getRealTime(clock)
            println("Clock updated = $time")

            syncCounter++
            sendCounter++

            if (syncCounter == syncMins) {
                syncClock(socket)
                syncCounter = 0
            }

            if (sendCounter == 5) {
                sendParm(socket, serverIP, serverPort)
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

        // Something is wrong here...
        val d = clock + Constants.resquestDelay - clock - Constants.I
        val newClock = realTime + d
        clock = newClock

        println("New clock: ${helper.getRealTime(newClock)}")
    }

    internal abstract fun sendParm(socket: DatagramSocket, trafficServerIP: String, trafficServerPort: Int)
    internal abstract fun updateClock(): Long
}