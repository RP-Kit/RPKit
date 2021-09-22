package com.rpkit.languages.bukkit.command

import com.rpkit.core.service.Services
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.language.RPKLanguageService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class LanguageListCommand(private val plugin: RPKLanguagesBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.language.command.language.list")) {
            sender.sendMessage(plugin.messages.noPermissionLanguageList)
            return true
        }
        val languageService = Services[RPKLanguageService::class.java]
        if (languageService == null) {
            sender.sendMessage(plugin.messages.noLanguageService)
            return true
        }
        sender.sendMessage(plugin.messages.languageListTitle)
        for (language in languageService.languages) {
            sender.sendMessage(plugin.messages.languageListItem.withParameters(
                language = language
            ))
        }
        return true
    }
}
