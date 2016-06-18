package com.seventh_root.elysium.shops.bukkit.shopcount

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.shops.bukkit.ElysiumShopsBukkit
import com.seventh_root.elysium.shops.bukkit.database.table.ElysiumShopCountTable


class ElysiumShopCountProvider(val plugin: ElysiumShopsBukkit): ServiceProvider {

    fun getShopCount(character: ElysiumCharacter): Int {
        return (plugin.core.database.getTable(ElysiumShopCount::class.java) as? ElysiumShopCountTable)?.get(character)?.count?:0
    }

    fun setShopCount(character: ElysiumCharacter, amount: Int) {
        val shopCount = (plugin.core.database.getTable(ElysiumShopCount::class.java) as? ElysiumShopCountTable)?.get(character)?:
                ElysiumShopCount(0, character, 0)
        shopCount.count = amount
        plugin.core.database.getTable(ElysiumShopCount::class.java)?.update(shopCount)
    }

}
