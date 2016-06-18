package com.seventh_root.elysium.banks.bukkit.bank

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.database.TableRow
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency


class ElysiumBank(
        override var id: Int = 0,
        val character: ElysiumCharacter,
        val currency: ElysiumCurrency,
        var balance: Int
): TableRow