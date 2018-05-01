package Utils

import java.util.concurrent.Semaphore

class ClockHelper {
    private var time: Long = 0
    private val sem = Semaphore(1)

    fun getRealTime(time: Long): String {
        val mins = time / Constants.minute
        val seconds = (time % Constants.minute) / Constants.second
        
        var secondsStr = seconds.toString()

        if (secondsStr.length == 1) {
            secondsStr = "0" + secondsStr
        }

        return mins.toString() + ":" + secondsStr
    }
}