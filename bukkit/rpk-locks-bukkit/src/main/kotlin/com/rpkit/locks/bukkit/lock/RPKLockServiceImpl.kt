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

package com.rpkit.locks.bukkit.lock

import com.rpkit.core.bukkit.extension.withDisplayName
import com.rpkit.core.bukkit.extension.withLore
import com.rpkit.core.bukkit.extension.withoutLoreMatching
import com.rpkit.core.service.Services
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.table.RPKLockedBlockTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerGettingKeyTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerUnclaimingTable
import com.rpkit.locks.bukkit.event.lock.RPKBukkitBlockLockEvent
import com.rpkit.locks.bukkit.event.lock.RPKBukkitBlockUnlockEvent
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack


class RPKLockServiceImpl(override val plugin: RPKLocksBukkit) : RPKLockService {

    override val lockItem: ItemStack = plugin.config.getItemStack("lock-item")
            ?: ItemStack(Material.IRON_INGOT).withDisplayName("Lock")
    val keyItem: ItemStack = plugin.config.getItemStack("key-item")
            ?: ItemStack(Material.IRON_INGOT).withDisplayName("Key")

    override fun isLocked(block: Block): Boolean {
        return plugin.database.getTable(RPKLockedBlockTable::class.java).get(block) != null
    }

    override fun setLocked(block: Block, locked: Boolean) {
        val lockedBlockTable = plugin.database.getTable(RPKLockedBlockTable::class.java)
        if (locked) {
            val event = RPKBukkitBlockLockEvent(block)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            val lockedBlock = lockedBlockTable.get(event.block)
            if (lockedBlock == null) {
                lockedBlockTable.insert(RPKLockedBlock(block = event.block))
            }
        } else {
            val event = RPKBukkitBlockUnlockEvent(block)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            val lockedBlock = lockedBlockTable.get(event.block)
            if (lockedBlock != null) {
                lockedBlockTable.delete(lockedBlock)
            }
        }
    }

    override fun isClaiming(minecraftProfile: RPKMinecraftProfile): Boolean {
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
        val bukkitPlayer = bukkitOfflinePlayer.player ?: return false
        val item = bukkitPlayer.inventory.itemInMainHand
        val lockService = Services[RPKLockService::class.java] ?: return false
        if (item.isSimilar(lockService.lockItem)) {
            return true
        }
        return false
    }

    override fun isUnclaiming(minecraftProfile: RPKMinecraftProfile): Boolean {
        return plugin.database.getTable(RPKPlayerUnclaimingTable::class.java)[minecraftProfile] != null
    }

    override fun setUnclaiming(minecraftProfile: RPKMinecraftProfile, unclaiming: Boolean) {
        val playerUnclaimingTable = plugin.database.getTable(RPKPlayerUnclaimingTable::class.java)
        if (unclaiming) {
            val playerUnclaiming = playerUnclaimingTable[minecraftProfile]
            if (playerUnclaiming == null) {
                playerUnclaimingTable.insert(RPKPlayerUnclaiming(minecraftProfile = minecraftProfile))
            }
        } else {
            val playerUnclaiming = playerUnclaimingTable[minecraftProfile]
            if (playerUnclaiming != null) {
                playerUnclaimingTable.delete(playerUnclaiming)
            }
        }
    }

    override fun isGettingKey(minecraftProfile: RPKMinecraftProfile): Boolean {
        return plugin.database.getTable(RPKPlayerGettingKeyTable::class.java).get(minecraftProfile) != null
    }

    override fun setGettingKey(minecraftProfile: RPKMinecraftProfile, gettingKey: Boolean) {
        val playerGettingKeyTable = plugin.database.getTable(RPKPlayerGettingKeyTable::class.java)
        if (gettingKey) {
            val playerGettingKey = playerGettingKeyTable.get(minecraftProfile)
            if (playerGettingKey == null) {
                playerGettingKeyTable.insert(RPKPlayerGettingKey(minecraftProfile = minecraftProfile))
            }
        } else {
            val playerGettingKey = playerGettingKeyTable.get(minecraftProfile)
            if (playerGettingKey != null) {
                playerGettingKeyTable.delete(playerGettingKey)
            }
        }
    }

    override fun getKeyFor(block: Block): ItemStack {
        return ItemStack(keyItem)
                .withLore(listOf("${block.world.name},${block.x},${block.y},${block.z}"))
    }

    override fun isKey(item: ItemStack): Boolean {
        val key = ItemStack(item)
                .withoutLoreMatching("\\w+,-?\\d+,-?\\d+,-?\\d+")
        return key.isSimilar(keyItem)
    }

}