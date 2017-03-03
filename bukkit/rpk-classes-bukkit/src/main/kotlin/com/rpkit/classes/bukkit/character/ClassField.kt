package com.rpkit.classes.bukkit.character

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.CharacterCardField
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClassProvider


class ClassField(private val plugin: RPKClassesBukkit): CharacterCardField {

    override val name = "class"

    override fun get(character: RPKCharacter): String {
        return plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class).getClass(character)?.name?:"unset"
    }

}