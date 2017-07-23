package com.rpkit.blocklog.bukkit.block

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.Material


class RPKBlockChangeImpl(
        override var id: Int,
        override val blockHistory: RPKBlockHistory,
        override val time: Long,
        override val profile: RPKProfile?,
        override val minecraftProfile: RPKMinecraftProfile?,
        override val character: RPKCharacter?,
        override val from: Material,
        override val fromData: Byte,
        override val to: Material,
        override val toData: Byte,
        override val reason: String
) : RPKBlockChange