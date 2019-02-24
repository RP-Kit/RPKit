/*
 * Copyright 2018 Ross Binden
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
        when (storeItem) {
            is RPKConsumableStoreItem -> plugin.core.database.getTable(RPKConsumableStoreItemTable::class).insert(storeItem)
            is RPKPermanentStoreItem -> plugin.core.database.getTable(RPKPermanentStoreItemTable::class).insert(storeItem)
            is RPKTimedStoreItem -> plugin.core.database.getTable(RPKTimedStoreItemTable::class).insert(storeItem)
        }
    }

    override fun updateStoreItem(storeItem: RPKStoreItem) {
        when (storeItem) {
            is RPKConsumableStoreItem -> plugin.core.database.getTable(RPKConsumableStoreItemTable::class).update(storeItem)
            is RPKPermanentStoreItem -> plugin.core.database.getTable(RPKPermanentStoreItemTable::class).update(storeItem)
            is RPKTimedStoreItem -> plugin.core.database.getTable(RPKTimedStoreItemTable::class).update(storeItem)
        }
    }

    override fun removeStoreItem(storeItem: RPKStoreItem) {
        throw UnsupportedOperationException("not implemented")
    }
}