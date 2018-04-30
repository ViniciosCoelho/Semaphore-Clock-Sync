import Sensors.Handler.SensorH
import Sensors.Handler.SensorV
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    readLine()

    thread(true) {
        SensorH("localhost", 4000, 2)
            .monitorTraffic(43002, "localhost", 4001)
    }

    SensorV("localhost", 4000, 2)
            .monitorTraffic(43003, "localhost", 4001)
}