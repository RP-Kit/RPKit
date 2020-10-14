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

package com.rpkit.economy.bukkit.vault

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.RPKEconomyLibBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType.*

/**
 * A Vault [Economy] implementation for economy plugins.
 */
class RPKEconomyVaultEconomy(private val plugin: RPKEconomyLibBukkit) : AbstractEconomy() {

    override fun getBanks(): MutableList<String> {
        return mutableListOf()
    }

    override fun getBalance(playerName: String): Double {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return 0.0
        val characterService = Services[RPKCharacterService::class] ?: return 0.0
        val economyService = Services[RPKEconomyService::class] ?: return 0.0
        val currencyService = Services[RPKCurrencyService::class] ?: return 0.0
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer)
        return if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile)
            val currency = currencyService.defaultCurrency
            if (character != null && currency != null) {
                economyService.getBalance(character, currency).toDouble()
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
        return "rpk-economy"
    }

    override fun isBankOwner(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, NOT_IMPLEMENTED, "Banks not implemented! Please use rpk-bank-lib.")
    }

    override fun has(playerName: String, amount: Double): Boolean {
        return getBalance(playerName) >= amount
    }

    override fun has(playerName: String, worldName: String, amount: Double): Boolean {
        return has(playerName, amount)
    }

    override fun bankDeposit(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, NOT_IMPLEMENTED, "Banks not implemented! Please use rpk-bank-lib.")
    }

    override fun bankWithdraw(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, NOT_IMPLEMENTED, "Banks not implemented! Please use rpk-bank-lib.")
    }

    override fun deleteBank(name: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, NOT_IMPLEMENTED, "Banks not implemented! Please use rpk-bank-lib.")
    }

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val economyService = Services[RPKEconomyService::class]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no economy service available."
                )
        val currencyService = Services[RPKCurrencyService::class]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service available."
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile)
            val currency = currencyService.defaultCurrency
            return if (character != null) {
                if (currency != null) {
                    val balance = economyService.getBalance(character, currency)
                    if (balance + amount.toInt() <= 1720) {
                        economyService.setBalance(character, currency, balance + amount.toInt())
                        EconomyResponse(amount.toInt().toDouble(), economyService.getBalance(character, currency).toDouble(), SUCCESS, "")
                    } else {
                        EconomyResponse(0.0, economyService.getBalance(character, currency).toDouble(), FAILURE, "Can not hold more than 1720 in wallet.")
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

    override fun createBank(name: String, player: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, NOT_IMPLEMENTED, "Banks not implemented! Please use rpk-bank-lib.")
    }

    override fun hasAccount(playerName: String): Boolean {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return false
        val characterService = Services[RPKCharacterService::class] ?: return false
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer) ?: return false
        val character = characterService.getActiveCharacter(minecraftProfile)
        return character != null
    }

    override fun hasAccount(playerName: String, worldName: String): Boolean {
        return hasAccount(playerName)
    }

    override fun isBankMember(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, NOT_IMPLEMENTED, "Banks not implemented! Please use rpk-bank-lib.")
    }

    override fun createPlayerAccount(playerName: String): Boolean {
        return false
    }

    override fun createPlayerAccount(playerName: String, worldName: String): Boolean {
        return createPlayerAccount(playerName)
    }

    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no Minecraft profile service available."
                )
        val characterService = Services[RPKCharacterService::class]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no character service available."
                )
        val economyService = Services[RPKEconomyService::class]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no economy service"
                )
        val currencyService = Services[RPKCurrencyService::class]
                ?: return EconomyResponse(
                        0.0,
                        0.0,
                        FAILURE,
                        "There is no currency service"
                )
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val character = characterService.getActiveCharacter(minecraftProfile)
            val currency = currencyService.defaultCurrency
            return if (character != null) {
                if (currency != null) {
                    val balance = economyService.getBalance(character, currency)
                    if (balance - amount.toInt() >= 0) {
                        economyService.setBalance(character, currency, balance - amount.toInt())
                        EconomyResponse(amount.toInt().toDouble(), economyService.getBalance(character, currency).toDouble(), SUCCESS, "")
                    } else {
                        EconomyResponse(0.0, economyService.getBalance(character, currency).toDouble(), FAILURE, "Wallet does not have enough money.")
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
        val currencyService = Services[RPKCurrencyService::class]
        return currencyService?.defaultCurrency?.nameSingular ?: ""
    }

    override fun bankHas(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, NOT_IMPLEMENTED, "Banks not implemented! Please use rpk-bank-lib.")
    }

    override fun currencyNamePlural(): String {
        val currencyService = Services[RPKCurrencyService::class]
        return currencyService?.defaultCurrency?.namePlural ?: ""
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun fractionalDigits(): Int {
        return 0
    }

    override fun bankBalance(name: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, NOT_IMPLEMENTED, "Banks not implemented! Please use rpk-bank-lib.")
    }

    override fun format(amount: Double): String {
        val currencyService = Services[RPKCurrencyService::class]
        val currency = currencyService?.defaultCurrency
        return if (currency != null) {
            "${amount.toInt()} ${(if (amount.toInt() == 1) currency.nameSingular else currency.namePlural)}"
        } else {
            amount.toInt().toString()
        }
    }

    override fun hasBankSupport(): Boolean {
        return false
    }


}
