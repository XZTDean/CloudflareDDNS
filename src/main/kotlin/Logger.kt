import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val logFile = File("log/ddns_client.log")
    private val log = mutableListOf<String>()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O")
    private val logChannel = Channel<String>(64)

    init {
        if (!logFile.parentFile.exists()) {
            logFile.parentFile.mkdirs()
        }
        autoSaveLog()
    }

    fun log(message: String) {
        val timeString = ZonedDateTime.now().format(formatter)
        runBlocking {
            message.split("\n").forEach {
                logChannel.send("[$timeString] $it")
            }
        }
    }

    fun close() {
        logChannel.close()
    }

    private fun autoSaveLog() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            for (log in logChannel) {
                try {
                    logFile.appendText("$log\n")
                    println(log)
                } catch (e: IOException) {
                    println("Failed to write to log file\n${log}")
                }
            }
        }
    }
}