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

package com.rpkit.locks.bukkit.lock

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * Provides lock related operations.
 */
interface RPKLockService : Service {

    /**
     * The item used to represent a lock.
     * This should have item meta to distinguish it from other items.
     */
    val lockItem: ItemStack

    /**
     * Checks whether a block is locked.
     *
     * @param block The block
     * @return Whether the block is locked
     */
    fun isLocked(block: Block): Boolean

    /**
     * Sets a block to be locked.
     *
     * @param block The block
     * @param locked Whether the block should be locked
     */
    fun setLocked(block: Block, locked: Boolean)

    /**
     * Checks whether a Minecraft profile is currently locking a block.
     * When a Minecraft profile is claiming, the next block they interact with becomes locked, and they are given a key
     * item.
     *
     * @param minecraftProfile The Minecraft profile
     * @return Whether the Minecraft profile is claiming
     */
    fun isClaiming(minecraftProfile: RPKMinecraftProfile): Boolean

    /**
     * Checks whether a Minecraft profile is currently unlocking a block.
     * When a Minecraft profile is unclaiming, the next block they interact with becomes unlocked, the key is removed from
     * their inventory and keyring if present, and a lock is returned to them.
     *
     * @param minecraftProfile The Minecraft profile
     * @return Whether the Minecraft profile is unclaiming
     */
    fun isUnclaiming(minecraftProfile: RPKMinecraftProfile): Boolean

    /**
     * Sets whether a Minecraft profile is currently unlocking a block.
     * When a player is unclaiming, the next block they interact with becomes unlocked, the key is removed from their
     * inventory and keyring if present, and a lock is returned o them.
     *
     * @param minecraftProfile The Minecraft profile
     * @param unclaiming Whether the Minecraft profile should be unclaiming
     */
    fun setUnclaiming(minecraftProfile: RPKMinecraftProfile, unclaiming: Boolean)

    /**
     * Checks whether a Minecraft profile is currently getting a key.
     * When a Minecraft profile is getting a key, if the next block they interact with is locked, they receive the key
     * for the block.
     *
     * @param minecraftProfile The Minecraft profile
     * @return Whether the Minecraft profile is getting a key
     */
    fun isGettingKey(minecraftProfile: RPKMinecraftProfile): Boolean

    /**
     * Sets whether a Minecraft profile is currently getting a key.
     * When a Minecraft profile is getting a key, if the next block they interact with is locked, they receive the key
     * for the block.
     *
     * @param minecraftProfile The Minecraft profile
     * @param gettingKey Whether the Minecraft profile should be getting a key
     */
    fun setGettingKey(minecraftProfile: RPKMinecraftProfile, gettingKey: Boolean)

    /**
     * Gets the key item for a block, complete with location-specific lore.
     *
     * @param block The block to get the key item for
     * @return The key item
     */
    fun getKeyFor(block: Block): ItemStack

    /**
     * Checks whether an item is a key
     *
     * @param item The item to check
     * @return Whether the item is a key
     */
    fun isKey(item: ItemStack): Boolean

}