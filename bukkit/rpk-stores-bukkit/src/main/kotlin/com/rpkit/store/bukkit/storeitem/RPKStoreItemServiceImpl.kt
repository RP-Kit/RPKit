/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.store.bukkit.storeitem

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.database.table.RPKConsumableStoreItemTable
import com.rpkit.store.bukkit.database.table.RPKPermanentStoreItemTable
import com.rpkit.store.bukkit.database.table.RPKStoreItemTable
import com.rpkit.store.bukkit.database.table.RPKTimedStoreItemTable
import com.rpkit.store.bukkit.event.storeitem.RPKBukkitStoreItemCreateEvent
import com.rpkit.store.bukkit.event.storeitem.RPKBukkitStoreItemDeleteEvent
import com.rpkit.store.bukkit.event.storeitem.RPKBukkitStoreItemUpdateEvent
import java.time.Duration


class RPKStoreItemServiceImpl(override val plugin: RPKStoresBukkit) : RPKStoreItemService {

    override fun getStoreItem(plugin: RPKBukkitPlugin, identifier: String): RPKStoreItem? {
        return this.plugin.database.getTable(RPKStoreItemTable::class.java).get(plugin, identifier)
    }

    override fun getStoreItem(id: Int): RPKStoreItem? {
        return plugin.database.getTable(RPKStoreItemTable::class.java)[id]
    }

    override fun getStoreItems(): List<RPKStoreItem> {
        return plugin.database.getTable(RPKStoreItemTable::class.java).getAll()
    }

    override fun addStoreItem(storeItem: RPKStoreItem) {
        val event = RPKBukkitStoreItemCreateEvent(storeItem)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventStoreItem = event.storeItem
        when (eventStoreItem) {
            is RPKConsumableStoreItem -> plugin.database.getTable(RPKConsumableStoreItemTable::class.java).insert(eventStoreItem)
            is RPKPermanentStoreItem -> plugin.database.getTable(RPKPermanentStoreItemTable::class.java).insert(eventStoreItem)
            is RPKTimedStoreItem -> plugin.database.getTable(RPKTimedStoreItemTable::class.java).insert(eventStoreItem)
        }
    }

    override fun createConsumableStoreItem(
        plugin: String,
        identifier: String,
        description: String,
        cost: Int,
        uses: Int
    ): RPKConsumableStoreItem {
        val storeItem = RPKConsumableStoreItemImpl(
            null,
            uses,
            plugin,
            identifier,
            description,
            cost
        )
        addStoreItem(storeItem)
        return storeItem
    }

    override fun createPermanentStoreItem(
        plugin: String,
        identifier: String,
        description: String,
        cost: Int
    ): RPKPermanentStoreItem {
        val storeItem = RPKPermanentStoreItemImpl(
            null,
            plugin,
            identifier,
            description,
            cost
        )
        addStoreItem(storeItem)
        return storeItem
    }

    override fun createTimedStoreItem(
        plugin: String,
        identifier: String,
        description: String,
        cost: Int,
        duration: Duration
    ): RPKTimedStoreItem {
        val storeItem = RPKTimedStoreItemImpl(
            null,
            duration,
            plugin,
            identifier,
            description,
            cost
        )
        addStoreItem(storeItem)
        return storeItem
    }

    override fun updateStoreItem(storeItem: RPKStoreItem) {
        val event = RPKBukkitStoreItemUpdateEvent(storeItem)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventStoreItem = event.storeItem
        when (eventStoreItem) {
            is RPKConsumableStoreItem -> plugin.database.getTable(RPKConsumableStoreItemTable::class.java).update(eventStoreItem)
            is RPKPermanentStoreItem -> plugin.database.getTable(RPKPermanentStoreItemTable::class.java).update(eventStoreItem)
            is RPKTimedStoreItem -> plugin.database.getTable(RPKTimedStoreItemTable::class.java).update(eventStoreItem)
        }
    }

    override fun removeStoreItem(storeItem: RPKStoreItem) {
        val event = RPKBukkitStoreItemDeleteEvent(storeItem)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventStoreItem = event.storeItem
        when (eventStoreItem) {
            is RPKConsumableStoreItem -> plugin.database.getTable(RPKConsumableStoreItemTable::class.java).delete(eventStoreItem)
            is RPKPermanentStoreItem -> plugin.database.getTable(RPKPermanentStoreItemTable::class.java).delete(eventStoreItem)
            is RPKTimedStoreItem -> plugin.database.getTable(RPKTimedStoreItemTable::class.java).delete(eventStoreItem)
        }
    }
}