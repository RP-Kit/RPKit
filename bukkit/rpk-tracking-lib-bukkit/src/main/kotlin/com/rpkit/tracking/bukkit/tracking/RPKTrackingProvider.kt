package com.rpkit.tracking.bukkit.tracking

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider

interface RPKTrackingProvider: ServiceProvider {

    fun isTrackable(character: RPKCharacter): Boolean
    fun setTrackable(character: RPKCharacter, trackable: Boolean)

}