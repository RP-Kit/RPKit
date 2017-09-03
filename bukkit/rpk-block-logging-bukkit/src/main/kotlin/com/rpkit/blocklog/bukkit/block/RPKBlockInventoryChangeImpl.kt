package com.rpkit.blocklog.bukkit.block

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.inventory.ItemStack


class RPKBlockInventoryChangeImpl(
        override var id: Int = 0,
        override val blockHistory: RPKBlockHistory,
        override val time: Long,
        override val profile: RPKProfile?,
        override val minecraftProfile: RPKMinecraftProfile?,
        override val character: RPKCharacter?,
        override val from: Array<ItemStack>,
        override val to: Array<ItemStack>,
        override val reason: String
) : RPKBlockInventoryChange