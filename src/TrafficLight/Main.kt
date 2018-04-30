package TrafficLight

import TrafficLight.Handler.TrafficLightHandler

fun main(args: Array<String>) {
    print("Choose a port: ")
    val port = readLine()!!.toInt()

    print("Choose a port: ")
    val serverIP = readLine()!!.toInt()

    print("Choose synchronization time in minutes: ")
    val k = readLine()!!.toInt()

    TrafficLightHandler().runTraffic(port)
}