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
import java.util.concurrent.CompletableFuture


class RPKStoreItemServiceImpl(override val plugin: RPKStoresBukkit) : RPKStoreItemService {

    override fun getStoreItem(plugin: RPKBukkitPlugin, identifier: String): CompletableFuture<RPKStoreItem?> {
        return this.plugin.database.getTable(RPKStoreItemTable::class.java).get(plugin, identifier)
    }

    override fun getStoreItem(id: RPKStoreItemId): CompletableFuture<RPKStoreItem?> {
        return plugin.database.getTable(RPKStoreItemTable::class.java)[id]
    }

    override fun getStoreItems(): CompletableFuture<List<RPKStoreItem>> {
        return plugin.database.getTable(RPKStoreItemTable::class.java).getAll()
    }

    override fun addStoreItem(storeItem: RPKStoreItem): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitStoreItemCreateEvent(storeItem, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val eventStoreItem = event.storeItem
            when (eventStoreItem) {
                is RPKConsumableStoreItem -> plugin.database.getTable(RPKConsumableStoreItemTable::class.java)
                    .insert(eventStoreItem).join()
                is RPKPermanentStoreItem -> plugin.database.getTable(RPKPermanentStoreItemTable::class.java)
                    .insert(eventStoreItem).join()
                is RPKTimedStoreItem -> plugin.database.getTable(RPKTimedStoreItemTable::class.java)
                    .insert(eventStoreItem).join()
            }
        }
    }

    override fun createConsumableStoreItem(
        plugin: String,
        identifier: String,
        description: String,
        cost: Int,
        uses: Int
    ): CompletableFuture<RPKConsumableStoreItem> {
        val storeItem = RPKConsumableStoreItemImpl(
            null,
            uses,
            plugin,
            identifier,
            description,
            cost
        )
        return addStoreItem(storeItem).thenApply { storeItem }
    }

    override fun createPermanentStoreItem(
        plugin: String,
        identifier: String,
        description: String,
        cost: Int
    ): CompletableFuture<RPKPermanentStoreItem> {
        val storeItem = RPKPermanentStoreItemImpl(
            null,
            plugin,
            identifier,
            description,
            cost
        )
        return addStoreItem(storeItem).thenApply { storeItem }
    }

    override fun createTimedStoreItem(
        plugin: String,
        identifier: String,
        description: String,
        cost: Int,
        duration: Duration
    ): CompletableFuture<RPKTimedStoreItem> {
        val storeItem = RPKTimedStoreItemImpl(
            null,
            duration,
            plugin,
            identifier,
            description,
            cost
        )
        return addStoreItem(storeItem).thenApply { storeItem }
    }

    override fun updateStoreItem(storeItem: RPKStoreItem): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitStoreItemUpdateEvent(storeItem, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val eventStoreItem = event.storeItem
            when (eventStoreItem) {
                is RPKConsumableStoreItem -> plugin.database.getTable(RPKConsumableStoreItemTable::class.java)
                    .update(eventStoreItem).join()
                is RPKPermanentStoreItem -> plugin.database.getTable(RPKPermanentStoreItemTable::class.java)
                    .update(eventStoreItem).join()
                is RPKTimedStoreItem -> plugin.database.getTable(RPKTimedStoreItemTable::class.java)
                    .update(eventStoreItem).join()
            }
        }
    }

    override fun removeStoreItem(storeItem: RPKStoreItem): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitStoreItemDeleteEvent(storeItem, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val eventStoreItem = event.storeItem
            when (eventStoreItem) {
                is RPKConsumableStoreItem -> plugin.database.getTable(RPKConsumableStoreItemTable::class.java)
                    .delete(eventStoreItem).join()
                is RPKPermanentStoreItem -> plugin.database.getTable(RPKPermanentStoreItemTable::class.java)
                    .delete(eventStoreItem).join()
                is RPKTimedStoreItem -> plugin.database.getTable(RPKTimedStoreItemTable::class.java)
                    .delete(eventStoreItem).join()
            }
        }
    }
}