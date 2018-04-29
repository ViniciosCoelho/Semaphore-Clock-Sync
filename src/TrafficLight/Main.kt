package TrafficLight

import TrafficLight.Handler.TrafficLightHandler

fun main(args: Array<String>) {
    print("Choose a port: ")
    val port = readLine()!!.toInt()

    TrafficLightHandler().runTraffic(port)
}