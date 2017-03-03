package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider


interface RPKClassProvider: ServiceProvider {

    val classes: List<RPKClass>
    fun getClass(name: String): RPKClass?
    fun getClass(character: RPKCharacter): RPKClass?
    fun setClass(character: RPKCharacter, clazz: RPKClass)
    fun getLevel(character: RPKCharacter, clazz: RPKClass): Int
    fun setLevel(character: RPKCharacter, clazz: RPKClass, level: Int)
    fun getExperience(character: RPKCharacter, clazz: RPKClass): Int
    fun setExperience(character: RPKCharacter, clazz: RPKClass, experience: Int)

}