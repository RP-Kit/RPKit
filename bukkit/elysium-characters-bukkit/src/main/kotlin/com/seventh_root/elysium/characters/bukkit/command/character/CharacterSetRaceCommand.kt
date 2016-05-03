package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class CharacterSetRaceCommand(private val plugin: ElysiumCharactersBukkit) : CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(RacePrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
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
            if (sender.hasPermission("elysium.characters.command.character.set.race")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    if (args.size > 0) {
                        val raceBuilder = StringBuilder()
                        for (i in 0..args.size - 1 - 1) {
                            raceBuilder.append(args[i]).append(" ")
                        }
                        raceBuilder.append(args[args.size - 1])
                        val raceProvider = plugin.core.serviceManager.getServiceProvider(BukkitRaceProvider::class.java)
                        val race = raceProvider.getRace(raceBuilder.toString())
                        if (race != null) {
                            character.race = race
                            characterProvider.updateCharacter(character)
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-valid")))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-invalid-race")))
                        }
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-set-race")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class RacePrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(BukkitRaceProvider::class.java).getRace(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                val raceProvider = plugin.core.serviceManager.getServiceProvider(BukkitRaceProvider::class.java)
                val player = playerProvider.getPlayer(conversable)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    character.race = raceProvider.getRace(input)!!
                    characterProvider.updateCharacter(character)
                }
            }
            return RaceSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-invalid-race"))
        }

        override fun getPromptText(context: ConversationContext): String {
            val raceProvider = plugin.core.serviceManager.getServiceProvider(BukkitRaceProvider::class.java)
            val raceListBuilder = StringBuilder()
            for (race in raceProvider.races) {
                raceListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-list-item")
                        .replace("\$race", race.name))).append("\n")
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-prompt")) + "\n" + raceListBuilder.toString()
        }

    }

    private inner class RaceSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-valid"))
        }

    }

}
