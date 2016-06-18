package com.seventh_root.elysium.economy.bukkit.wallet

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.core.database.TableRow


data class ElysiumWallet(
        override var id: Int = 0,
        val character: ElysiumCharacter,
        val currency: ElysiumCurrency,
        var balance: Int
): TableRow