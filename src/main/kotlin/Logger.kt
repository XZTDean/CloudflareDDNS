import java.io.File
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val logFile = File("log/ddns_client.log")
    private val log = mutableListOf<String>()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O")

    init {
        if (!logFile.parentFile.exists()) {
            logFile.parentFile.mkdirs()
        }
    }

    fun log(message: String) {
        val timeString = ZonedDateTime.now().format(formatter)
        message.split("\n").forEach {
            log.add("[$timeString] $it")
        }
    }

    fun saveLog() {
        if (log.isEmpty()) {
            return
        }
        try {
            val outputLogs = log.joinToString("\n") + "\n"
            logFile.appendText(outputLogs)
            print(outputLogs)
            log.clear()
        } catch (_: IOException) {
            println("Failed to write to log file")
        }
    }
}