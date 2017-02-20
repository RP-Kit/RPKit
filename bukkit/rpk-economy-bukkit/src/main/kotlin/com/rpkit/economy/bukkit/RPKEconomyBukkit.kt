/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.economy.bukkit

import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldProvider
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.economy.bukkit.character.MoneyField
import com.rpkit.economy.bukkit.command.currency.CurrencyCommand
import com.rpkit.economy.bukkit.command.money.MoneyCommand
import com.rpkit.economy.bukkit.command.money.MoneyPayCommand
import com.rpkit.economy.bukkit.command.money.MoneyWalletCommand
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.currency.RPKCurrencyProviderImpl
import com.rpkit.economy.bukkit.database.table.MoneyHiddenTable
import com.rpkit.economy.bukkit.database.table.RPKCurrencyTable
import com.rpkit.economy.bukkit.database.table.RPKWalletTable
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProviderImpl
import com.rpkit.economy.bukkit.listener.InventoryCloseListener
import com.rpkit.economy.bukkit.listener.PlayerInteractListener
import com.rpkit.economy.bukkit.listener.PluginEnableListener
import com.rpkit.economy.bukkit.listener.SignChangeListener

/**
 * RPK economy plugin default implementation.
 */
class RPKEconomyBukkit: RPKBukkitPlugin() {

    private lateinit var currencyProvider: RPKCurrencyProvider
    private lateinit var economyProvider: RPKEconomyProvider
    private var moneyFieldInitialised: Boolean = false

    override fun onEnable() {
        saveDefaultConfig()
        currencyProvider = RPKCurrencyProviderImpl(this)
        economyProvider = RPKEconomyProviderImpl(this)
        serviceProviders = arrayOf(
                currencyProvider,
                economyProvider
        )
    }

    override fun onPostEnable() {
        attemptCharacterCardFieldInitialisation()
    }

    fun attemptCharacterCardFieldInitialisation() {
        if (!moneyFieldInitialised) {
            try {
                core.serviceManager.getServiceProvider(RPKCharacterCardFieldProvider::class)
                        .characterCardFields.add(MoneyField(this))
                moneyFieldInitialised = true
            } catch (ignore: UnregisteredServiceException) {}
        }
    }

    override fun registerCommands() {
        getCommand("money").executor = MoneyCommand(this)
        getCommand("pay").executor = MoneyPayCommand(this)
        getCommand("wallet").executor = MoneyWalletCommand(this)
        getCommand("currency").executor = CurrencyCommand(this)
    }

    override fun registerListeners() {
        registerListeners(
                InventoryCloseListener(this),
                PlayerInteractListener(this),
                PluginEnableListener(this),
                SignChangeListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKCurrencyTable(database, this))
        database.addTable(RPKWalletTable(database, this))
        database.addTable(MoneyHiddenTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("money-usage", "&cUsage: /money [subtract|add|set|view|pay|wallet]")
        messages.setDefault("money-subtract-player-prompt", "&fWhich player would you like to subtract money from? &7(Type cancel to cancel)")
        messages.setDefault("money-subtract-player-invalid-player", "&cThat player is not online.")
        messages.setDefault("money-subtract-player-valid", "&aPlayer set.")
        messages.setDefault("money-subtract-character-prompt", "&fWhich character would you like to subtract money from? &7(Type cancel to cancel)")
        messages.setDefault("money-subtract-character-prompt-list-item", "&f- &7\$character")
        messages.setDefault("money-subtract-character-invalid-character", "&cThat player does not have a character by that name.")
        messages.setDefault("money-subtract-character-valid", "&aCharacter set.")
        messages.setDefault("money-subtract-currency-prompt", "&fWhich currency would you like to use? &7(Type cancel to cancel)")
        messages.setDefault("money-subtract-currency-prompt-list-item", "&f- &7\$currency")
        messages.setDefault("money-subtract-currency-invalid-currency", "&cThat's not a valid currency.")
        messages.setDefault("money-subtract-currency-valid", "&aCurrency set.")
        messages.setDefault("money-subtract-amount-prompt", "&fHow much money would you like to subtract? &7(Type cancel to cancel)")
        messages.setDefault("money-subtract-amount-invalid-amount-balance", "&cThat player's character does not have enough money.")
        messages.setDefault("money-subtract-amount-invalid-amount-negative", "&cYou may not subtract negative money.")
        messages.setDefault("money-subtract-amount-invalid-amount-number", "&cYou must specify a number to subtract.")
        messages.setDefault("money-subtract-amount-valid", "&aAmount set.")
        messages.setDefault("money-subtract-valid", "&aMoney subtracted.")
        messages.setDefault("money-add-player-prompt", "&fWhich player would you like to add money to? &7(Type cancel to cancel)")
        messages.setDefault("money-add-player-invalid-player", "&cThat player is not online.")
        messages.setDefault("money-add-player-valid", "&aPlayer set.")
        messages.setDefault("money-add-character-prompt", "&fWhich character would you like to add money to? &7(Type cancel to cancel)")
        messages.setDefault("money-add-character-prompt-list-item", "&f- &7\$character")
        messages.setDefault("money-add-character-invalid-character", "&cThat player does not have a character by that name.")
        messages.setDefault("money-add-character-valid", "&aCharacter set.")
        messages.setDefault("money-add-currency-prompt", "&fWhich currency would you like to use? &7(Type cancel to cancel)")
        messages.setDefault("money-add-currency-prompt-list-item", "&f- &7\$currency")
        messages.setDefault("money-add-currency-invalid-currency", "&cThat's not a valid currency.")
        messages.setDefault("money-add-currency-valid", "&aCurrency set.")
        messages.setDefault("money-add-amount-prompt", "&fHow much money would you like to add? &7(Type cancel to cancel)")
        messages.setDefault("money-add-amount-invalid-amount-negative", "&cYou may not add negative money.")
        messages.setDefault("money-add-amount-invalid-amount-number", "&cYou must specify a number to add.")
        messages.setDefault("money-add-amount-invalid-amount-limit", "&cThat amount would not fit in the receiver's wallet.")
        messages.setDefault("money-add-amount-valid", "&aAmount set.")
        messages.setDefault("money-add-valid", "&aMoney added.")
        messages.setDefault("money-set-player-prompt", "&fWhich player would you like to set the balance of? &7(Type cancel to cancel)")
        messages.setDefault("money-set-player-invalid-player", "&cThat player is not online.")
        messages.setDefault("money-set-player-valid", "&aPlayer set.")
        messages.setDefault("money-set-character-prompt", "&fWhich character would you like to set the balance of? &7(Type cancel to cancel)")
        messages.setDefault("money-set-character-prompt-list-item", "&f- &7\$character")
        messages.setDefault("money-set-character-invalid-character", "&cThat player does not have a character by that name.")
        messages.setDefault("money-set-character-valid", "&aCharacter set.")
        messages.setDefault("money-set-currency-prompt", "&fWhich currency would you like to use? &7(Type cancel to cancel)")
        messages.setDefault("money-set-currency-prompt-list-item", "&f- &7\$currency")
        messages.setDefault("money-set-currency-invalid-currency", "&cThat's not a valid currency.")
        messages.setDefault("money-set-currency-valid", "&aCurrency set.")
        messages.setDefault("money-set-amount-prompt", "&fWhat would you like to set the balance to? &7(Type cancel to cancel)")
        messages.setDefault("money-set-amount-invalid-amount-negative", "&cYou may not set balance to a negative number.")
        messages.setDefault("money-set-amount-invalid-amount-number", "&cYou must specify a number to set the balance to.")
        messages.setDefault("money-set-amount-invalid-amount-limit", "&cThat amount would not fit in the receiver's wallet.")
        messages.setDefault("money-set-amount-valid", "&aAmount set.")
        messages.setDefault("money-set-valid", "&aBalance set.")
        messages.setDefault("money-view-player-prompt", "&fWhich player would you like to view the balance of?")
        messages.setDefault("money-view-player-invalid-player", "&cThat player is not online.")
        messages.setDefault("money-view-player-valid", "&aPlayer set.")
        messages.setDefault("money-view-character-prompt", "&fWhich character would you like to view money of? &7(Type cancel to cancel)")
        messages.setDefault("money-view-character-prompt-list-item", "&f- &7\$character")
        messages.setDefault("money-view-character-invalid-character", "&cThat player does not have a character by that name.")
        messages.setDefault("money-view-character-valid", "&aCharacter set.")
        messages.setDefault("money-view-currency-prompt", "&fWhich currency would you like to use? &7(Type cancel to cancel)")
        messages.setDefault("money-view-currency-prompt-list-item", "&f- &7\$currency")
        messages.setDefault("money-view-currency-invalid-currency", "&cThat's not a valid currency.")
        messages.setDefault("money-view-currency-valid", "&aCurrency set.")
        messages.setDefault("money-pay-player-prompt", "&fWhich player would you like to pay? &7(Type cancel to cancel)")
        messages.setDefault("money-pay-player-invalid-player-offline", "&cThat player is not online.")
        messages.setDefault("money-pay-player-invalid-player-distance", "&cThat player is too far away to pay.")
        messages.setDefault("money-pay-player-valid", "&aPlayer set.")
        messages.setDefault("money-pay-character-prompt", "&fWhich character would you like to pay? &7(Type cancel to cancel)")
        messages.setDefault("money-pay-character-prompt-list-item", "&f- &7\$character")
        messages.setDefault("money-pay-character-invalid-character", "&cThat player does not have a character by that name.")
        messages.setDefault("money-pay-character-valid", "&aCharacter set.")
        messages.setDefault("money-pay-currency-prompt", "&fWhich currency would you like to use? &7(Type cancel to cancel)")
        messages.setDefault("money-pay-currency-prompt-list-item", "&f- &7\$currency")
        messages.setDefault("money-pay-currency-invalid-currency", "&cThat's not a valid currency.")
        messages.setDefault("money-pay-currency-valid", "&aCurrency set.")
        messages.setDefault("money-pay-amount-prompt", "&fHow much would you like to pay? &7(Type cancel to cancel)")
        messages.setDefault("money-pay-amount-invalid-amount-balance", "&cYou may not pay more money than you have.")
        messages.setDefault("money-pay-amount-invalid-amount-negative", "&cYou may not pay a negative amount.")
        messages.setDefault("money-pay-amount-invalid-amount-number", "&cYou must specify a number to pay")
        messages.setDefault("money-pay-amount-invalid-amount-limit", "&cThat amount would not fit in the receiver's wallet.")
        messages.setDefault("money-pay-amount-valid", "&aAmount set.")
        messages.setDefault("money-pay-valid", "&aPaid \$amount \$currency to \$character.")
        messages.setDefault("money-pay-received", "&aReceived \$amount \$currency from \$character.")
        messages.setDefault("money-view-player-prompt", "&fWhich player would you like to view the money of? &7(Type cancel to cancel)")
        messages.setDefault("money-view-player-invalid-player", "&cThat player is not online.")
        messages.setDefault("money-view-player-valid", "&aPlayer set.")
        messages.setDefault("money-view-character-prompt", "&fWhich character would you like to view the money of? &7(Type cancel to cancel)")
        messages.setDefault("money-view-character-prompt-list-item", "&f- &7\$character")
        messages.setDefault("money-view-character-invalid-character", "&cThat player does not have a character by that name.")
        messages.setDefault("money-view-valid", "&fBalance:")
        messages.setDefault("money-view-valid-list-item", "&7\$currency: &f\$balance")
        messages.setDefault("money-wallet-currency-prompt", "&fWhich currency would you like to use?")
        messages.setDefault("money-wallet-currency-prompt-list-item", "&f- &7\$currency")
        messages.setDefault("money-wallet-currency-invalid-currency", "&cThat's not a valid currency.")
        messages.setDefault("money-wallet-currency-valid", "&aCurrency set.")
        messages.setDefault("money-wallet-valid", "&aOpening wallet...")
        messages.setDefault("currency-usage", "&cUsage: /currency [add|remove|list]")
        messages.setDefault("currency-set-name-prompt", "&fWhat is the currency's name?")
        messages.setDefault("currency-set-name-invalid-name", "&cA currency by that name already exists.")
        messages.setDefault("currency-set-name-valid", "&aName set.")
        messages.setDefault("currency-set-name-singular-prompt", "&fWhat would you like the display name for referring to the currency in it's singular form to be?")
        messages.setDefault("currency-set-name-singular-valid", "&aSingular name set.")
        messages.setDefault("currency-set-name-plural-prompt", "&fWhat would you like the display name for referring to the currency in it's plural form to be?")
        messages.setDefault("currency-set-name-plural-valid", "&aPlural name set.")
        messages.setDefault("currency-set-rate-prompt", "&fWhat would you like to set the rate of conversion to?")
        messages.setDefault("currency-set-rate-invalid-rate-number", "&cRate must be a number.")
        messages.setDefault("currency-set-rate-invalid-rate-negative", "&cRate must be greater than zero.")
        messages.setDefault("currency-set-rate-valid", "&aRate set.")
        messages.setDefault("currency-set-default-amount-prompt", "&fHow much of this currency would you like new characters to have of this currency by default?")
        messages.setDefault("currency-set-default-amount-invalid-amount-number", "&cDefault amount must be a number.")
        messages.setDefault("currency-set-default-amount-invalid-amount-negative", "&cDefault amount cannot be negative.")
        messages.setDefault("currency-set-default-amount-valid", "&aDefault amount set.")
        messages.setDefault("currency-set-material-prompt", "&fWhat would you like to use to represent this currency as a physical currency?")
        messages.setDefault("currency-set-material-invalid-material", "&cThat material is not valid.")
        messages.setDefault("currency-set-material-valid", "&aMaterial set.")
        messages.setDefault("currency-add-valid", "&aCurrency created.")
        messages.setDefault("currency-remove-prompt", "&fWhich currency would you like to remove?")
        messages.setDefault("currency-remove-invalid-currency", "&cThat currency does not exist.")
        messages.setDefault("currency-remove-valid", "&aCurrency removed.")
        messages.setDefault("currency-list-title", "&fCurrencies:")
        messages.setDefault("currency-list-item", "&f- &7\$currency")
        messages.setDefault("exchange-sign-invalid-format-from", "&cThe format of the second line must be \"[amount] [currency]\"")
        messages.setDefault("exchange-sign-invalid-currency-from", "&cThe currency on the second line is invalid.")
        messages.setDefault("exchange-sign-invalid-currency-to", "&cThe currency on the fourth line is invalid.")
        messages.setDefault("exchange-valid", "&aExchanged \$from-amount \$from-currency for \$to-amount \$to-currency.")
        messages.setDefault("exchange-invalid-wallet-balance-too-high", "&cThe amount you are attempting to exchange for would not fit in your wallet.")
        messages.setDefault("exchange-invalid-wallet-balance-too-low", "&cYou do not have enough money.")
        messages.setDefault("exchange-invalid-format", "&cThat exchange sign is not formatted correctly.")
        messages.setDefault("not-from-console", "&cYou may not use this command from console.")
        messages.setDefault("operation-cancelled", "&cOperation cancelled.")
        messages.setDefault("no-permission-money-subtract", "&cYou do not have permission to subtract money.")
        messages.setDefault("no-permission-money-add", "&cYou do not have permission to add money.")
        messages.setDefault("no-permission-money-set", "&cYou do not have permission to set money.")
        messages.setDefault("no-permission-money-view-self", "&cYou do not have permission to view your own money.")
        messages.setDefault("no-permission-money-view-other", "&cYou do not have permission to view other people's money")
        messages.setDefault("no-permission-money-pay", "&cYou do not have permission to pay people money.")
        messages.setDefault("no-permission-money-wallet", "&cYou do not have permission to open your wallet.")
        messages.setDefault("no-permission-currency-add", "&cYou do not have permission to add currencies.")
        messages.setDefault("no-permission-currency-remove", "&cYou do not have permission to remove currencies.")
        messages.setDefault("no-permission-currency-list", "&cYou do not have permission to list currencies.")
        messages.setDefault("no-permission-exchange-create", "&cYou do not have permission to create exchanges.")
        messages.setDefault("no-character", "&cYou need a character to transfer money. Please create one.")
        messages.setDefault("recipient-no-character", "&cThe recipient needs a character to transfer money to. Please get them to create one.")
    }

}