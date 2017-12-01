package com.rpkit.blocklog.bukkit.block

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.inventory.ItemStack


interface RPKBlockInventoryChange: Entity {

    val blockHistory: RPKBlockHistory
    val time: Long
    val profile: RPKProfile?
    val minecraftProfile: RPKMinecraftProfile?
    val character: RPKCharacter?
    val from: Array<ItemStack>
    val to: Array<ItemStack>
    val reason: String

}