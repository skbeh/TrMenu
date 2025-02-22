package me.arasple.mc.trmenu.module.internal.command.impl

import me.arasple.mc.trmenu.module.display.layout.MenuLayout
import me.arasple.mc.trmenu.module.display.texture.Texture
import me.arasple.mc.trmenu.module.internal.command.CommandExpresser
import me.arasple.mc.trmenu.util.Time
import me.arasple.mc.trmenu.util.net.Paster
import org.apache.commons.lang3.math.NumberUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.command.subCommand
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.configuration.MemoryConfiguration
import taboolib.library.configuration.YamlConfiguration
import taboolib.library.xseries.XSound
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.sendLang

/**
 * @author Arasple
 * @date 2020/7/22 12:08
 */
object CommandTemplate : CommandExpresser {

    // menu template <rows>
    override val command = subCommand {
        dynamic(optional = true) {
            suggestion<Player> { _, _ ->
                listOf("1", "2", "3", "4", "5", "6")
            }
            execute<Player> { player, _, argument ->
                val rows = (if (argument.isNotEmpty()) NumberUtils.toInt(argument, 5) else 3).coerceAtMost(6)

                player.openMenu<Basic>("Template#$rows") {
                    rows(rows)
                    handLocked(false)
                    onClose { e ->
                        val inventory = e.inventory

                        if (inventory.all { it == null || it.type == Material.AIR }) {
                            player.sendLang("Command-Template-Empty")
                            return@onClose
                        }

                        XSound.BLOCK_NOTE_BLOCK_BIT.play(player, 1f, 0f)
                        Paster.paste(player, generate(inventory), "yml")

                        inventory.contents.forEach {
                            if (!(it == null || it.type == Material.AIR)) {
                                player.inventory.addItem(it).values.forEach { e -> player.world.dropItem(player.location, e) }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 生成模板
     */
    private fun generate(inventory: Inventory): String {
        val rows = inventory.size / 9
        val keys = MenuLayout.commonKeys.iterator()
        val items = collectItems(inventory)
        val layout = Array(rows) { "         " }.toMutableList()
        val layoutPlayerInventory = Array(4) { "         " }.toMutableList()
        val yaml = YamlConfiguration().also { conf ->
            conf.options().header(
                buildString {
                    appendLine()
                    append("Made by TrMenu Template\n")
                    append("Date: ${Time.formatDate()}\n ")
                    appendLine()
                }
            )
            conf["Title"] = "Template*$rows"
            conf["Layout"] = layout
            if (items.any { it -> it.value.any { it > inventory.size } }) {
                conf["PlayerInventory"] = layoutPlayerInventory
            }
        }

        items.entries.sortedByDescending { it.value.size }.forEach { (item, slots) ->
            val key = keys.next()
            slots.forEach {
                if (it > inventory.size) modifyLayout(layoutPlayerInventory, it, key)
                else modifyLayout(layout, it, key)
            }
            yaml["Icons.$key.display"] = formatDisplaySection(item)
        }

        return yaml.saveToString()
    }

    private fun collectItems(inventory: Inventory): MutableMap<ItemStack, MutableSet<Int>> {
        val items = mutableMapOf<ItemStack, MutableSet<Int>>()

        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i)
            if (!(item == null || item.type == Material.AIR)) {
                items.computeIfAbsent(item) { mutableSetOf() }.add(i)
            }
        }

        return items
    }
    private fun formatDisplaySection(item: ItemStack): ConfigurationSection {
        val section = MemoryConfiguration()
        val meta = item.itemMeta
        section["material"] = Texture.createTexture(item)
        meta?.let {
            if (it.hasDisplayName()) section["name"] = it.displayName
            if (it.hasLore()) section["lore"] = it.lore
            if (it.itemFlags.isNotEmpty()) section["flags"] = it.itemFlags.map { flag -> flag.name }
        }
        if (item.amount > 1) section["amount"] = item.amount
        if (item.enchantments.isNotEmpty()) section["shiny"] = true
        return section
    }

    private fun modifyLayout(layout: MutableList<String>, slot: Int, key: Char) {
        var line = 0
        var pos = slot + 1
        while (pos > 9) {
            pos -= 9
            line++
        }
        layout[line].toCharArray().let {
            it[pos - 1] = key
            layout[line] = String(it)
        }
    }

}