package me.arasple.mc.trmenu.api.action.impl

import me.arasple.mc.trmenu.api.action.base.AbstractAction
import me.arasple.mc.trmenu.api.action.base.ActionOption
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit

/**
 * @author Arasple
 * @date 2021/5/3 20:47
 */
class ActionReloadInventory : AbstractAction() {

    override fun onExecute(player: Player, placeholderPlayer: Player) {
        submit(delay = 2) {
            player.session().playerItemSlots()
        }
    }

    companion object {

        private val name = "(reload|rl)-?inv(entory)?s?".toRegex()

        private val parser: (Any, ActionOption) -> AbstractAction = { _, _ -> ActionReloadInventory() }

        val registery = name to parser

    }

}