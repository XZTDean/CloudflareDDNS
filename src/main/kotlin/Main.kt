fun main() {
    val ipChecker = IpChecker()
    println(ipChecker.getIp())
    Config.loadConfig()
    val updater = DdnsUpdater()
    updater.updateDns(ipChecker.getIp())
}