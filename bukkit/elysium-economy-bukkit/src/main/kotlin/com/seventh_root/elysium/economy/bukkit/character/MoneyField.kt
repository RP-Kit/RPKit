package com.seventh_root.elysium.economy.bukkit.character

import com.seventh_root.elysium.characters.bukkit.character.field.CharacterCardField
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.BukkitCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.BukkitEconomyProvider

class MoneyField(val plugin: ElysiumEconomyBukkit): CharacterCardField {

    override val name = "money"
    override fun get(character: ElysiumCharacter): String {
        val economyProvider = plugin.core.serviceManager.getServiceProvider(BukkitEconomyProvider::class.java)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(BukkitCurrencyProvider::class.java)
        return currencyProvider.currencies
                .map { currency ->
                    val balance = economyProvider.getBalance(character, currency)
                    "${balance.toString()} ${if (balance == 1) currency.nameSingular else currency.namePlural}"
                }
                .joinToString(", ")
    }

}
