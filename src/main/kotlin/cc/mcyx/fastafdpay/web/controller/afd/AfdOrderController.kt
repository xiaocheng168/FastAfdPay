package cc.mcyx.fastafdpay.web.controller.afd

import cc.mcyx.fastafdpay.FastAfdPay
import cc.mcyx.fastafdpay.database.OrderDatabase
import cc.mcyx.fastafdpay.event.PayInfo
import cc.mcyx.fastafdpay.event.api.AfdOrderPayEvent
import cc.mcyx.fastafdpay.web.controller.BaseController
import cc.mcyx.fastafdpay.web.controller.payload
import cn.hutool.crypto.digest.MD5
import cn.hutool.http.HttpUtil
import cn.hutool.http.Method
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.sun.net.httpserver.HttpExchange
import org.black_ixx.playerpoints.PlayerPoints
import org.black_ixx.playerpoints.PlayerPointsAPI
import org.bukkit.Bukkit

object AfdOrderController : BaseController(Method.POST) {

    override fun request(p0: HttpExchange): Any? {
        val playerPoints: PlayerPointsAPI = (Bukkit.getPluginManager().getPlugin("PlayerPoints") as PlayerPoints).api
        val payload = p0.payload()
        val order =
            payload.getJSONObject("data")?.getJSONObject("order") ?: throw RuntimeException("无效的数据 $payload")
        val outTradeNo = order.getStr("out_trade_no")
        val id = order.getStr("remark")
        if (id == "") {
            FastAfdPay.fastAfdPay.logger.info("订单 $outTradeNo 没有填写游戏id，已略过处理")
            return null
        }
        OrderDatabase.getOrderByNo(outTradeNo)?.also {
            throw RuntimeException("玩家 $id 充值过程，提交重复订单号! $outTradeNo")
        }
        if (outTradeNo == "202106232138371083454010626") {
            FastAfdPay.fastAfdPay.logger.info("爱发电测试已通过!")
            return null
        }
        if (checkIsPay(outTradeNo).isEmpty()) throw SecurityException("玩家 $id 充值过程，异常请求!并没有查找到这个订单! $outTradeNo")
        val payMoney = order.getDouble("total_amount")
        val offlinePlayer =
            Bukkit.getOfflinePlayer(id) ?: throw RuntimeException("玩家 $id 不存在!,充值金额 $payMoney")
        val points = (payMoney * FastAfdPay.fastAfdPay.config.getInt("scale", 10)).toInt()

        OrderDatabase.addOrder(outTradeNo, offlinePlayer.name!!, payload)
        Bukkit.getScheduler().runTask(FastAfdPay.fastAfdPay, Runnable {
            // CallEvent
            AfdOrderPayEvent(PayInfo(id, outTradeNo, payMoney, points)).also {
                Bukkit.getPluginManager().callEvent(it)
                if (it.isCancelled) return@also
                playerPoints.give(offlinePlayer.uniqueId, points)
                FastAfdPay.fastAfdPay.logger.info("玩家 $id 使用爱发电充值 $payMoney 活的 $points 点券")
                if (offlinePlayer.isOnline) offlinePlayer.player?.sendMessage("充值成功 $payMoney 获得 $points 点券")
            }
        })
        return null
    }

    /**
     * 获得或者筛选对应订单列表，默认查所有
     */
    private fun checkIsPay(outTradeNo: String = ""): JSONArray {
        val post = HttpUtil.createPost("https://afdian.com/api/open/query-order")
        val param = mutableMapOf<String, Any>()
        param["params"] = JSONObject().apply {
            set("out_trade_no", outTradeNo)
        }.toString()
        param["ts"] = System.currentTimeMillis() / 1000
        val token = FastAfdPay.token
        val userId = FastAfdPay.userId
        param["user_id"] = userId
        param["sign"] = MD5.create().digestHex("${token}params${param["params"]}ts${param["ts"]}user_id${userId}")
        post.body(JSONUtil.toJsonStr(param), "application/json")
        val response = JSONUtil.parseObj(post.execute().body() ?: "{}")
        if ((response["ec"] ?: -1) == 200) {
            val data = response.getJSONObject("data")
            val list = data.getJSONArray("list")
            return list
        } else throw RuntimeException(response.getStr("em"))
    }
}