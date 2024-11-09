package cc.mcyx.fastafdpay.command

import cc.mcyx.arona.core.command.BaseCommand
import cc.mcyx.arona.core.command.annotation.Command
import cc.mcyx.arona.core.listener.annotation.Listener
import cc.mcyx.arona.core.listener.annotation.SubscribeEvent
import cc.mcyx.fastafdpay.FastAfdPay
import cc.mcyx.fastafdpay.event.api.AfdOrderPayEvent
import cn.hutool.extra.qrcode.QrCodeUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.util.*

@Command("FAfdOpen", permission = "cc.mcyx.fastafdpay.open")
@Listener
object AfdOpenCommand : BaseCommand() {
    private val payIng = mutableMapOf<Player, ItemStack>()
    private val timer = Timer()
    override fun execute(commandSender: CommandSender, s: String, strings: Array<out String>): Boolean {
        if (commandSender is Player) {
            if (isPaying(commandSender)) {
                commandSender.sendMessage("§c§l请先完成目前订单")
                return false
            }
            commandSender.teleport(commandSender.location.apply { pitch = 90F })
            commandSender.inventory.setItemInMainHand(getItem(commandSender))
        } else commandSender.sendMessage("§a只有玩家可以执行这个命令")
        return true
    }

    private fun getItem(player: Player): ItemStack {
        Bukkit.createMap(player.world).also { mapView ->
            mapView.scale = MapView.Scale.NORMAL
            mapView.addRenderer(object : MapRenderer() {
                override fun render(p0: MapView, p1: MapCanvas, p2: Player) {
                    p1.drawImage(
                        0,
                        0,
                        QrCodeUtil.generate(
                            "https://ifdian.net/order/create?user_id=${FastAfdPay.userId}&remark=${player.name}",
                            128,
                            128
                        )
                    )
                    p0.renderers.clear()
                }
            })
            payIng[player] = player.inventory.itemInMainHand
            val item = ItemStack(getMap(), 1)
            item.durability = mapView.javaClass.getMethod("getId").invoke(mapView).toString().toShort()
            if (item.type.name == "FILLED_MAP") {
                item.itemMeta = (item.itemMeta as MapMeta).also {
                    it.javaClass.getDeclaredMethod("setMapView", MapView::class.java).apply { isAccessible = true }
                        .invoke(it, mapView)
                }
            }
            return item
        }
    }

    private fun getMap(): Material {
        return try {
            return (Material::class.java.getDeclaredField("FILLED_MAP").get(null) as Material)
        } catch (e: Exception) {
            Material.MAP
        }
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun onMove(e: PlayerMoveEvent) {
        if (isPaying(e.player)) e.setTo(e.from)
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun onInt(e: PlayerItemHeldEvent) {
        e.isCancelled = isPaying(e.player)
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun onDrop(e: PlayerDropItemEvent) {
        e.isCancelled = cancelPay(e.player)

    }

    @SubscribeEvent(ignoreCancelled = true)
    fun paySuccess(e: AfdOrderPayEvent) {
        val payInfo = e.payInfo
        val offlinePlayer = Bukkit.getOfflinePlayer(payInfo.playerName)
        if (offlinePlayer.isOnline) {
            val player = offlinePlayer.player!!
            if (isPaying(player)) cancelPay(player, true)
        }
    }

    private fun isPaying(player: Player) = payIng[player] != null
    private fun cancelPay(player: Player, success: Boolean = false): Boolean {
        if (isPaying(player)) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    player.inventory.setItemInMainHand(payIng[player])
                    payIng.remove(player)
                    if (!success) player.sendTitle("", "§c§l取消支付", 5, 10, 5)
                }
            }, 100)
            return true
        }
        return false
    }

    fun cancelAll() {
        payIng.keys.forEach { cancelPay(it) }
        Thread.sleep(1000)
    }
}