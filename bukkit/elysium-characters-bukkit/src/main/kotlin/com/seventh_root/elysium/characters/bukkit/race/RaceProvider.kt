package com.seventh_root.elysium.characters.bukkit.race

import com.seventh_root.elysium.core.service.ServiceProvider

interface RaceProvider<T : Race> : ServiceProvider {

    fun getRace(id: Int): Race?
    fun getRace(name: String): Race?
    val races: Collection<T>
    fun addRace(race: T)
    fun removeRace(race: T)

}
