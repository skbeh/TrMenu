package me.arasple.mc.trmenu.api.action.impl

import me.arasple.mc.trmenu.api.action.base.AbstractAction
import me.arasple.mc.trmenu.api.action.base.ActionOption
import me.arasple.mc.trmenu.util.bukkit.ItemHelper
import me.arasple.mc.trmenu.util.collections.Variables
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.entity.Player
import taboolib.module.chat.HexColor
import taboolib.module.chat.TellrawJson

/**
 * @author Arasple
 * @date 2021/1/31 11:55
 * TELLRAW: Content Message <special@hover=hovertext\nLine2@url=xxxx> <xxx>
 */
class ActionTellraw(content: String, option: ActionOption) : AbstractAction(content, option) {

    override fun onExecute(player: Player, placeholderPlayer: Player) {
        player.spigot().sendMessage(*ComponentSerializer.parse(parseContent(placeholderPlayer)))
    }

    companion object {

        private val matcher = "<(.+?)>".toRegex()

        private val name = "(tell(raw)?|json)s?".toRegex()

        private val parser: (Any, ActionOption) -> AbstractAction = { value, option ->
            val raw = value.toString()
            val json: String
            if (ItemHelper.isJson(raw)) {
                json = raw
            } else {
                val tellraw = TellrawJson()

                Variables(raw, matcher) { it[1] }.element.forEach { result ->

                    if (result.isVariable) {
                        val splits = result.value.split("@")
                        tellraw.append(HexColor.translate(splits[0]))

                        splits.mapNotNull {
                            val keyValue = it.split("=", ":", limit = 2)
                            if (keyValue.size >= 2)
                                keyValue[0] to keyValue[1]
                            else null
                        }.forEach {
                            val (type, content) = it
                            when (type.lowercase()) {
                                "hover" -> tellraw.hoverText(content.replace("\\n", "\n"))
                                "suggest" -> tellraw.suggestCommand(content)
                                "command", "execute" -> tellraw.runCommand(content)
                                "url", "open_url" -> tellraw.openURL(content)
                            }
                        }
                    } else tellraw.append(HexColor.translate(result.value))
                }
                json = tellraw.toRawMessage()
            }

            ActionTellraw(json, option)
        }

        val registery = name to parser

    }

}