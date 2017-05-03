package com.rpkit.permissions.bukkit.group

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKProfileGroup(
        override var id: Int = 0,
        val profile: RPKProfile,
        val group: RPKGroup
): Entity