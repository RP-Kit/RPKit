package com.seventh_root.elysium.economy.bukkit.economy

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency


interface EconomyProvider : ServiceProvider {

    fun getBalance(character: ElysiumCharacter, currency: ElysiumCurrency): Int
    fun setBalance(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int)
    fun transfer(from: ElysiumCharacter, to: ElysiumCharacter, currency: ElysiumCurrency, amount: Int)
    fun getRichestCharacters(currency: ElysiumCurrency, amount: Int = 5): List<ElysiumCharacter>

}