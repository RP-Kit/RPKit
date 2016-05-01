package com.seventh_root.elysium.economy.bukkit.wallet

import com.seventh_root.elysium.api.character.ElysiumCharacter
import com.seventh_root.elysium.api.economy.ElysiumCurrency
import com.seventh_root.elysium.core.database.TableRow


data class BukkitWallet(
        override var id: Int = 0,
        val character: ElysiumCharacter,
        val currency: ElysiumCurrency,
        var balance: Int
) : TableRow