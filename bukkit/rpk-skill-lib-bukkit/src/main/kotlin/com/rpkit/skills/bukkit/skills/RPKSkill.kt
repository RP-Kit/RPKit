package com.rpkit.skills.bukkit.skills

import com.rpkit.characters.bukkit.character.RPKCharacter


interface RPKSkill {

    val name: String
    val manaCost: Int
    val cooldown: Int

    fun use(character: RPKCharacter)

    fun canUse(character: RPKCharacter): Boolean

}

fun RPKCharacter.use(skill: RPKSkill) {
    skill.use(this)
}

fun RPKCharacter.canUse(skill: RPKSkill): Boolean {
    return skill.canUse(this)
}