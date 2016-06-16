package com.seventh_root.elysium.banks.bukkit.bank

import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter


interface BankProvider {

    fun getBalance(character: ElysiumCharacter, currency: ElysiumCurrency): Double
    fun setBalance(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Double)
    fun transfer(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Double)

}