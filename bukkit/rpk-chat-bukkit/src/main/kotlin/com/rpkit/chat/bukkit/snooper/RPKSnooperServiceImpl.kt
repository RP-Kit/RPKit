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

package com.rpkit.chat.bukkit.snooper

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.table.RPKSnooperTable
import com.rpkit.chat.bukkit.event.snooper.RPKBukkitSnoopingBeginEvent
import com.rpkit.chat.bukkit.event.snooper.RPKBukkitSnoopingEndEvent
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.util.concurrent.CompletableFuture

/**
 * Snooper service implementation.
 */
class RPKSnooperServiceImpl(override val plugin: RPKChatBukkit) : RPKSnooperService {

    override val snoopers: CompletableFuture<List<RPKMinecraftProfile>>
        get() = plugin.database.getTable(RPKSnooperTable::class.java).getAll().thenApply { it.map(RPKSnooper::minecraftProfile) }

    override fun addSnooper(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void> {
        return snoopers.thenAccept { snoopers ->
            if (!snoopers.contains(minecraftProfile)) {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    val event = RPKBukkitSnoopingBeginEvent(minecraftProfile)
                    plugin.server.pluginManager.callEvent(event)
                    if (event.isCancelled) return@Runnable
                    plugin.database.getTable(RPKSnooperTable::class.java).insert(RPKSnooper(minecraftProfile = minecraftProfile))
                })
            }
        }
    }

    override fun removeSnooper(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void> {
        val snooperTable = plugin.database.getTable(RPKSnooperTable::class.java)
        return snooperTable.get(minecraftProfile).thenAccept { snooper ->
            if (snooper != null) {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    val event = RPKBukkitSnoopingEndEvent(minecraftProfile)
                    plugin.server.pluginManager.callEvent(event)
                    if (event.isCancelled) return@Runnable
                    snooperTable.delete(snooper)
                })
            }
        }
    }

    override fun isSnooping(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Boolean> {
        val snooperTable = plugin.database.getTable(RPKSnooperTable::class.java)
        return snooperTable.get(minecraftProfile).thenApply { it != null }
    }

}