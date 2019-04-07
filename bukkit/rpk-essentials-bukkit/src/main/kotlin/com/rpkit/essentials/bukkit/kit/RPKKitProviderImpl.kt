package com.rpkit.essentials.bukkit.kit

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.kit.bukkit.kit.RPKKit
import com.rpkit.kit.bukkit.kit.RPKKitProvider


class RPKKitProviderImpl(private val plugin: RPKEssentialsBukkit): RPKKitProvider {

    override val kits: MutableList<RPKKit> = plugin.config.getList("kits") as MutableList<RPKKit>

    override fun getKit(id: Int): RPKKit? {
        return kits.firstOrNull { it.id == id }
    }

    override fun getKit(name: String): RPKKit? {
        return kits.firstOrNull { it.name == name }
    }

    override fun addKit(kit: RPKKit) {
        kits.add(kit)
        plugin.config.set("kits", kits)
        plugin.saveConfig()
    }

    override fun updateKit(kit: RPKKit) {
        removeKit(kit)
        addKit(kit)
    }

    override fun removeKit(kit: RPKKit) {
        kits.remove(getKit(kit.id))
        plugin.config.set("kits", kits)
        plugin.saveConfig()
    }
}