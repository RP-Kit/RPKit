package com.seventh_root.elysium.characters.bukkit.gender

import com.seventh_root.elysium.core.service.ServiceProvider

interface GenderProvider<T : Gender> : ServiceProvider {

    fun getGender(id: Int): T?
    fun getGender(name: String): T?
    val genders: Collection<T>
    fun addGender(gender: T)
    fun removeGender(gender: T)

}
