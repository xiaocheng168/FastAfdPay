package cc.mcyx.fastafdpay.event

data class PayInfo(
    val playerName: String,
    val outTradeNo: String,
    val payMoney: Double,
    val points: Int
)