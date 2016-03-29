package com.seventh_root.elysium.api.character

import com.seventh_root.elysium.api.player.ElysiumPlayer
import com.seventh_root.elysium.core.service.ServiceProvider

interface CharacterProvider<T : ElysiumCharacter> : ServiceProvider {

    fun getCharacter(id: Int): T?
    fun getActiveCharacter(player: ElysiumPlayer): T?
    fun setActiveCharacter(player: ElysiumPlayer, character: T?)
    fun getCharacters(player: ElysiumPlayer): Collection<T>
    fun addCharacter(character: T): Int
    fun removeCharacter(character: T)
    fun updateCharacter(character: T)

}
