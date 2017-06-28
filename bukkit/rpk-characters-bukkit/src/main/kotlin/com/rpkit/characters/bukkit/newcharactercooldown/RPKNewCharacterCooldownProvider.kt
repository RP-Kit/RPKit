package com.rpkit.characters.bukkit.newcharactercooldown

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.table.RPKNewCharacterCooldownTable
import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKNewCharacterCooldownProvider(private val plugin: RPKCharactersBukkit): ServiceProvider {

    fun getNewCharacterCooldown(profile: RPKProfile): Long {
        val newCharacterCooldown = plugin.core.database.getTable(RPKNewCharacterCooldownTable::class).get(profile) ?: return 0
        return Math.max(newCharacterCooldown.cooldownTimestamp - System.currentTimeMillis(), 0)
    }

    fun setNewCharacterCooldown(profile: RPKProfile, cooldown: Long) {
        val newCharacterCooldownTable = plugin.core.database.getTable(RPKNewCharacterCooldownTable::class)
        var newCharacterCooldown = newCharacterCooldownTable.get(profile)
        if (newCharacterCooldown == null) {
            newCharacterCooldown = RPKNewCharacterCooldown(profile = profile, cooldownTimestamp = System.currentTimeMillis() + cooldown)
            newCharacterCooldownTable.insert(newCharacterCooldown)
        } else {
            newCharacterCooldown.cooldownTimestamp = System.currentTimeMillis() + cooldown
            newCharacterCooldownTable.update(newCharacterCooldown)
        }
    }

}