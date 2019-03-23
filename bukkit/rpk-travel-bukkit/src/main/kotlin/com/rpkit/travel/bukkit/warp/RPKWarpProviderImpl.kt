package com.rpkit.travel.bukkit.warp

import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.database.table.RPKWarpTable
import com.rpkit.warp.bukkit.event.warp.RPKBukkitWarpCreateEvent
import com.rpkit.warp.bukkit.event.warp.RPKBukkitWarpUpdateEvent
import com.rpkit.warp.bukkit.warp.RPKWarp
import com.rpkit.warp.bukkit.warp.RPKWarpProvider


class RPKWarpProviderImpl(private val plugin: RPKTravelBukkit): RPKWarpProvider {
    override val warps: List<RPKWarp>
        get() = plugin.core.database.getTable(RPKWarpTable::class).getAll()

    override fun getWarp(id: Int): RPKWarp? {
        return plugin.core.database.getTable(RPKWarpTable::class).get(id)
    }

    override fun getWarp(name: String): RPKWarp? {
        return plugin.core.database.getTable(RPKWarpTable::class).get(name)
    }

    override fun addWarp(warp: RPKWarp) {
        val event = RPKBukkitWarpCreateEvent(warp)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKWarpTable::class).insert(event.warp)
    }

    override fun updateWarp(warp: RPKWarp) {
        val event = RPKBukkitWarpUpdateEvent(warp)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKWarpTable::class).update(event.warp)
    }

    override fun removeWarp(warp: RPKWarp) {
        val event = RPKBukkitWarpUpdateEvent(warp)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKWarpTable::class).delete(event.warp)
    }
}