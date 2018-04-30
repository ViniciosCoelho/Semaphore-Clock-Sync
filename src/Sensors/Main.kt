import Sensors.Handler.SensorH
import Sensors.Handler.SensorV
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    readLine()

    thread(true) {
        SensorH("localhost", 4000, 2)
            .monitorTraffic(43502, "10.95.56.99", 4002)
    }

    SensorV("localhost", 4000, 2)
            .monitorTraffic(43503, "10.95.56.99", 4002)
}