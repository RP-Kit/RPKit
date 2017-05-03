package com.rpkit.characters.bukkit.character.field

import com.rpkit.characters.bukkit.character.RPKCharacter


class ProfileField: HideableCharacterCardField {

    override val name = "profile"
    override fun get(character: RPKCharacter): String {
        if (isHidden(character)) return "[HIDDEN]"
        val profile = character.profile ?: return "unset"
        return profile.name
    }
    override fun isHidden(character: RPKCharacter): Boolean {
        return character.isProfileHidden
    }
    override fun setHidden(character: RPKCharacter, hidden: Boolean) {
        character.isProfileHidden = hidden
    }

}