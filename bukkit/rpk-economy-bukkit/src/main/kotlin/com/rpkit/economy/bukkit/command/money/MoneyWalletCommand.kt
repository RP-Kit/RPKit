/*
 * Copyright 2020 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.economy.bukkit.command.money

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Money wallet command.
 * Opens the player's active character's wallet with a physical currency representation.
 */
class MoneyWalletCommand(private val plugin: RPKEconomyBukkit): CommandExecutor {

    private val conversationFactory = ConversationFactory(plugin)
            .withModality(true)
            .withFirstPrompt(CurrencyPrompt())
            .withEscapeSequence("cancel")
            .thatExcludesNonPlayersWithMessage(plugin.messages["not-from-console"])
            .addConversationAbandonedListener { event ->
                if (!event.gracefulExit()) {
                    val conversable = event.context.forWhom
                    if (conversable is Player) {
                        conversable.sendMessage(plugin.messages["operation-cancelled"])
                    }
                }
            }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.economy.command.money.wallet")) {
            if (sender is Player) {
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
                if (args.isNotEmpty()) {
                    val currency = currencyProvider.getCurrency(args[0])
                    if (currency != null) {
                        showWallet(sender, currency)
                    } else {
                        sender.sendMessage(plugin.messages["money-wallet-currency-invalid-currency"])
                    }
                } else {
                    val currency = currencyProvider.defaultCurrency
                    if (currency != null) {
                        showWallet(sender, currency)
                    } else {
                        conversationFactory.buildConversation(sender).begin()
                    }
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-money-wallet"])
        }
        return true
    }

    private fun showWallet(bukkitPlayer: Player, currency: RPKCurrency) {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
            if (character != null) {
                val wallet = plugin.server.createInventory(null, 27, "Wallet [" + currency.name + "]")
                val coin = ItemStack(currency.material)
                val meta = coin.itemMeta ?: plugin.server.itemFactory.getItemMeta(coin.type) ?: return
                meta.setDisplayName(currency.nameSingular)
                coin.itemMeta = meta
                val coinStack = ItemStack(currency.material, 64)
                val stackMeta = coinStack.itemMeta ?: plugin.server.itemFactory.getItemMeta(coinStack.type) ?: return
                stackMeta.setDisplayName(currency.nameSingular)
                coinStack.itemMeta = stackMeta
                val remainder = (economyProvider.getBalance(character, currency) % 64)
                var i = 0
                while (i < economyProvider.getBalance(character, currency)) {
                    val leftover = wallet.addItem(coinStack)
                    if (!leftover.isEmpty()) {
                        bukkitPlayer.world.dropItem(bukkitPlayer.location, leftover.values.iterator().next())
                    }
                    i += 64
                }
                if (remainder != 0) {
                    val remove = ItemStack(coin)
                    remove.amount = 64 - remainder
                    wallet.removeItem(remove)
                }
                bukkitPlayer.openInventory(wallet)
            } else {
                bukkitPlayer.sendMessage(plugin.messages["no-character"])
            }
        } else {
            bukkitPlayer.sendMessage(plugin.messages["no-minecraft-profile"])
        }
    }

    private inner class CurrencyPrompt: ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("currency", plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).getCurrency(input))
            return CurrencySetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-currency-prompt"] + "\n" +
                    plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class).currencies
                            .joinToString("\n") { currency ->
                                plugin.messages["money-subtract-currency-prompt-list-item", mapOf(
                                        Pair("currency", currency.name)
                                )]
                            }
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["money-subtract-currency-invalid-currency"]
        }

    }

    private inner class CurrencySetPrompt: MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            showWallet(context.forWhom as Player, context.getSessionData("currency") as RPKCurrency)
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-currency-valid"]
        }

    }

}