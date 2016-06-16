package com.seventh_root.elysium.shops.bukkit.shopcount

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.database.TableRow


data class BukkitShopCount(
        override var id: Int,
        val character: ElysiumCharacter,
        var count: Int
) : TableRow