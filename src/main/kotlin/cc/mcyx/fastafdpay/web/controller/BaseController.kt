package cc.mcyx.fastafdpay.web.controller

import cc.mcyx.fastafdpay.FastAfdPay
import cn.hutool.core.net.url.UrlQuery
import cn.hutool.core.util.ObjectUtil
import cn.hutool.http.Method
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


abstract class BaseController(private val method: Method = Method.POST) : HttpHandler {
    private val threadPool: ExecutorService = Executors.newSingleThreadExecutor()

    final override fun handle(p0: HttpExchange) {
        threadPool.execute {
            try {
                FastAfdPay.fastAfdPay.logger.info("[${method.name}] ${p0.localAddress} -> ${p0.requestURI}")
                if (method.name != p0.requestMethod) throw RuntimeException("未知接口")
                val response = this.request(p0)
                p0.response(response)
            } catch (th: Throwable) {
                p0.response(code = 500, msg = th.localizedMessage ?: th.toString())
                th.printStackTrace()
            }
        }
    }

    abstract fun request(p0: HttpExchange): Any?
}


fun HttpExchange.payload(): JSONObject {
    return JSONUtil.parseObj(this.requestBody.readBytes().toString(Charsets.UTF_8).also { this.requestBody.close() })
}

fun HttpExchange.param(): UrlQuery {
    return UrlQuery.of(this.requestURI.query, Charsets.UTF_8)
}

fun UrlQuery.getStr(key: String): String {
    return this.get(key).toString()
}


fun HttpExchange.response(data: Any? = null, msg: String = "", code: Int = 200) {
    this.responseHeaders.set("Content-Type", "application/json")
    val bytes = JSONObject().also {
        it.set("ec", code)
        it.set("em", msg)
        if (ObjectUtil.isNotNull(data)) it.set("data", data)
    }.toString().toByteArray(Charsets.UTF_8)
    this.sendResponseHeaders(200, bytes.size.toLong())
    this.responseBody.write(bytes)
    this.responseBody.flush()
    this.responseBody.close()
}