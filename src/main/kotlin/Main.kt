fun main() {
    Config.loadConfig()
    val ipChecker = IpChecker()
    val updater = DdnsUpdater()
    while (true) {
        val ip = ipChecker.getIp()
        if (ip.isNotBlank()) {
            updater.updateDns(ip)
        }
        Thread.sleep(1000*60*5) // 5 minutes
    }
}