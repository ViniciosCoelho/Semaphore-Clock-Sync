package TrafficLight

import TrafficLight.Handler.TrafficLightHandler

fun main(args: Array<String>) {
    print("Time server IP: ")
    val serverIP = readLine()!!.toString()

    print("Time server port: ")
    val serverPort = readLine()!!.toInt()

    print("Choose synchronization time in minutes: ")
    val k = readLine()!!.toInt()

    print("Choose a port to run the client: ")
    val clientPort = readLine()!!.toInt()

    print("Choose a port to run the service: ")
    val port = readLine()!!.toInt()

    TrafficLightHandler(serverIP, serverPort, clientPort, k).runTraffic(port)
}