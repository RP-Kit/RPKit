package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider


interface RPKClassProvider: ServiceProvider {

    val classes: List<RPKClass>
    fun getClass(name: String): RPKClass?
    fun getClass(character: RPKCharacter): RPKClass?
    fun setClass(character: RPKCharacter, `class`: RPKClass)
    fun getLevel(character: RPKCharacter, `class`: RPKClass): Int
    fun setLevel(character: RPKCharacter, `class`: RPKClass, level: Int)
    fun getExperience(character: RPKCharacter, `class`: RPKClass): Int
    fun setExperience(character: RPKCharacter, `class`: RPKClass, experience: Int)

}