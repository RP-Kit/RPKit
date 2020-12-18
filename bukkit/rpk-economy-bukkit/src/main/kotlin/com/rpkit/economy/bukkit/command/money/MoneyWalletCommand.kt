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

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.MessagePrompt
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.ValidatingPrompt
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Money wallet command.
 * Opens the player's active character's wallet with a physical currency representation.
 */
class MoneyWalletCommand(private val plugin: RPKEconomyBukkit) : CommandExecutor {

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
        if (!sender.hasPermission("rpkit.economy.command.money.wallet")) {
            sender.sendMessage(plugin.messages["no-permission-money-wallet"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val currencyService = Services[RPKCurrencyService::class.java]
        if (currencyService == null) {
            sender.sendMessage(plugin.messages["no-currency-service"])
            return true
        }
        if (args.isNotEmpty()) {
            val currency = currencyService.getCurrency(args[0])
            if (currency == null) {
                sender.sendMessage(plugin.messages["money-wallet-currency-invalid-currency"])
                return true
            }
            showWallet(sender, currency)
        } else {
            val currency = currencyService.defaultCurrency
            if (currency == null) {
                conversationFactory.buildConversation(sender).begin()
                return true
            }
            showWallet(sender, currency)
        }
        return true
    }

    private fun showWallet(bukkitPlayer: Player, currency: RPKCurrency) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            bukkitPlayer.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            bukkitPlayer.sendMessage(plugin.messages["no-character-service"])
            return
        }
        val economyService = Services[RPKEconomyService::class.java]
        if (economyService == null) {
            bukkitPlayer.sendMessage(plugin.messages["no-economy-service"])
            return
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            bukkitPlayer.sendMessage(plugin.messages["no-minecraft-profile"])
            return
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            bukkitPlayer.sendMessage(plugin.messages["no-character"])
            return
        }
        val wallet = plugin.server.createInventory(null, 27, "Wallet [" + currency.name + "]")
        val coin = ItemStack(currency.material)
        val meta = coin.itemMeta ?: plugin.server.itemFactory.getItemMeta(coin.type) ?: return
        meta.setDisplayName(currency.nameSingular)
        coin.itemMeta = meta
        val coinStack = ItemStack(currency.material, 64)
        val stackMeta = coinStack.itemMeta ?: plugin.server.itemFactory.getItemMeta(coinStack.type) ?: return
        stackMeta.setDisplayName(currency.nameSingular)
        coinStack.itemMeta = stackMeta
        val remainder = (economyService.getBalance(character, currency) % 64)
        var i = 0
        while (i < economyService.getBalance(character, currency)) {
            val leftover = wallet.addItem(coinStack)
            if (leftover.isNotEmpty()) {
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
    }

    private inner class CurrencyPrompt : ValidatingPrompt() {
        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val currencyService = Services[RPKCurrencyService::class.java] ?: return false
            context.setSessionData("currencyService", currencyService)
            return currencyService.getCurrency(input) != null
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            val currencyService = context.getSessionData("currencyService") as RPKCurrencyService
            context.setSessionData("currency", currencyService.getCurrency(input))
            return CurrencySetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            val currencyService = Services[RPKCurrencyService::class.java] ?: return plugin.messages["no-currency-service"]
            return plugin.messages["money-subtract-currency-prompt"] + "\n" +
                    currencyService.currencies
                            .joinToString("\n") { currency ->
                                plugin.messages["money-subtract-currency-prompt-list-item", mapOf(
                                    "currency" to currency.name
                                )]
                            }
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String {
            return plugin.messages["money-subtract-currency-invalid-currency"]
        }

    }

    private inner class CurrencySetPrompt : MessagePrompt() {
        override fun getNextPrompt(context: ConversationContext): Prompt? {
            showWallet(context.forWhom as Player, context.getSessionData("currency") as RPKCurrency)
            return END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages["money-subtract-currency-valid"]
        }

    }

}