package com.rpkit.kit.bukkit.kit

import com.rpkit.core.service.ServiceProvider


interface RPKKitProvider: ServiceProvider {

    val kits: List<RPKKit>
    fun getKit(id: Int): RPKKit?
    fun getKit(name: String): RPKKit?
    fun addKit(kit: RPKKit)
    fun updateKit(kit: RPKKit)
    fun removeKit(kit: RPKKit)

}