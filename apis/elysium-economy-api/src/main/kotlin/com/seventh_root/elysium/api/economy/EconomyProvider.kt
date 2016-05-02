package com.seventh_root.elysium.api.economy

import com.seventh_root.elysium.api.character.ElysiumCharacter
import com.seventh_root.elysium.core.service.ServiceProvider


interface EconomyProvider : ServiceProvider {

    fun getBalance(character: ElysiumCharacter, currency: ElysiumCurrency): Int
    fun setBalance(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Int)
    fun transfer(from: ElysiumCharacter, to: ElysiumCharacter, currency: ElysiumCurrency, amount: Int)
    fun getRichestCharacters(currency: ElysiumCurrency, amount: Int = 5): List<ElysiumCharacter>

}