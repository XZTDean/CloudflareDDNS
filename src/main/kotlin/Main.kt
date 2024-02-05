fun main() {
    Runtime.getRuntime().addShutdownHook(Thread {
        Logger.close()
    })

    Config.loadConfig()
    val ipChecker = IpChecker()
    val updater = DdnsUpdater()
    try {
        while (true) {
            try {
                val ip = ipChecker.getIp()
                if (ip.isNotBlank()) {
                    updater.updateDns(ip)
                }
            } catch (e: Exception) {
                if (e is InterruptedException) {
                    throw e
                }
                Logger.log("Exception: $e")
                e.stackTrace.forEach {
                    Logger.log("\t" + it.toString())
                }
            }
            Thread.sleep(1000 * 60 * 5) // 5 minutes
        }
    } finally {
        Logger.close()
    }
}