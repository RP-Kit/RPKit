/*
 * Copyright 2020 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.travel.bukkit.warp

import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.database.table.RPKWarpTable
import com.rpkit.warp.bukkit.event.warp.RPKBukkitWarpCreateEvent
import com.rpkit.warp.bukkit.event.warp.RPKBukkitWarpDeleteEvent
import com.rpkit.warp.bukkit.event.warp.RPKBukkitWarpUpdateEvent
import com.rpkit.warp.bukkit.warp.RPKWarp
import com.rpkit.warp.bukkit.warp.RPKWarpProvider


class RPKWarpProviderImpl(private val plugin: RPKTravelBukkit): RPKWarpProvider {
    override val warps: List<RPKWarp>
        get() = plugin.core.database.getTable(RPKWarpTable::class).getAll()

    override fun getWarp(id: Int): RPKWarp? {
        return plugin.core.database.getTable(RPKWarpTable::class)[id]
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
        val event = RPKBukkitWarpDeleteEvent(warp)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKWarpTable::class).delete(event.warp)
    }
}