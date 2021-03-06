package Sensors.Handler

import Utils.Constants
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Random

class SensorV(
        serverIP: String,
        serverPort: Int,
        k: Long
) : Sensors(serverIP, serverPort, k) {

    init {
        name = "V"
    }

    override fun sendParm(socket: DatagramSocket, trafficServerIP: String, trafficServerPort: Int) {
        val rnd = Random()

        val secs = rnd.nextInt(46) + 5
        val sendVarTime = secs * Constants.second
        sleep(sendVarTime)

        println("$name - Message delayed = $secs seconds")

        val qVal = rnd.nextInt(100) + 1
        val buffer = ("q" + qVal.toString() + '\n').toByteArray()
        val packet = DatagramPacket(buffer, buffer.size, InetAddress.getByName(trafficServerIP), trafficServerPort)

        semClock.acquire()
        println("$name - Sending Q = $qVal in clock = ${helper.getRealTime(clock)} + $secs sec")
        semClock.release()

        socket.send(packet)
    }

    override fun updateClock(): Long = Constants.minute - 6 * Constants.second
}