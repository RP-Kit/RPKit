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

package com.rpkit.banks.bukkit.vault

import com.rpkit.banks.bukkit.RPKBankLibBukkit
import com.rpkit.banks.bukkit.bank.RPKBankService
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType.FAILURE
import net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS

/**
 * A Vault [Economy] implementation for banks plugins.
 * Registered at higher priority than the rpk-economy implementation
 */
class RPKBanksVaultEconomy(private val plugin: RPKBankLibBukkit) : AbstractEconomy() {

    override fun getBanks(): MutableList<String>? {
        throw UnsupportedOperationException("rpk-bank-lib does not support listing of banks.")
    }

    override fun getBalance(playerName: String): Double {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (getBalance)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return 0.0
        val characterService = Services[RPKCharacterService::class.java] ?: return 0.0
        val economyService = Services[RPKEconomyService::class.java] ?: return 0.0
        val currencyService = Services[RPKCurrencyService::class.java] ?: return 0.0
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        return if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            if (character != null && currency != null) {
                economyService.getBalance(character, currency).join().toDouble()
            } else {
                0.0
            }
        } else {
            0.0
        }
    }

    override fun getBalance(playerName: String, world: String): Double {
        return getBalance(playerName)
    }

    override fun getName(): String {
        return "rpk-banks"
    }

    override fun isBankOwner(name: String, playerName: String): EconomyResponse {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (isBankOwner)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val currencyService = Services[RPKCurrencyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bankService = Services[RPKBankService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no bank service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            return if (character != null) {
                if (currency != null) {
                    if (name == playerName) {
                        EconomyResponse(0.0, bankService.getBalance(character, currency).join().toDouble(), SUCCESS, "")
                    } else {
                        EconomyResponse(0.0, 0.0, FAILURE, "Bank is not owned by player.")
                    }
                } else {
                    EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
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
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (bankDeposit)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val currencyService = Services[RPKCurrencyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bankService = Services[RPKBankService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no bank service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            if (character != null) {
                return if (currency != null) {
                    val balance = bankService.getBalance(character, currency).join()
                    bankService.setBalance(character, currency, balance + amount.toInt()).join()
                    EconomyResponse(
                            amount.toInt().toDouble(),
                            bankService.getBalance(character, currency).join().toDouble(),
                            SUCCESS,
                            ""
                    )
                } else {
                    EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun bankWithdraw(name: String, amount: Double): EconomyResponse {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (bankWithdraw)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val currencyService = Services[RPKCurrencyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bankService = Services[RPKBankService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no bank service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            if (character != null) {
                return if (currency != null) {
                    val balance = bankService.getBalance(character, currency).join()
                    bankService.setBalance(character, currency, balance - amount.toInt()).join()
                    EconomyResponse(
                            amount.toInt().toDouble(),
                            bankService.getBalance(character, currency).join().toDouble(),
                            SUCCESS,
                            ""
                    )
                } else {
                    EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have Minecraft profile.")
        }
    }

    override fun deleteBank(name: String): EconomyResponse {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (deleteBank)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val currencyService = Services[RPKCurrencyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bankService = Services[RPKBankService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no bank service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            return if (character != null) {
                if (currency != null) {
                    bankService.setBalance(character, currency, 0).join()
                    EconomyResponse(
                            0.0,
                            bankService.getBalance(character, currency).join().toDouble(),
                            SUCCESS,
                            ""
                    )
                } else {
                    EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (depositPlayer)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val economyService = Services[RPKEconomyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no economy service available."
                )
        val currencyService = Services[RPKCurrencyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            return if (character != null) {
                if (currency != null) {
                    val balance = economyService.getBalance(character, currency).join()
                    if (balance + amount.toInt() <= 1720) {
                        economyService.setBalance(character, currency, balance + amount.toInt()).join()
                        EconomyResponse(amount.toInt().toDouble(), economyService.getBalance(character, currency).join().toDouble(), SUCCESS, "")
                    } else {
                        EconomyResponse(0.0, economyService.getBalance(character, currency).join().toDouble(), FAILURE, "Can not hold more than 1720 in wallet.")
                    }
                } else {
                    EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return depositPlayer(playerName, amount)
    }

    override fun createBank(name: String, playerName: String): EconomyResponse {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (createBank)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val currencyService = Services[RPKCurrencyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bankService = Services[RPKBankService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no bank service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            return if (character != null) {
                if (currency != null) {
                    bankService.setBalance(character, currency, 0).join()
                    EconomyResponse(
                            0.0,
                            bankService.getBalance(character, currency).join().toDouble(),
                            SUCCESS,
                            ""
                    )
                } else {
                    EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun hasAccount(playerName: String): Boolean {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (hasAccount)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
        val characterService = Services[RPKCharacterService::class.java] ?: return false
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join() ?: return false
        val character = characterService.getActiveCharacter(minecraftProfile).join()
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
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (withdrawPlayer)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val economyService = Services[RPKEconomyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no economy service available."
                )
        val currencyService = Services[RPKCurrencyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            return if (character != null) {
                if (currency != null) {
                    val balance = economyService.getBalance(character, currency).join()
                    if (balance - amount.toInt() >= 0) {
                        economyService.setBalance(character, currency, balance - amount.toInt()).join()
                        EconomyResponse(amount.toInt().toDouble(), economyService.getBalance(character, currency).join().toDouble(), SUCCESS, "")
                    } else {
                        EconomyResponse(0.0, economyService.getBalance(character, currency).join().toDouble(), FAILURE, "Wallet does not have enough money.")
                    }
                } else {
                    EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(playerName, amount)
    }

    override fun currencyNameSingular(): String {
        val currencyService = Services[RPKCurrencyService::class.java]
        return currencyService?.defaultCurrency?.nameSingular ?: ""
    }

    override fun bankHas(name: String, amount: Double): EconomyResponse {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (bankHas)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val economyService = Services[RPKEconomyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no economy service available."
                )
        val currencyService = Services[RPKCurrencyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bankService = Services[RPKBankService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no bank service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            return if (character != null) {
                if (currency != null) {
                    if (bankService.getBalance(character, currency).join() >= amount.toInt()) {
                        EconomyResponse(0.0, economyService.getBalance(character, currency).join().toDouble(), SUCCESS, "")
                    } else {
                        EconomyResponse(0.0, economyService.getBalance(character, currency).join().toDouble(), FAILURE, "Bank does not have enough money.")
                    }
                } else {
                    EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            return EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun currencyNamePlural(): String {
        val currencyService = Services[RPKCurrencyService::class.java]
        return currencyService?.defaultCurrency?.namePlural ?: ""
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun fractionalDigits(): Int {
        return 0
    }

    override fun bankBalance(name: String): EconomyResponse {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (bankBalance)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val currencyService = Services[RPKCurrencyService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bankService = Services[RPKBankService::class.java]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no bank service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(name)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer).join()
        return if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile).join()
            val currency = currencyService.defaultCurrency
            if (character != null) {
                if (currency != null) {
                    EconomyResponse(0.0, bankService.getBalance(character, currency).join().toDouble(), SUCCESS, "")
                } else {
                    EconomyResponse(0.0, 0.0, FAILURE, "No default currency is set.")
                }
            } else {
                EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a character.")
            }
        } else {
            EconomyResponse(0.0, 0.0, FAILURE, "Player does not have a Minecraft profile.")
        }
    }

    override fun format(amount: Double): String {
        val currencyService = Services[RPKCurrencyService::class.java]
        val currency = currencyService?.defaultCurrency
        return if (currency != null) {
            "${amount.toInt()} ${(if (amount.toInt() == 1) currency.nameSingular else currency.namePlural)}"
        } else {
            amount.toInt().toString()
        }
    }

    override fun hasBankSupport(): Boolean {
        return true
    }
}
