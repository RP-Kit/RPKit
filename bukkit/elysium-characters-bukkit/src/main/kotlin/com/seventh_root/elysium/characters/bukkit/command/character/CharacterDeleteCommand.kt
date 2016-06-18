package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class CharacterDeleteCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {
    private val conversationFactory: ConversationFactory
    private val confirmationConversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(CharacterPrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                }
            }
        }
        confirmationConversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(ConfirmationPrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
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
            if (sender.hasPermission("elysium.characters.command.character.delete")) {
                if (args.size > 0) {
                    val characterNameBuilder = StringBuilder()
                    for (i in 0..args.size - 1 - 1) {
                        characterNameBuilder.append(args[i]).append(" ")
                    }
                    characterNameBuilder.append(args[args.size - 1])
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                    val player = playerProvider.getPlayer(sender)
                    var charFound = false
                    // Prioritise exact matches...
                    for (character in characterProvider.getCharacters(player)) {
                        if (character.name.equals(characterNameBuilder.toString(), ignoreCase = true)) {
                            val conversation = confirmationConversationFactory.buildConversation(sender)
                            conversation.context.setSessionData("character_id", character.id)
                            conversation.begin()
                            charFound = true
                            break
                        }
                    }
                    // And fall back to partial matches
                    if (!charFound) {
                        for (character in characterProvider.getCharacters(player)) {
                            if (character.name.toLowerCase().contains(characterNameBuilder.toString().toLowerCase())) {
                                val conversation = confirmationConversationFactory.buildConversation(sender)
                                conversation.context.setSessionData("character_id", character.id)
                                conversation.begin()
                                charFound = true
                                break
                            }
                        }
                    }
                    if (charFound) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-delete-valid")))
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-delete-invalid-character")))
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-delete")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class CharacterPrompt: ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val conversable = context.forWhom
            if (conversable is Player) {
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                val player = playerProvider.getPlayer(conversable)
                for (character in characterProvider.getCharacters(player)) {
                    if (character.name.equals(input, ignoreCase = true)) {
                        return true
                    }
                }
                for (character in characterProvider.getCharacters(player)) {
                    if (character.name.toLowerCase().contains(input.toLowerCase())) {
                        return true
                    }
                }
            }
            return false
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val conversable = context.forWhom
            if (conversable is Player) {
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                val player = playerProvider.getPlayer(conversable)
                var charFound = false
                // Prioritise exact matches...
                for (character in characterProvider.getCharacters(player)) {
                    if (character.name.equals(input, ignoreCase = true)) {
                        context.setSessionData("character_id", character.id)
                        charFound = true
                        break
                    }
                }
                // And fall back to partial matches
                if (!charFound) {
                    for (character in characterProvider.getCharacters(player)) {
                        if (character.name.toLowerCase().contains(input.toLowerCase())) {
                            context.setSessionData("character_id", character.id)
                            break
                        }
                    }
                }
            }
            return CharacterDeletedPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-delete-invalid-character"))
        }

        override fun getPromptText(context: ConversationContext): String {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
            val player = playerProvider.getPlayer(context.forWhom as Player)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
            val characterListBuilder = StringBuilder()
            for (character in characterProvider.getCharacters(player)) {
                characterListBuilder.append("\n").append(
                        ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-list-item")
                                .replace("\$character", character.name)))
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-delete-prompt")) + characterListBuilder.toString()
        }

    }

    private inner class ConfirmationPrompt: BooleanPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Boolean): Prompt? {
            if (input) {
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                val character = characterProvider.getCharacter(context.getSessionData("character_id") as Int)
                characterProvider.removeCharacter(character!!)
                return CharacterDeletedPrompt()
            } else {
                return Prompt.END_OF_CONVERSATION
            }
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-delete-confirmation-invalid-boolean"))
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-delete-confirmation"))
        }

    }

    private inner class CharacterDeletedPrompt: MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-delete-valid"))
        }
    }

}
