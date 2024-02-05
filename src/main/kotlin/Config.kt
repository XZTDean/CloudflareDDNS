import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.util.Properties

object Config {
    private val configFile = File("ddns.conf")
    private val config = Properties()
    private val fields = arrayOf("api_email", "api_key", "zone_id", "domain")

    fun loadConfig(): Boolean {
        if (configFile.exists()) {
            configFile.inputStream().use {
                try {
                    val newConfig = Properties()
                    newConfig.load(it)
                    config.clear()
                    config.putAll(newConfig)
                } catch (_: IOException) {
                    Logger.log("Failed to load config file")
                }
            }
        }
        if (checkConfig()) {
            return true
        }
        saveDefaultConfig()
        return false
    }

    private fun saveDefaultConfig() {
        configFile.outputStream().use {
            try {
                fields.forEach {
                    val value = config.getProperty(it, "")
                    config.setProperty(it, value)
                }
                config.store(it, null)
            } catch (_: IOException) {
                Logger.log("Failed to save default config file")
            }
        }
    }

    private fun checkConfig(): Boolean {
        fields.forEach {
            val value = config.getProperty(it)
            if (value.isNullOrBlank()) {
                return false
            }
        }
        return true
    }

    fun get(key: String): String {
        val ret = config.getProperty(key)
        if (ret.isNullOrBlank()) {
            throw IllegalStateException("Missing config value for $key")
        }
        return ret
    }

    val api_email: String
        get() = get("api_email")

    val api_key: String
        get() = get("api_key")

    val zone_id: String
        get() = get("zone_id")

    val domain: String
        get() = get("domain")
}