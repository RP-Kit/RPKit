package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.api.character.Gender
import com.seventh_root.elysium.api.character.Race
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player

class CharacterNewCommand(private val plugin: ElysiumCharactersBukkit) : CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin).withModality(true).withFirstPrompt(NamePrompt()).withEscapeSequence("cancel").thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console"))).addConversationAbandonedListener { event ->
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
            if (sender.hasPermission("elysium.characters.command.character.new")) {
                conversationFactory.buildConversation(sender).begin()
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-character-new"))))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class NamePrompt : StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-name-prompt"))
        }

        override fun acceptInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("name", input)
            return NameSetPrompt()
        }

    }

    private inner class NameSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return GenderPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-name-valid"))
        }

    }

    private inner class GenderPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core!!.serviceManager.getServiceProvider(BukkitGenderProvider::class.java).getGender(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val genderProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitGenderProvider::class.java)
            context.setSessionData("gender", genderProvider.getGender(input))
            return GenderSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-invalid-gender"))
        }

        override fun getPromptText(context: ConversationContext): String {
            val genderProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitGenderProvider::class.java)
            val genderListBuilder = StringBuilder()
            for (gender in genderProvider.genders) {
                genderListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.gender-list-item")
                        .replace("\$gender", gender.name))).append("\n")
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-prompt") + "\n" + genderListBuilder.toString())
        }

    }

    private inner class GenderSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AgePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-gender-valid"))
        }

    }

    private inner class AgePrompt : NumericPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-prompt"))
        }

        override fun isNumberValid(context: ConversationContext?, input: Number?): Boolean {
            return input!!.toInt() >= plugin.config.getInt("characters.min-age") && input.toInt() <= plugin.config.getInt("characters.max-age")
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: Number?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-invalid-validation"))
        }

        override fun getInputNotNumericText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-invalid-number"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("age", input.toInt())
            return AgeSetPrompt()
        }

    }

    private inner class AgeSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return RacePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-age-valid"))
        }

    }

    private inner class RacePrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core!!.serviceManager.getServiceProvider(BukkitRaceProvider::class.java).getRace(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val raceProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitRaceProvider::class.java)
            context.setSessionData("race", raceProvider.getRace(input))
            return RaceSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-invalid-race"))
        }

        override fun getPromptText(context: ConversationContext): String {
            val raceProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitRaceProvider::class.java)
            val raceListBuilder = StringBuilder()
            for (race in raceProvider.races) {
                raceListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.race-list-item")
                        .replace("\$race", race.name))).append("\n")
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-prompt")) + "\n" + raceListBuilder.toString()
        }

    }

    private inner class RaceSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return DescriptionPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-race-valid"))
        }

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
                context.setSessionData("description", (previousDescription + " ") + input)
                return DescriptionPrompt()
            }
        }

    }

    private inner class DescriptionSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CharacterCreatedPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-description-valid"))
        }

    }

    private inner class CharacterCreatedPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val conversable = context.forWhom
            if (conversable is Player) {
                val characterProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                val playerProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                val player = playerProvider.getPlayer(conversable)
                val newCharacter = BukkitCharacter(
                        plugin = plugin,
                        player = player,
                        name = context.getSessionData("name") as String,
                        gender = context.getSessionData("gender") as Gender,
                        age = context.getSessionData("age") as Int,
                        race = context.getSessionData("race") as Race,
                        description = context.getSessionData("description") as String
                )
                characterProvider.addCharacter(newCharacter)
                characterProvider.setActiveCharacter(player, newCharacter)
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-new-valid"))
        }

    }

}
