package cc.mcyx.fastafdpay.database.model

import org.ktorm.schema.Table
import org.ktorm.schema.text
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.time.Instant

/**
 * 订单表
 */
object Order : Table<Nothing>("player_order") {
    val orderNo = varchar("order_no").primaryKey()
    val playerName = varchar("player_name")
    val createTime = timestamp("create_time")
    val originData = text("origin_data")
}

/**
 * 订单实体类
 */
data class OrderEntity(
    val orderNo: String,
    val playerName: String,
    val createTime: Instant?,
    val originData: String
)