import java.io.File
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val logFile = File("log/ddns_client.log")
    private val log = mutableListOf<String>()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O")


    fun log(message: String) {
        val timeString = ZonedDateTime.now().format(formatter)
        log.add("[$timeString] $message")
    }

    fun saveLog() {
        if (log.isEmpty()) {
            return
        }
        try {
            logFile.appendText(log.joinToString("\n"))
            log.clear()
        } catch (_: IOException) {
            println("Failed to write to log file")
        }
    }
}