package com.seventh_root.elysium.economy.bukkit.command.money

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player


class MoneySubtractCommand(val plugin: ElysiumEconomyBukkit): CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(PlayerPrompt())
            .withEscapeSequence("cancel")
            .thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            .addConversationAbandonedListener { event ->
                if (!event.gracefulExit()) {
                    val conversable = event.context.forWhom
                    if (conversable is Player) {
                        conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                    }
                }
            }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("elysium.economy.command.money.subtract")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class.java)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java)
                if (args.size > 0) {
                    val bukkitPlayer = plugin.server.getPlayer(args[0])
                    if (bukkitPlayer != null) {
                        val player = playerProvider.getPlayer(bukkitPlayer)
                        if (args.size > 1) {
                            val character = characterProvider.getCharacters(player)
                                    .filter { character -> character.name.startsWith(args[1]) }
                                    .firstOrNull()
                            if (character != null) {
                                if (args.size > 2) {
                                    val currency = currencyProvider.getCurrency(args[2])
                                    if (currency != null) {
                                        if (args.size > 3) {
                                            try {
                                                val amount = args[3].toInt()
                                                if (amount >= 0) {
                                                    economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - amount)
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-value-valid")))
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-valid")))
                                                } else {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-value-invalid-value-negative")))
                                                }
                                            } catch (exception: NumberFormatException) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-value-invalid-value-number")))
                                            }
                                        } else {
                                            conversationFactory.buildConversation(sender).begin()
                                        }
                                    } else {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-currency-invalid-currency")))
                                    }
                                } else {
                                    conversationFactory.buildConversation(sender).begin()
                                }
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-character-invalid-character")))
                            }
                        } else {
                            conversationFactory.buildConversation(sender).begin()
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-player-invalid-player")))
                    }
                } else {
                    conversationFactory.buildConversation(sender).begin()
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-money-subtract")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class PlayerPrompt: PlayerNamePrompt(plugin) {

        override fun acceptValidatedInput(context: ConversationContext, input: Player): Prompt {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
            val player = playerProvider.getPlayer(input)
            context.setSessionData("player", player)
            return PlayerSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-player-prompt"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-player-invalid-player"))
        }

    }

    private inner class PlayerSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CharacterPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-player-valid"))
        }

    }

    private inner class CharacterPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                    .getCharacters(context.getSessionData("player") as ElysiumPlayer)
                    .filter { character -> character.name.equals(input) }
                    .isNotEmpty()
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("character", plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                    .getCharacters(context.getSessionData("player") as ElysiumPlayer)
                    .filter { character -> character.name.equals(input) }
                    .first()
            )
            return CharacterSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-character-prompt")) +
                    "\n" +
                    plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                            .getCharacters(context.getSessionData("player") as ElysiumPlayer)
                            .map { character -> ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-character-prompt-list-item")).replace("\$character", character.name) }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-character-invalid-character"))
        }
    }

    private inner class CharacterSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CurrencyPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-character-valid"))
        }

    }

    private inner class CurrencyPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java).getCurrency(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("currency", plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java).getCurrency(input))
            return CurrencySetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-currency-prompt")) + "\n" +
                    plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java).currencies
                            .map { currency -> ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-currency-prompt-list-item")).replace("\$currency", currency.name) }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-currency-invalid-currency"))
        }
    }

    private inner class CurrencySetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AmountPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-currency-valid"))
        }

    }

    private inner class AmountPrompt: NumericPrompt() {

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class.java)
            val balance = economyProvider.getBalance(context.getSessionData("character") as ElysiumCharacter, context.getSessionData("currency") as ElysiumCurrency)
            return input.toInt() > 0 && input.toInt() <= balance
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("amount", input.toInt())
            return AmountSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-amount-prompt"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            if (invalidInput.toInt() <= 0) {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-amount-invalid-amount-negative"))
            } else {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-amount-invalid-amount-balance"))
            }
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-amount-invalid-amount-number"))
        }

    }

    private inner class AmountSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MoneySubtractCompletePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-amount-valid"))
        }

    }

    private inner class MoneySubtractCompletePrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class.java)
            val character = context.getSessionData("character") as ElysiumCharacter
            val currency = context.getSessionData("currency") as ElysiumCurrency
            val amount = context.getSessionData("amount") as Int
            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - amount)
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-subtract-valid"))
        }

    }

}