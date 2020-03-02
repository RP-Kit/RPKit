package com.rpkit.statbuilds.bukkit.statbuild

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttribute

interface RPKStatBuildProvider: ServiceProvider {

    fun getStatPoints(character: RPKCharacter, statAttribute: RPKStatAttribute): Int
    fun setStatPoints(character: RPKCharacter, statAttribute: RPKStatAttribute, amount: Int)
    fun getMaxStatPoints(character: RPKCharacter, statAttribute: RPKStatAttribute): Int
    fun getTotalStatPoints(character: RPKCharacter): Int
    fun getUnassignedStatPoints(character: RPKCharacter): Int
    fun getAssignedStatPoints(character: RPKCharacter): Int

}