package cc.mcyx.fastafdpay

import cc.mcyx.arona.config.AronaConfig
import cc.mcyx.arona.core.listener.annotation.Listener
import cc.mcyx.arona.core.loader.AronaLoader
import cc.mcyx.arona.core.loader.LibInfo
import cc.mcyx.arona.core.plugin.AronaPlugin
import cc.mcyx.fastafdpay.command.AfdOpenCommand
import cc.mcyx.fastafdpay.web.WebService

@Listener
class FastAfdPay : AronaPlugin() {

    companion object {
        lateinit var fastAfdPay: FastAfdPay
        lateinit var token: String
        lateinit var userId: String
    }

    private lateinit var ws: WebService
    override fun onLoaded() {
        AronaLoader.loadCloudLib(LibInfo("com.google.zxing", "core", "3.3.3", LibInfo.Source.ALIBABA))
        AronaLoader.loadCloudLib(LibInfo("org.ktorm", "ktorm-core", "4.1.1", LibInfo.Source.ALIBABA))
        AronaLoader.loadCloudLib(LibInfo("org.ktorm", "ktorm-support-sqlite", "4.1.1", LibInfo.Source.ALIBABA))
    }

    override fun onEnabled() {
        fastAfdPay = this
        AronaConfig(this, "config.yml", saveResource = true).also { ac ->
            fun loadConfig() {
                token = ac.config.getString("afd.token") ?: ""
                userId = ac.config.getString("afd.userId") ?: ""
            }
            ac.configReload { loadConfig() }
            loadConfig()
        }
        ws = WebService(config.getInt("web.port", 8000)).also { it.start() }
        metricsCall(23720)
    }

    override fun onDisabled() {
        AfdOpenCommand.cancelAll()
        ws.closeService()
    }
}