package com.seventh_root.elysium.economy.bukkit.economy

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.economy.bukkit.economy.EconomyProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.database.table.BukkitWalletTable
import com.seventh_root.elysium.economy.bukkit.exception.NegativeBalanceException
import com.seventh_root.elysium.economy.bukkit.wallet.BukkitWallet


class BukkitEconomyProvider(val plugin: ElysiumEconomyBukkit) : EconomyProvider {

    override fun getBalance(character: ElysiumCharacter, currency: ElysiumCurrency): Int {
        return (plugin.core.database.getTable(BukkitWallet::class.java) as BukkitWalletTable).get(character, currency).balance
    }

    override fun setBalance(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        if (amount < 0) throw NegativeBalanceException()
        val wallet = (plugin.core.database.getTable(BukkitWallet::class.java) as BukkitWalletTable).get(character, currency)
        wallet.balance = amount
        plugin.core.database.getTable(BukkitWallet::class.java)!!.update(wallet)
    }

    override fun transfer(from: ElysiumCharacter, to: ElysiumCharacter, currency: ElysiumCurrency, amount: Int) {
        setBalance(from, currency, getBalance(from, currency) - amount)
        setBalance(to, currency, getBalance(to, currency) + amount)
    }

    override fun getRichestCharacters(currency: ElysiumCurrency, amount: Int): List<ElysiumCharacter> {
        return (plugin.core.database.getTable(BukkitWallet::class.java) as BukkitWalletTable).getTop(amount, currency)
    }

}