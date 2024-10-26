package cc.mcyx.fastafdpay

import cc.mcyx.arona.core.listener.annotation.Listener
import cc.mcyx.arona.core.metrics.Metrics
import cc.mcyx.arona.core.plugin.AronaPlugin
import cc.mcyx.fastafdpay.web.WebService

@Listener
class FastAfdPay : AronaPlugin() {

    companion object {
        lateinit var fastAfdPay: FastAfdPay
        lateinit var token: String
        lateinit var userId: String
    }

    override fun onEnabled() {
        saveDefaultConfig()
        WebService(config.getInt("web.port", 8000)).start()
        token = config.getString("afd.token") ?: throw RuntimeException("token 未设置")
        userId = config.getString("afd.userId") ?: throw RuntimeException("userId 未设置")
        fastAfdPay = this
        this.status()
    }

    private fun status() {
        val pluginId = 23720
        val metrics = Metrics(this, pluginId)
        metrics.addCustomChart(Metrics.SimplePie("chart_id") { "FastAfdPay" })
    }
}