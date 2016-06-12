package com.seventh_root.elysium.economy.bukkit.command.money

import com.seventh_root.elysium.api.character.ElysiumCharacter
import com.seventh_root.elysium.api.economy.ElysiumCurrency
import com.seventh_root.elysium.api.player.ElysiumPlayer
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.BukkitCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.BukkitEconomyProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player


class MoneySetCommand(private val plugin: ElysiumEconomyBukkit): CommandExecutor {

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
            if (sender.hasPermission("elysium.economy.command.money.set")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(BukkitEconomyProvider::class.java)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(BukkitCurrencyProvider::class.java)
                val fromBukkitPlayer = sender
                val fromPlayer = playerProvider.getPlayer(fromBukkitPlayer)
                val fromCharacter = characterProvider.getActiveCharacter(fromPlayer)
                if (fromCharacter != null) {
                    if (args.size > 0) {
                        val toBukkitPlayer = plugin.server.getPlayer(args[0])
                        if (toBukkitPlayer != null) {
                            val toPlayer = playerProvider.getPlayer(toBukkitPlayer)
                            if (args.size > 1) {
                                val character = characterProvider.getCharacters(toPlayer)
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
                                                        if (amount <= 1728) {
                                                            economyProvider.setBalance(character, currency, amount)
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-amount-valid")))
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-valid")))
                                                        } else {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-amount-invalid-amount-limit")))
                                                        }
                                                    } else {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-amount-invalid-amount-negative")))
                                                    }
                                                } catch (exception: NumberFormatException) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-amount-invalid-amount-number")))
                                                }
                                            } else {
                                                conversationFactory.buildConversation(sender).begin()
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-currency-invalid-currency")))
                                        }
                                    } else {
                                        conversationFactory.buildConversation(sender).begin()
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-character-invalid-character")))
                                }
                            } else {
                                conversationFactory.buildConversation(sender).begin()
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-player-invalid-player")))
                        }
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-money-set")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class PlayerPrompt: PlayerNamePrompt(plugin) {

        override fun acceptValidatedInput(context: ConversationContext, input: Player): Prompt {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
            val player = playerProvider.getPlayer(input)
            context.setSessionData("player", player)
            return PlayerSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-player-prompt"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-player-invalid-player"))
        }

    }

    private inner class PlayerSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CharacterPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-player-valid"))
        }

    }

    private inner class CharacterPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                    .getCharacters(context.getSessionData("player") as ElysiumPlayer)
                    .filter { character -> character.name.equals(input) }
                    .isNotEmpty()
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("character", plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                    .getCharacters(context.getSessionData("player") as ElysiumPlayer)
                    .filter { character -> character.name.equals(input) }
                    .first()
            )
            return CharacterSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-character-prompt")) +
                    "\n" +
                    plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                            .getCharacters(context.getSessionData("player") as ElysiumPlayer)
                            .map { character -> ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-character-prompt-list-item")).replace("\$character", character.name) }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-character-invalid-character"))
        }
    }

    private inner class CharacterSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return CurrencyPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-character-valid"))
        }

    }

    private inner class CurrencyPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(BukkitCurrencyProvider::class.java).getCurrency(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("currency", plugin.core.serviceManager.getServiceProvider(BukkitCurrencyProvider::class.java).getCurrency(input))
            return CurrencySetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-currency-prompt")) + "\n" +
                    plugin.core.serviceManager.getServiceProvider(BukkitCurrencyProvider::class.java).currencies
                            .map { currency -> ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-currency-prompt-list-item")).replace("\$currency", currency.name) }
                            .joinToString("\n")
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-currency-invalid-currency"))
        }
    }

    private inner class CurrencySetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return AmountPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-currency-valid"))
        }

    }

    private inner class AmountPrompt: NumericPrompt() {

        override fun isNumberValid(context: ConversationContext, input: Number): Boolean {
            return input.toInt() >= 0 && input.toInt() <= 1728
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("amount", input.toInt())
            return AmountSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-amount-prompt"))
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: Number): String {
            if (invalidInput.toInt() < 0) {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-amount-invalid-amount-negative"))
            } else {
                return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-amount-invalid-amount-limit"))
            }
        }

        override fun getInputNotNumericText(context: ConversationContext, invalidInput: String): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-amount-invalid-amount-number"))
        }

    }

    private inner class AmountSetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt {
            return MoneySetCompletePrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-amount-valid"))
        }

    }

    private inner class MoneySetCompletePrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            val economyProvider = plugin.core.serviceManager.getServiceProvider(BukkitEconomyProvider::class.java)
            val character = context.getSessionData("character") as ElysiumCharacter
            val currency = context.getSessionData("currency") as ElysiumCurrency
            val amount = context.getSessionData("amount") as Int
            economyProvider.setBalance(character, currency, amount)
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-set-valid"))
        }

    }
    
}