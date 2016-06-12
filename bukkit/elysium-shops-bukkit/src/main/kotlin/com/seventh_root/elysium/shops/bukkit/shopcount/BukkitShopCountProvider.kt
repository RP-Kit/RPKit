package com.seventh_root.elysium.shops.bukkit.shopcount

import com.seventh_root.elysium.api.character.ElysiumCharacter
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.shops.bukkit.ElysiumShopsBukkit
import com.seventh_root.elysium.shops.bukkit.database.table.BukkitShopCountTable


class BukkitShopCountProvider(val plugin: ElysiumShopsBukkit): ServiceProvider {

    fun getShopCount(character: ElysiumCharacter): Int {
        return (plugin.core.database.getTable(BukkitShopCount::class.java) as? BukkitShopCountTable)?.get(character)?.count?:0
    }

    fun setShopCount(character: ElysiumCharacter, amount: Int) {
        val shopCount = (plugin.core.database.getTable(BukkitShopCount::class.java) as? BukkitShopCountTable)?.get(character)?:
                BukkitShopCount(0, character, 0)
        shopCount.count = amount
        plugin.core.database.getTable(BukkitShopCount::class.java)?.update(shopCount)
    }

}
