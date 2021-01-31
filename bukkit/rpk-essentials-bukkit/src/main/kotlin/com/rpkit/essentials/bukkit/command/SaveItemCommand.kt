package com.rpkit.essentials.bukkit.command

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class SaveItemCommand(private val plugin: RPKEssentialsBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CommandResult {
        if (!sender.hasPermission("rpkit.essentials.command.saveitem")) {
            sender.sendMessage(plugin.messages.noPermissionSaveItem)
            return NoPermissionFailure("rpkit.essentials.command.saveitem")
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return NotAPlayerFailure()
        }
        val bukkitPlayer = plugin.server.getPlayer(sender.minecraftUUID)
        if (bukkitPlayer == null) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return NotAPlayerFailure()
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.saveItemUsage)
            return IncorrectUsageFailure()
        }
        val itemName = args[0]
        val itemInHand = bukkitPlayer.inventory.itemInMainHand
        val itemsFile = File(plugin.dataFolder, "items.yml")
        val itemsConfig = YamlConfiguration.loadConfiguration(itemsFile)
        itemsConfig.set(itemName, itemInHand)
        itemsConfig.save(itemsFile)
        sender.sendMessage(plugin.messages.saveItemValid.withParameters(itemName))
        return CommandSuccess
    }
}