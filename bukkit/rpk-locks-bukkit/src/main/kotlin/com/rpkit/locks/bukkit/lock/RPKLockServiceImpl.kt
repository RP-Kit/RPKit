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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.CopyOnWriteArrayList


class RPKLockServiceImpl(override val plugin: RPKLocksBukkit) : RPKLockService {

    override val lockItem: ItemStack = plugin.config.getItemStack("lock-item")
            ?: ItemStack(Material.IRON_INGOT).withDisplayName("Lock")
    val keyItem: ItemStack = plugin.config.getItemStack("key-item")
            ?: ItemStack(Material.IRON_INGOT).withDisplayName("Key")

    private data class LockedBlockKey(
        val world: String,
        val x: Int,
        val y: Int,
        val z: Int
    )

    private val lockedBlocks = CopyOnWriteArrayList<LockedBlockKey>()
    private val unclaiming = CopyOnWriteArrayList<Int>()
    private val gettingKey = CopyOnWriteArrayList<Int>()

    private fun Block.toLockedBlockKey() = LockedBlockKey(world.name, x, y, z)

    override fun isLocked(block: Block): Boolean {
        return lockedBlocks.contains(block.toLockedBlockKey())
    }

    override fun setLocked(block: Block, locked: Boolean): CompletableFuture<Void> = runAsync {
        val lockedBlockTable = plugin.database.getTable(RPKLockedBlockTable::class.java)
        if (locked) {
            val event = RPKBukkitBlockLockEvent(block, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val lockedBlock = lockedBlockTable[event.block].join()
            if (lockedBlock == null) {
                lockedBlockTable.insert(RPKLockedBlock(block = event.block)).join()
                lockedBlocks.add(block.toLockedBlockKey())
            }
        } else {
            val event = RPKBukkitBlockUnlockEvent(block, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val lockedBlock = lockedBlockTable[event.block].join()
            if (lockedBlock != null) {
                lockedBlockTable.delete(lockedBlock).join()
                lockedBlocks.remove(block.toLockedBlockKey())
            }
        }
    }

    private fun loadLockedBlocks() {
        plugin.database.getTable(RPKLockedBlockTable::class.java).getAll().thenAccept { blocks ->
            lockedBlocks.addAll(blocks.map { it.block.toLockedBlockKey() })
            plugin.logger.info("Loaded locked blocks")
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
        return minecraftProfile.id?.value?.let { unclaiming.contains(it) } ?: false
    }

    override fun setUnclaiming(minecraftProfile: RPKMinecraftProfile, unclaiming: Boolean): CompletableFuture<Void> {
        return runAsync {
            val playerUnclaimingTable = plugin.database.getTable(RPKPlayerUnclaimingTable::class.java)
            if (unclaiming) {
                val playerUnclaiming = playerUnclaimingTable[minecraftProfile].join()
                if (playerUnclaiming == null) {
                    playerUnclaimingTable.insert(RPKPlayerUnclaiming(minecraftProfile = minecraftProfile)).join()
                    minecraftProfile.id?.value?.let { this.unclaiming.add(it) }
                }
            } else {
                val playerUnclaiming = playerUnclaimingTable[minecraftProfile].join()
                if (playerUnclaiming != null) {
                    playerUnclaimingTable.delete(playerUnclaiming).join()
                    minecraftProfile.id?.value?.let { this.unclaiming.remove(it) }
                }
            }
        }
    }

    private fun loadUnclaiming() {
        plugin.database.getTable(RPKPlayerUnclaimingTable::class.java).getAll().thenAccept { unclaimingPlayers ->
            unclaiming.addAll(unclaimingPlayers.mapNotNull { it.minecraftProfile.id?.value })
            plugin.logger.info("Loaded unclaiming players")
        }
    }

    override fun isGettingKey(minecraftProfile: RPKMinecraftProfile): Boolean {
        return minecraftProfile.id?.value?.let { gettingKey.contains(it) } ?: false
    }

    override fun setGettingKey(minecraftProfile: RPKMinecraftProfile, gettingKey: Boolean): CompletableFuture<Void> = runAsync {
        val playerGettingKeyTable = plugin.database.getTable(RPKPlayerGettingKeyTable::class.java)
        if (gettingKey) {
            val playerGettingKey = playerGettingKeyTable[minecraftProfile].join()
            if (playerGettingKey == null) {
                playerGettingKeyTable.insert(RPKPlayerGettingKey(minecraftProfile = minecraftProfile)).join()
                minecraftProfile.id?.value?.let { this.gettingKey.add(it) }
            }
        } else {
            val playerGettingKey = playerGettingKeyTable[minecraftProfile].join()
            if (playerGettingKey != null) {
                playerGettingKeyTable.delete(playerGettingKey).join()
                minecraftProfile.id?.value?.let { this.gettingKey.remove(it) }
            }
        }
    }

    private fun loadGettingKey() {
        plugin.database.getTable(RPKPlayerGettingKeyTable::class.java).getAll().thenAccept { playersGettingKey ->
            gettingKey.addAll(playersGettingKey.mapNotNull { it.minecraftProfile.id?.value})
            plugin.logger.info("Loaded players getting key")
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

    fun loadData() {
        loadLockedBlocks()
        loadGettingKey()
        loadUnclaiming()
    }

}