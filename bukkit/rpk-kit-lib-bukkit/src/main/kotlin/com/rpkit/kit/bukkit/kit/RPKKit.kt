package com.rpkit.kit.bukkit.kit

import com.rpkit.core.database.Entity
import org.bukkit.inventory.ItemStack


interface RPKKit: Entity {

    val name: String
    val items: List<ItemStack>

}