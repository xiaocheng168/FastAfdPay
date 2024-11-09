package cc.mcyx.fastafdpay.command

import cc.mcyx.arona.core.command.BaseCommand
import cc.mcyx.arona.core.command.annotation.Command
import cc.mcyx.arona.core.listener.annotation.Listener
import cc.mcyx.arona.core.listener.annotation.SubscribeEvent
import cc.mcyx.fastafdpay.FastAfdPay
import cn.hutool.extra.qrcode.QrCodeUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.util.Timer
import java.util.TimerTask

@Command("FAfdOpen", permission = "cc.mcyx.fastafdpay.open")
@Listener
object AfdOpenCommand : BaseCommand() {
    private val payIng = mutableMapOf<Player, ItemStack>()
    private val timer = Timer()
    override fun execute(commandSender: CommandSender?, s: String?, strings: Array<out String>?): Boolean {
        if (commandSender is Player) {
            if (isPaying(commandSender)) {
                commandSender.sendMessage("§c§l请先完成目前订单")
                return false
            }
            Bukkit.createMap(commandSender.world).also {
                it.scale = MapView.Scale.NORMAL
                it.addRenderer(object : MapRenderer() {
                    override fun render(p0: MapView?, p1: MapCanvas?, p2: Player?) {
                        p1?.drawImage(
                            0,
                            0,
                            QrCodeUtil.generate(
                                "https://ifdian.net/order/create?user_id=${FastAfdPay.userId}&remark=${commandSender.name}",
                                128,
                                128
                            )
                        )
                        p0?.renderers?.clear()
                    }
                })
                payIng[commandSender] = commandSender.inventory.itemInMainHand
                val itemStack = ItemStack(Material.MAP, 1)
                itemStack.durability = it.id
                commandSender.inventory.itemInMainHand = itemStack
                commandSender.teleport(commandSender.location.apply { pitch = 90F })
            }
        } else commandSender?.sendMessage("§a只有玩家可以执行这个命令")
        return true
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun onMove(e: PlayerMoveEvent) {
        if (isPaying(e.player)) e.to = e.from
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun onInt(e: PlayerItemHeldEvent) {
        e.isCancelled = isPaying(e.player)
    }

    @SubscribeEvent(ignoreCancelled = true)
    fun onDrop(e: PlayerDropItemEvent) {
        e.isCancelled = cancelPay(e.player)

    }

    private fun isPaying(player: Player) = payIng[player] != null
    private fun cancelPay(player: Player): Boolean {
        if (isPaying(player)) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    player.inventory.itemInMainHand = payIng[player]
                    payIng.remove(player)
                    player.sendTitle("", "§c§l取消支付", 5, 10, 5)
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