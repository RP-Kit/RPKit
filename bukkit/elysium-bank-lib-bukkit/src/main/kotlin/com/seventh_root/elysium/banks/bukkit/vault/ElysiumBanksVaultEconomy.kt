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

package com.seventh_root.elysium.banks.bukkit.vault

import com.seventh_root.elysium.banks.bukkit.ElysiumBankLibBukkit
import com.seventh_root.elysium.banks.bukkit.bank.ElysiumBankProvider
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType.FAILURE
import net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS
/**
 * A Vault [Economy] implementation for banks plugins.
 * Registered at higher priority than the elysium-economy implementation
 */
class ElysiumBanksVaultEconomy(private val plugin: ElysiumBankLibBukkit): AbstractEconomy() {

    override fun getBanks(): MutableList<String>? {
        throw UnsupportedOperationException("elysium-bank-lib does not support listing of banks.")
    }

    override fun getBalance(playerName: String): Double {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null && currency != null) {
            return economyProvider.getBalance(character, currency).toDouble()
        } else {
            return 0.toDouble()
        }
    }

    override fun getBalance(playerName: String, world: String): Double {
        return getBalance(playerName)
    }

    override fun getName(): String {
        return "elysium-banks"
    }

    override fun isBankOwner(name: String, playerName: String): EconomyResponse {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(ElysiumBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null) {
            if (currency != null) {
                if (name == playerName) {
                    return EconomyResponse(0.0, bankProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
                } else {
                    return EconomyResponse(0.0, 0.0, FAILURE, "Bank is not owned by player.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No default currency is set.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player does not have a character.")
        }
    }

    override fun has(playerName: String, amount: Double): Boolean {
        return getBalance(playerName) >= amount
    }

    override fun has(playerName: String, worldName: String, amount: Double): Boolean {
        return has(playerName, amount)
    }

    override fun bankDeposit(name: String, amount: Double): EconomyResponse {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(ElysiumBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null) {
            if (currency != null) {
                bankProvider.setBalance(character, currency, bankProvider.getBalance(character, currency) + amount.toInt())
                return EconomyResponse(
                        amount.toInt().toDouble(),
                        bankProvider.getBalance(character, currency).toDouble(),
                        SUCCESS,
                        ""
                )
            } else {
                return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No default currency is set.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player does not have a character.")
        }
    }

    override fun bankWithdraw(name: String, amount: Double): EconomyResponse {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(ElysiumBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null) {
            if (currency != null) {
                bankProvider.setBalance(character, currency, bankProvider.getBalance(character, currency) - amount.toInt())
                return EconomyResponse(
                        amount.toInt().toDouble(),
                        bankProvider.getBalance(character, currency).toDouble(),
                        SUCCESS,
                        ""
                )
            } else {
                return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No default currency is set.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player does not have a character.")
        }
    }

    override fun deleteBank(name: String): EconomyResponse {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(ElysiumBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null) {
            if (currency != null) {
                bankProvider.setBalance(character, currency, 0)
                return EconomyResponse(
                        0.0,
                        bankProvider.getBalance(character, currency).toDouble(),
                        SUCCESS,
                        ""
                )
            } else {
                return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No default currency is set.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player does not have a character.")
        }
    }

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null) {
            if (currency != null) {
                if (economyProvider.getBalance(character, currency) + amount.toInt() <= 1720) {
                    economyProvider.setBalance(character, currency, amount.toInt())
                    return EconomyResponse(amount.toInt().toDouble(), economyProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
                } else {
                    return EconomyResponse(0.0, economyProvider.getBalance(character, currency).toDouble(), EconomyResponse.ResponseType.FAILURE, "Can not hold more than 1720 in wallet.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No default currency is set.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player does not have a character.")
        }
    }

    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return depositPlayer(playerName, amount)
    }

    override fun createBank(name: String, playerName: String): EconomyResponse {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(ElysiumBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null) {
            if (currency != null) {
                bankProvider.setBalance(character, currency, 0)
                return EconomyResponse(
                        0.0,
                        bankProvider.getBalance(character, currency).toDouble(),
                        SUCCESS,
                        ""
                )
            } else {
                return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No default currency is set.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player does not have a character.")
        }
    }

    override fun hasAccount(playerName: String): Boolean {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        return character != null
    }

    override fun hasAccount(playerName: String, worldName: String): Boolean {
        return hasAccount(playerName)
    }

    override fun isBankMember(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not implemented! Please use elysium-bank-lib.")
    }

    override fun createPlayerAccount(playerName: String): Boolean {
        return false
    }

    override fun createPlayerAccount(playerName: String, worldName: String): Boolean {
        return createPlayerAccount(playerName)
    }

    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null) {
            if (currency != null) {
                if (economyProvider.getBalance(character, currency) - amount.toInt() >= 0) {
                    economyProvider.setBalance(character, currency, amount.toInt())
                    return EconomyResponse(amount.toInt().toDouble(), economyProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
                } else {
                    return EconomyResponse(0.0, economyProvider.getBalance(character, currency).toDouble(), EconomyResponse.ResponseType.FAILURE, "Wallet does not have enough money.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No default currency is set.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player does not have a character.")
        }
    }

    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(playerName, amount)
    }

    override fun currencyNameSingular(): String {
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        return currencyProvider.defaultCurrency?.nameSingular?:""
    }

    override fun bankHas(name: String, amount: Double): EconomyResponse {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(ElysiumBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null) {
            if (currency != null) {
                if (bankProvider.getBalance(character, currency) >= amount.toInt()) {
                    return EconomyResponse(0.0, economyProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
                } else {
                    return EconomyResponse(0.0, economyProvider.getBalance(character, currency).toDouble(), FAILURE, "Bank does not have enough money.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No default currency is set.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player does not have a character.")
        }
    }

    override fun currencyNamePlural(): String {
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        return currencyProvider.defaultCurrency?.namePlural?:""
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun fractionalDigits(): Int {
        return 0
    }

    override fun bankBalance(name: String): EconomyResponse {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(ElysiumBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val player = playerProvider.getPlayer(bukkitOfflinePlayer)
        val character = characterProvider.getActiveCharacter(player)
        val currency = currencyProvider.defaultCurrency
        if (character != null) {
            if (currency != null) {
                return EconomyResponse(0.0, bankProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
            } else {
                return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "No default currency is set.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player does not have a character.")
        }
    }

    override fun format(amount: Double): String {
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class)
        val currency = currencyProvider.defaultCurrency
        if (currency != null) {
            return "${amount.toInt().toString()} ${(if (amount.toInt() == 1) currency.nameSingular else currency.namePlural)}"
        } else {
            return amount.toInt().toString()
        }
    }

    override fun hasBankSupport(): Boolean {
        return true
    }
}
