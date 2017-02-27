package com.rpkit.skills.bukkit.skills

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity


class RPKSkillCooldown(
        override var id: Int = 0,
        val character: RPKCharacter,
        val skill: RPKSkill,
        var cooldownTimestamp: Long
) : Entity