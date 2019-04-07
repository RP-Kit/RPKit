package com.rpkit.locks.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.keyring.RPKKeyringProvider
import com.rpkit.locks.bukkit.lock.RPKLockProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PlayerInteractListener(private val plugin: RPKLocksBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock
        if (clickedBlock != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
            if (minecraftProfile != null) {
                val character = characterProvider.getActiveCharacter(minecraftProfile)
                if (character != null) {
                    val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
                    var block: Block? = null
                    xLoop@ for (x in clickedBlock.x - 1..clickedBlock.x + 1) {
                        for (y in clickedBlock.y - 1..clickedBlock.y + 1) {
                            for (z in clickedBlock.z - 1..clickedBlock.z + 1) {
                                if (lockProvider.isLocked(clickedBlock.world.getBlockAt(x, y, z))) {
                                    block = clickedBlock.world.getBlockAt(x, y, z)
                                    break@xLoop
                                }
                            }
                        }
                    }
                    if (lockProvider.isClaiming(minecraftProfile)) {
                        if (block == null) {
                            if (event.player.inventory.itemInMainHand.amount == 1) {
                                event.player.inventory.setItemInMainHand(null)
                            } else {
                                event.player.inventory.itemInMainHand.amount = event.player.inventory.itemInMainHand.amount - 1
                            }
                            lockProvider.setLocked(clickedBlock, true)
                            for (item in event.player.inventory.addItem(lockProvider.getKeyFor(clickedBlock)).values) {
                                event.player.world.dropItem(event.player.location, item)
                            }
                            event.player.updateInventory()
                            event.player.sendMessage(plugin.messages["lock-successful"])
                        } else {
                            event.player.sendMessage(plugin.messages["lock-invalid-already-locked"])
                        }
                        event.isCancelled = true
                    } else if (lockProvider.isUnclaiming(minecraftProfile)) {
                        if (block != null) {
                            if (hasKey(character, block)) {
                                lockProvider.setLocked(block, false)
                                event.player.sendMessage(plugin.messages["unlock-successful"])
                                removeKey(character, block)
                                event.player.inventory.addItem(lockProvider.lockItem)
                                event.player.updateInventory()
                            } else {
                                event.player.sendMessage(plugin.messages["unlock-invalid-no-key"])
                            }
                        } else {
                            event.player.sendMessage(plugin.messages["unlock-invalid-not-locked"])
                        }
                        lockProvider.setUnclaiming(minecraftProfile, false)
                        event.isCancelled = true
                    } else if (lockProvider.isGettingKey(minecraftProfile)) {
                        if (block == null) {
                            event.player.sendMessage(plugin.messages["get-key-invalid-not-locked"])
                        } else {
                            for (item in event.player.inventory.addItem(lockProvider.getKeyFor(block)).values) {
                                event.player.world.dropItem(event.player.location, item)
                            }
                            event.player.updateInventory()
                            event.player.sendMessage(plugin.messages["get-key-successful"])
                        }
                        lockProvider.setGettingKey(minecraftProfile, false)
                        event.isCancelled = true
                    } else {
                        if (block != null) {
                            if (hasKey(character, block)) return
                            if (!hasKey(character, block)) {
                                event.isCancelled = true
                                event.player.sendMessage(plugin.messages["block-locked", mapOf(
                                        Pair("block", block.type.toString().toLowerCase().replace('_', ' '))
                                )])
                            }
                        }
                    }
                } else {
                    event.isCancelled = true
                }
            } else {
                event.isCancelled = true
            }
        }
    }

    fun hasKey(character: RPKCharacter, block: Block): Boolean {
        val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
        val keyringProvider = plugin.core.serviceManager.getServiceProvider(RPKKeyringProvider::class)
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val offlineBukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            val bukkitPlayer = offlineBukkitPlayer.player
            if (bukkitPlayer != null) {
                val inventory = bukkitPlayer.inventory
                repeat(keyringProvider.getKeyring(character)
                        .filter { it.isSimilar(lockProvider.getKeyFor(block)) }.size) { return true }
                repeat(inventory.contents
                        .filter { it != null }
                        .filter { it.isSimilar(lockProvider.getKeyFor(block)) }.size) { return true }
            }
        }
        return false
    }

    fun removeKey(character: RPKCharacter, block: Block) {
        val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
        val keyringProvider = plugin.core.serviceManager.getServiceProvider(RPKKeyringProvider::class)
        val keyring = keyringProvider.getKeyring(character)
        val iterator = keyring.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            if (key.isSimilar(lockProvider.getKeyFor(block))) {
                if (key.amount > 1) {
                    key.amount = key.amount - 1
                } else {
                    iterator.remove()
                }
                return
            }
        }
        keyringProvider.setKeyring(character, keyring)
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val offlineBukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            val bukkitPlayer = offlineBukkitPlayer.player
            if (bukkitPlayer != null) {
                val inventory = bukkitPlayer.inventory
                val inventoryContents = inventory.contents
                for (key in inventoryContents) {
                    if (key.isSimilar(lockProvider.getKeyFor(block))) {
                        val oneKey = ItemStack(key)
                        oneKey.amount = 1
                        inventory.removeItem(oneKey)
                        return
                    }
                }
            }
        }

    }
}
