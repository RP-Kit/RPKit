package com.rpkit.locks.bukkit.lock

import com.rpkit.core.bukkit.util.withDisplayName
import com.rpkit.core.bukkit.util.withLore
import com.rpkit.core.bukkit.util.withoutLoreMatching
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.table.RPKLockedBlockTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerGettingKeyTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerUnclaimingTable
import com.rpkit.locks.bukkit.event.lock.RPKBukkitBlockLockEvent
import com.rpkit.locks.bukkit.event.lock.RPKBukkitBlockUnlockEvent
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack


class RPKLockProviderImpl(private val plugin: RPKLocksBukkit): RPKLockProvider {

    override val lockItem: ItemStack = plugin.config.getItemStack("lock-item")
            ?: ItemStack(Material.IRON_INGOT).withDisplayName("Lock")
    val keyItem: ItemStack = plugin.config.getItemStack("key-item")
            ?: ItemStack(Material.IRON_INGOT).withDisplayName("Key")

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
        val bukkitPlayer = bukkitOfflinePlayer.player
        if (bukkitPlayer != null) {
            val item = bukkitPlayer.inventory.itemInMainHand
            val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
            if (item.isSimilar(lockProvider.lockItem)) {
                return true
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
        return ItemStack(keyItem)
                .withLore(listOf("${block.world.name},${block.x},${block.y},${block.z}"))
    }

    override fun isKey(item: ItemStack): Boolean {
        val key = ItemStack(item)
                .withoutLoreMatching(Regex("\\w+,-?\\d+,-?\\d+,-?\\d+"))
        return key.isSimilar(keyItem)
    }

}