package TrafficLight

import TrafficLight.Handler.TrafficLightHandler

fun main(args: Array<String>) {
    TrafficLightHandler("localhost", 4000, 43001, 2)
            .runTraffic(4002)
}