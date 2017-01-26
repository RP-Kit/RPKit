package com.rpkit.essentials.bukkit.tracking

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.table.RPKTrackingEnabledTable
import com.rpkit.tracking.bukkit.tracking.RPKTrackingProvider


class RPKTrackingProviderImpl(private val plugin: RPKEssentialsBukkit): RPKTrackingProvider {

    override fun isTrackable(character: RPKCharacter): Boolean {
        return plugin.core.database.getTable(RPKTrackingEnabledTable::class).get(character)?.enabled?:true
    }

    override fun setTrackable(character: RPKCharacter, trackable: Boolean) {
        val trackingEnabledTable = plugin.core.database.getTable(RPKTrackingEnabledTable::class)
        var trackingEnabled = trackingEnabledTable.get(character)
        if (trackingEnabled != null) {
            trackingEnabled.enabled = trackable
            trackingEnabledTable.update(trackingEnabled)
        } else {
            trackingEnabled = RPKTrackingEnabled(character = character, enabled = trackable)
            trackingEnabledTable.insert(trackingEnabled)
        }
    }

}