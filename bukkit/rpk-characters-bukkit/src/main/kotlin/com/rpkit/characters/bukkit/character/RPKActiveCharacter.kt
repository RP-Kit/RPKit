package com.rpkit.characters.bukkit.character

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile


class RPKActiveCharacter(
        override var id: Int,
        val minecraftProfile: RPKMinecraftProfile,
        val character: RPKCharacter
) : Entity