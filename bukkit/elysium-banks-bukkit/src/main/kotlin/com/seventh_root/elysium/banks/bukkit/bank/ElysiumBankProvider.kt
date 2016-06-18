package com.seventh_root.elysium.banks.bukkit.bank

import com.seventh_root.elysium.banks.bukkit.ElysiumBanksBukkit
import com.seventh_root.elysium.banks.bukkit.database.table.ElysiumBankTable
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.economy.bukkit.exception.NegativeBalanceException


class ElysiumBankProvider(private val plugin: ElysiumBanksBukkit): ServiceProvider {

    fun getBalance(character: ElysiumCharacter, currency: ElysiumCurrency): Int {
        return (plugin.core.database.getTable(ElysiumBank::class.java) as ElysiumBankTable).get(character, currency).balance
    }

    fun setBalance(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        if (amount < 0) throw NegativeBalanceException()
        val bank = (plugin.core.database.getTable(ElysiumBank::class.java) as ElysiumBankTable).get(character, currency)
        bank.balance = amount
        plugin.core.database.getTable(ElysiumBank::class.java)!!.update(bank)
    }

    fun deposit(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class.java)
        if (economyProvider.getBalance(character, currency) >= amount) {
            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) - amount)
            setBalance(character, currency, getBalance(character, currency) + amount)
        }
    }

    fun withdraw(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class.java)
        if (getBalance(character, currency) >= amount) {
            economyProvider.setBalance(character, currency, economyProvider.getBalance(character, currency) + amount)
            setBalance(character, currency, getBalance(character, currency) + amount)
        }
    }

}