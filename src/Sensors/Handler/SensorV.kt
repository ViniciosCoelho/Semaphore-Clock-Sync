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
        k: Int
) : Sensors(serverIP, serverPort, k) {

    init {
        name = 'V'
    }

    override fun sendParm(socket: DatagramSocket, trafficServerIP: String, trafficServerPort: Int) {
        val rnd = Random()

        val secs = (rnd.nextInt(50) + 5) % 50
        sendVarTime = secs * Constants.second
        sleep(sendVarTime)

        println("$name - Message delayed = $secs seconds")

        val qVal = rnd.nextInt(100) + 1
        val buffer = ("q" + qVal.toString() + '\n').toByteArray()
        val packet = DatagramPacket(buffer, buffer.size, InetAddress.getByName(trafficServerIP), trafficServerPort)

        clock += sendVarTime
        println("$name - Sending Q = $qVal in clock = ${helper.getRealTime(clock)}")

        socket.send(packet)
    }

    override fun updateClock(): Long {
        val updVal = Constants.minute + 6 * Constants.second - sendVarTime
        sendVarTime = 0
        return updVal
    }
}