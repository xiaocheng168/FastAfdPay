package cc.mcyx.fastafdpay.event.api

import cc.mcyx.fastafdpay.event.PayInfo
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class AfdOrderPayEvent(
    val payInfo: PayInfo
) : Event(), Cancellable {

    companion object {
        val handlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    private var isCancel: Boolean = false

    override fun isCancelled(): Boolean {
        return isCancel
    }

    override fun setCancelled(p0: Boolean) {
        this.isCancel = p0
    }
}