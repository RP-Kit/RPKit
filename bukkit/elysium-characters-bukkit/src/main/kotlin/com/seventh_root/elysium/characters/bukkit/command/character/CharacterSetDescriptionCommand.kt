package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class CharacterSetDescriptionCommand(private val plugin: ElysiumCharactersBukkit) : CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(DescriptionPrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("elysium.characters.command.character.set.description")) {
                val playerProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                val characterProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    if (args.size > 0) {
                        val descriptionBuilder = StringBuilder()
                        for (i in 0..args.size - 1 - 1) {
                            descriptionBuilder.append(args[i]).append(" ")
                        }
                        descriptionBuilder.append(args[args.size - 1])
                        character.description = descriptionBuilder.toString()
                        characterProvider.updateCharacter(character)
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-description-valid")))
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-set-description")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class DescriptionPrompt : StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-description-prompt"))
        }

        override fun acceptInput(context: ConversationContext, input: String): Prompt {
            if (input.equals("end", ignoreCase = true)) {
                if (context.getSessionData("description") == null) {
                    context.setSessionData("description", "")
                }
                val conversable = context.forWhom
                if (conversable is Player) {
                    val playerProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                    val characterProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                    val player = playerProvider.getPlayer(conversable)
                    val character = characterProvider.getActiveCharacter(player)
                    if (character != null) {
                        character.description = context.getSessionData("description") as String
                        characterProvider.updateCharacter(character)
                    }
                }
                return DescriptionSetPrompt()
            } else {
                val previousDescription = context.getSessionData("description") as String
                context.setSessionData("description", previousDescription + " " + input)
                return DescriptionPrompt()
            }
        }

    }

    private inner class DescriptionSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-description-valid"))
        }

    }
}
