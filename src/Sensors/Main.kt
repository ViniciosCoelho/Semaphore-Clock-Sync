import Sensors.Handler.SensorH
import Sensors.Handler.SensorV
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    readLine()

    thread(true) {
        SensorH("localhost", 4000, 2)
            .monitorTraffic(43502, "localhost", 4002)
    }

    SensorV("localhost", 4000, 2)
            .monitorTraffic(43503, "localhost", 4002)
}