package cc.mcyx.fastafdpay.web

import cc.mcyx.fastafdpay.web.controller.afd.AfdOrderController
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

class WebService(private val port: Int) : Thread() {

    override fun run() {
        val ws = HttpServer.create(InetSocketAddress(this.port), 0)
        ws.createContext("/api/afd/order", AfdOrderController)
        ws.start()
    }
}