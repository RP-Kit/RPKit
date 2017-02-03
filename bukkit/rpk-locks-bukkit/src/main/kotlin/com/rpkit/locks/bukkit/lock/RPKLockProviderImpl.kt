package com.rpkit.locks.bukkit.lock

import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.table.RPKLockedBlockTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerGettingKeyTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerUnclaimingTable
import com.rpkit.players.bukkit.player.RPKPlayer
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack


class RPKLockProviderImpl(private val plugin: RPKLocksBukkit): RPKLockProvider {

    override val lockItem: ItemStack = plugin.config.getItemStack("lock-item")
    val keyItem: ItemStack = plugin.config.getItemStack("key-item")

    override fun isLocked(block: Block): Boolean {
        return plugin.core.database.getTable(RPKLockedBlockTable::class).get(block) != null
    }

    override fun setLocked(block: Block, locked: Boolean) {
        val lockedBlockTable = plugin.core.database.getTable(RPKLockedBlockTable::class)
        if (locked) {
            val lockedBlock = lockedBlockTable.get(block)
            if (lockedBlock == null) {
                lockedBlockTable.insert(RPKLockedBlock(block = block))
            }
        } else {
            val lockedBlock = lockedBlockTable.get(block)
            if (lockedBlock != null) {
                lockedBlockTable.delete(lockedBlock)
            }
        }
    }

    override fun isClaiming(player: RPKPlayer): Boolean {
        val bukkitOfflinePlayer = player.bukkitPlayer
        if (bukkitOfflinePlayer != null) {
            if (bukkitOfflinePlayer.isOnline) {
                val bukkitPlayer = bukkitOfflinePlayer.player
                val item = bukkitPlayer.inventory.itemInMainHand
                if (item != null) {
                    val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
                    if (item.isSimilar(lockProvider.lockItem)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun isUnclaiming(player: RPKPlayer): Boolean {
        return plugin.core.database.getTable(RPKPlayerUnclaimingTable::class).get(player) != null
    }

    override fun setUnclaiming(player: RPKPlayer, unclaiming: Boolean) {
        val playerUnclaimingTable = plugin.core.database.getTable(RPKPlayerUnclaimingTable::class)
        if (unclaiming) {
            val playerUnclaiming = playerUnclaimingTable.get(player)
            if (playerUnclaiming == null) {
                playerUnclaimingTable.insert(RPKPlayerUnclaiming(player = player))
            }
        } else {
            val playerUnclaiming = playerUnclaimingTable.get(player)
            if (playerUnclaiming != null) {
                playerUnclaimingTable.delete(playerUnclaiming)
            }
        }
    }

    override fun isGettingKey(player: RPKPlayer): Boolean {
        return plugin.core.database.getTable(RPKPlayerGettingKeyTable::class).get(player) != null
    }

    override fun setGettingKey(player: RPKPlayer, gettingKey: Boolean) {
        val playerGettingKeyTable = plugin.core.database.getTable(RPKPlayerGettingKeyTable::class)
        if (gettingKey) {
            val playerGettingKey = playerGettingKeyTable.get(player)
            if (playerGettingKey == null) {
                playerGettingKeyTable.insert(RPKPlayerGettingKey(player = player))
            }
        } else {
            val playerGettingKey = playerGettingKeyTable.get(player)
            if (playerGettingKey != null) {
                playerGettingKeyTable.delete(playerGettingKey)
            }
        }
    }

    override fun getKeyFor(block: Block): ItemStack {
        val key = ItemStack(keyItem)
        val meta = key.itemMeta
        val lore = if (key.hasItemMeta()) if (meta.hasLore()) meta.lore else mutableListOf<String>() else mutableListOf<String>()
        lore.add("${block.world.name},${block.x},${block.y},${block.z}")
        meta.lore = lore
        key.itemMeta = meta
        return key
    }

    override fun isKey(item: ItemStack): Boolean {
        val key = ItemStack(item)
        val meta = key.itemMeta ?: return false
        val lore = if (key.hasItemMeta()) if (meta.hasLore()) meta.lore else mutableListOf<String>() else mutableListOf<String>()
        val locationLoreItem = lore.filter { it.matches(Regex("\\w+,-?\\d+,-?\\d+,-?\\d+")) }.lastOrNull()
        if (locationLoreItem != null) {
            lore.remove(locationLoreItem)
        }
        meta.lore = lore
        key.itemMeta = meta
        return key.isSimilar(keyItem)
    }

}