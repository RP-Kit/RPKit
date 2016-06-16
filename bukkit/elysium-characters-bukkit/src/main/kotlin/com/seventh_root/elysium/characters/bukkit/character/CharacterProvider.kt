package com.seventh_root.elysium.characters.bukkit.character

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

interface CharacterProvider<T : ElysiumCharacter> : ServiceProvider {

    fun getCharacter(id: Int): T?
    fun getActiveCharacter(player: ElysiumPlayer): T?
    fun setActiveCharacter(player: ElysiumPlayer, character: T?)
    fun getCharacters(player: ElysiumPlayer): Collection<T>
    fun addCharacter(character: T): Int
    fun removeCharacter(character: T)
    fun updateCharacter(character: T)

}
