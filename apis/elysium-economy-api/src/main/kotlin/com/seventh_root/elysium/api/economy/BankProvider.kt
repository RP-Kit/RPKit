package com.seventh_root.elysium.api.economy

import com.seventh_root.elysium.api.character.ElysiumCharacter


interface BankProvider {

    fun getBalance(character: ElysiumCharacter, currency: ElysiumCurrency): Double
    fun setBalance(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Double)
    fun transfer(character: ElysiumCharacter, currency: ElysiumCurrency, amount: Double)

}