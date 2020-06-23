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


class RPKStoreItemProviderImpl(private val plugin: RPKStoresBukkit): RPKStoreItemProvider {

    override fun getStoreItem(plugin: RPKBukkitPlugin, identifier: String): RPKStoreItem? {
        return this.plugin.core.database.getTable(RPKStoreItemTable::class).get(plugin, identifier)
    }

    override fun getStoreItem(id: Int): RPKStoreItem? {
        return plugin.core.database.getTable(RPKStoreItemTable::class)[id]
    }

    override fun getStoreItems(): List<RPKStoreItem> {
        return plugin.core.database.getTable(RPKStoreItemTable::class).getAll()
    }

    override fun addStoreItem(storeItem: RPKStoreItem) {
        val event = RPKBukkitStoreItemCreateEvent(storeItem)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventStoreItem = event.storeItem
        when (eventStoreItem) {
            is RPKConsumableStoreItem -> plugin.core.database.getTable(RPKConsumableStoreItemTable::class).insert(eventStoreItem)
            is RPKPermanentStoreItem -> plugin.core.database.getTable(RPKPermanentStoreItemTable::class).insert(eventStoreItem)
            is RPKTimedStoreItem -> plugin.core.database.getTable(RPKTimedStoreItemTable::class).insert(eventStoreItem)
        }
    }

    override fun updateStoreItem(storeItem: RPKStoreItem) {
        val event = RPKBukkitStoreItemUpdateEvent(storeItem)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventStoreItem = event.storeItem
        when (eventStoreItem) {
            is RPKConsumableStoreItem -> plugin.core.database.getTable(RPKConsumableStoreItemTable::class).update(eventStoreItem)
            is RPKPermanentStoreItem -> plugin.core.database.getTable(RPKPermanentStoreItemTable::class).update(eventStoreItem)
            is RPKTimedStoreItem -> plugin.core.database.getTable(RPKTimedStoreItemTable::class).update(eventStoreItem)
        }
    }

    override fun removeStoreItem(storeItem: RPKStoreItem) {
        val event = RPKBukkitStoreItemDeleteEvent(storeItem)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val eventStoreItem = event.storeItem
        when (eventStoreItem) {
            is RPKConsumableStoreItem -> plugin.core.database.getTable(RPKConsumableStoreItemTable::class).delete(eventStoreItem)
            is RPKPermanentStoreItem -> plugin.core.database.getTable(RPKPermanentStoreItemTable::class).delete(eventStoreItem)
            is RPKTimedStoreItem -> plugin.core.database.getTable(RPKTimedStoreItemTable::class).delete(eventStoreItem)
        }
    }
}