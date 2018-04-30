package Utils

import java.util.concurrent.Semaphore

class ClockHelper {
    private var time: Long = 0
    private val sem = Semaphore(1)

    fun getRealTime(time: Long): String {
        val mins = time / Constants.minute
        val seconds = (time % Constants.minute) / Constants.second

        return mins.toString() + ":" + seconds.toString()
    }
}