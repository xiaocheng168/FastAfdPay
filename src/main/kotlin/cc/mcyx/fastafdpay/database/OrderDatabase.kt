package cc.mcyx.fastafdpay.database

import cc.mcyx.fastafdpay.FastAfdPay
import cc.mcyx.fastafdpay.database.model.Order
import cc.mcyx.fastafdpay.database.model.OrderEntity
import cn.hutool.json.JSONObject
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.io.File

object OrderDatabase {
    private val connect =
        Database.connect(
            "jdbc:sqlite:${
                File(
                    FastAfdPay.fastAfdPay.dataFolder,
                    "database.db"
                ).path
            }",
            "org.sqlite.JDBC"
        )

    init {
        connect.useConnection {
            it.createStatement().execute(
                """
                CREATE TABLE IF not exists  player_order(
                    order_no VARCHAR(255) NOT NULL PRIMARY KEY,
                    player_name VARCHAR(128) NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    origin_data TEXT NOT NULL
                )
            """.trimIndent()
            )
        }
    }

    fun addOrder(orderNo: String, playerName: String, order: JSONObject) {
        connect.insert(Order) {
            set(it.orderNo, orderNo)
            set(it.playerName, playerName)
            set(it.originData, order.toString())
        }
    }

    fun getOrderByNo(orderNo: String): OrderEntity? {
        connect.from(Order).select().where {
            Order.orderNo eq orderNo
        }.forEach {
            return OrderEntity(
                it[Order.orderNo].toString(),
                it[Order.playerName].toString(),
                it[Order.createTime],
                it[Order.originData].toString()
            )
        }
        return null
    }
}
