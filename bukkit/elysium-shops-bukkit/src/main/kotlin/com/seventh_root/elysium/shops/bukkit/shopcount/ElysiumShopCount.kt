package com.seventh_root.elysium.shops.bukkit.shopcount

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.database.TableRow


data class ElysiumShopCount(
        override var id: Int = 0,
        val character: ElysiumCharacter,
        var count: Int
): TableRow