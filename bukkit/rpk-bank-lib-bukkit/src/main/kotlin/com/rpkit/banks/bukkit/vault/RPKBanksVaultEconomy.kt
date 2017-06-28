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

package com.rpkit.banks.bukkit.vault

import com.rpkit.banks.bukkit.RPKBankLibBukkit
import com.rpkit.banks.bukkit.bank.RPKBankProvider
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType.FAILURE
import net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS
/**
 * A Vault [Economy] implementation for banks plugins.
 * Registered at higher priority than the rpk-economy implementation
 */
class RPKBanksVaultEconomy(private val plugin: RPKBankLibBukkit): AbstractEconomy() {

    override fun getBanks(): MutableList<String>? {
        throw UnsupportedOperationException("rpk-bank-lib does not support listing of banks.")
    }

    override fun getBalance(playerName: String): Double {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
            val currency = currencyProvider.defaultCurrency
            if (character != null && currency != null) {
                return economyProvider.getBalance(character, currency).toDouble()
            } else {
                return 0.0
            }
        } else {
            return 0.0
        }
    }

    override fun getBalance(playerName: String, world: String): Double {
        return getBalance(playerName)
    }

    override fun getName(): String {
        return "rpk-banks"
    }

    override fun isBankOwner(name: String, playerName: String): EconomyResponse {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
            val currency = currencyProvider.defaultCurrency
            if (character != null) {
                if (currency != null) {
                    if (name == playerName) {
                        return EconomyResponse(0.0, bankProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
                    } else {
                        return EconomyResponse(0.0, 0.0, FAILURE, "Bank is not owned by player.")
                    }
                } else {
                    return EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun has(playerName: String, amount: Double): Boolean {
        return getBalance(playerName) >= amount
    }

    override fun has(playerName: String, worldName: String, amount: Double): Boolean {
        return has(playerName, amount)
    }

    override fun bankDeposit(name: String, amount: Double): EconomyResponse {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
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
                    return EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun bankWithdraw(name: String, amount: Double): EconomyResponse {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
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
                    return EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have Minecraft profile.")
        }
    }

    override fun deleteBank(name: String): EconomyResponse {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
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
                    return EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
            val currency = currencyProvider.defaultCurrency
            if (character != null) {
                if (currency != null) {
                    if (economyProvider.getBalance(character, currency) + amount.toInt() <= 1720) {
                        economyProvider.setBalance(character, currency, amount.toInt())
                        return EconomyResponse(amount.toInt().toDouble(), economyProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
                    } else {
                        return EconomyResponse(0.0, economyProvider.getBalance(character, currency).toDouble(), FAILURE, "Can not hold more than 1720 in wallet.")
                    }
                } else {
                    return EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return depositPlayer(playerName, amount)
    }

    override fun createBank(name: String, playerName: String): EconomyResponse {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
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
                    return EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun hasAccount(playerName: String): Boolean {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer) ?: return false
        val character = characterProvider.getActiveCharacter(minecraftProfile)
        return character != null
    }

    override fun hasAccount(playerName: String, worldName: String): Boolean {
        return hasAccount(playerName)
    }

    override fun isBankMember(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks may not have members.")
    }

    override fun createPlayerAccount(playerName: String): Boolean {
        return false
    }

    override fun createPlayerAccount(playerName: String, worldName: String): Boolean {
        return createPlayerAccount(playerName)
    }

    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
            val currency = currencyProvider.defaultCurrency
            if (character != null) {
                if (currency != null) {
                    if (economyProvider.getBalance(character, currency) - amount.toInt() >= 0) {
                        economyProvider.setBalance(character, currency, amount.toInt())
                        return EconomyResponse(amount.toInt().toDouble(), economyProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
                    } else {
                        return EconomyResponse(0.0, economyProvider.getBalance(character, currency).toDouble(), FAILURE, "Wallet does not have enough money.")
                    }
                } else {
                    return EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(playerName, amount)
    }

    override fun currencyNameSingular(): String {
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        return currencyProvider.defaultCurrency?.nameSingular?:""
    }

    override fun bankHas(name: String, amount: Double): EconomyResponse {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
            val currency = currencyProvider.defaultCurrency
            if (character != null) {
                if (currency != null) {
                    if (bankProvider.getBalance(character, currency) >= amount.toInt()) {
                        return EconomyResponse(0.0, economyProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
                    } else {
                        return EconomyResponse(0.0, economyProvider.getBalance(character, currency).toDouble(), FAILURE, "Bank does not have enough money.")
                    }
                } else {
                    return EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun currencyNamePlural(): String {
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        return currencyProvider.defaultCurrency?.namePlural?:""
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun fractionalDigits(): Int {
        return 0
    }

    override fun bankBalance(name: String): EconomyResponse {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterProvider.getActiveCharacter(minecraftProfile)
            val currency = currencyProvider.defaultCurrency
            if (character != null) {
                if (currency != null) {
                    return EconomyResponse(0.0, bankProvider.getBalance(character, currency).toDouble(), SUCCESS, "")
                } else {
                    return EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun format(amount: Double): String {
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val currency = currencyProvider.defaultCurrency
        if (currency != null) {
            return "${amount.toInt()} ${(if (amount.toInt() == 1) currency.nameSingular else currency.namePlural)}"
        } else {
            return amount.toInt().toString()
        }
    }

    override fun hasBankSupport(): Boolean {
        return true
    }
}
