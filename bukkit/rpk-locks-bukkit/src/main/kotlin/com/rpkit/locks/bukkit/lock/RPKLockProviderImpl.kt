package com.rpkit.locks.bukkit.lock

import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.table.RPKLockedBlockTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerGettingKeyTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerUnclaimingTable
import com.rpkit.locks.bukkit.event.lock.RPKBukkitBlockLockEvent
import com.rpkit.locks.bukkit.event.lock.RPKBukkitBlockUnlockEvent
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
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

    override fun isClaiming(player: RPKPlayer): Boolean {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                return isClaiming(minecraftProfile)
            }
        }
        return false
    }

    override fun isClaiming(minecraftProfile: RPKMinecraftProfile): Boolean {
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
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
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                return isUnclaiming(minecraftProfile)
            }
        }
        return false
    }

    override fun isUnclaiming(minecraftProfile: RPKMinecraftProfile): Boolean {
        return plugin.core.database.getTable(RPKPlayerUnclaimingTable::class).get(minecraftProfile) != null
    }

    override fun setUnclaiming(player: RPKPlayer, unclaiming: Boolean) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                setUnclaiming(minecraftProfile, unclaiming)
            }
        }
    }

    override fun setUnclaiming(minecraftProfile: RPKMinecraftProfile, unclaiming: Boolean) {
        val playerUnclaimingTable = plugin.core.database.getTable(RPKPlayerUnclaimingTable::class)
        if (unclaiming) {
            val playerUnclaiming = playerUnclaimingTable.get(minecraftProfile)
            if (playerUnclaiming == null) {
                playerUnclaimingTable.insert(RPKPlayerUnclaiming(minecraftProfile = minecraftProfile))
            }
        } else {
            val playerUnclaiming = playerUnclaimingTable.get(minecraftProfile)
            if (playerUnclaiming != null) {
                playerUnclaimingTable.delete(playerUnclaiming)
            }
        }
    }

    override fun isGettingKey(player: RPKPlayer): Boolean {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                return isGettingKey(minecraftProfile)
            }
        }
        return false
    }

    override fun isGettingKey(minecraftProfile: RPKMinecraftProfile): Boolean {
        return plugin.core.database.getTable(RPKPlayerGettingKeyTable::class).get(minecraftProfile) != null
    }

    override fun setGettingKey(player: RPKPlayer, gettingKey: Boolean) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                setGettingKey(minecraftProfile, gettingKey)
            }
        }
    }

    override fun setGettingKey(minecraftProfile: RPKMinecraftProfile, gettingKey: Boolean) {
        val playerGettingKeyTable = plugin.core.database.getTable(RPKPlayerGettingKeyTable::class)
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