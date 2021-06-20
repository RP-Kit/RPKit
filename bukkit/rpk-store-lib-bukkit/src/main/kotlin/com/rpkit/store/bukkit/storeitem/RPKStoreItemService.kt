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
import com.rpkit.core.service.Service
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * Provides store item related operations
 */
interface RPKStoreItemService : Service {

    /**
     * Gets a store item by plugin and identifier.
     *
     * @param plugin The plugin providing the store item
     * @param identifier The unique identifier of the store item
     */
    fun getStoreItem(plugin: RPKBukkitPlugin, identifier: String): CompletableFuture<RPKStoreItem?>

    /**
     * Gets a store item by ID.
     *
     * @param id The ID of the store item
     */
    fun getStoreItem(id: RPKStoreItemId): CompletableFuture<RPKStoreItem?>

    /**
     * Gets a list of all store items available
     *
     * @return A list containing all available store items
     */
    fun getStoreItems(): CompletableFuture<List<RPKStoreItem>>

    /**
     * Adds a store item
     *
     * @param storeItem The store item to add
     */
    fun addStoreItem(storeItem: RPKStoreItem): CompletableFuture<Void>

    fun createConsumableStoreItem(
        plugin: String,
        identifier: String,
        description: String,
        cost: Int,
        uses: Int
    ): CompletableFuture<RPKConsumableStoreItem>

    fun createPermanentStoreItem(
        plugin: String,
        identifier: String,
        description: String,
        cost: Int
    ): CompletableFuture<RPKPermanentStoreItem>

    fun createTimedStoreItem(
        plugin: String,
        identifier: String,
        description: String,
        cost: Int,
        duration: Duration
    ): CompletableFuture<RPKTimedStoreItem>

    /**
     * Updates a store item in data storage
     *
     * @param storeItem The store item to update
     */
    fun updateStoreItem(storeItem: RPKStoreItem): CompletableFuture<Void>

    /**
     * Removes a store item
     *
     * @param storeItem The store item to remove
     */
    fun removeStoreItem(storeItem: RPKStoreItem): CompletableFuture<Void>

}