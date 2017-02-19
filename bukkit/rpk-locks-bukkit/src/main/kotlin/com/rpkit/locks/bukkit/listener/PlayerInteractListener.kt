package com.rpkit.locks.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.keyring.RPKKeyringProvider
import com.rpkit.locks.bukkit.lock.RPKLockProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PlayerInteractListener(private val plugin: RPKLocksBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hasBlock()) {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val player = playerProvider.getPlayer(event.player)
            val character = characterProvider.getActiveCharacter(player)
            if (character != null) {
                val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
                var block: Block? = null
                xLoop@ for (x in event.clickedBlock.x - 1..event.clickedBlock.x + 1) {
                    for (y in event.clickedBlock.y - 1..event.clickedBlock.y + 1) {
                        for (z in event.clickedBlock.z - 1..event.clickedBlock.z + 1) {
                            if (lockProvider.isLocked(event.clickedBlock.world.getBlockAt(x, y, z))) {
                                block = event.clickedBlock.world.getBlockAt(x, y, z)
                                break@xLoop
                            }
                        }
                    }
                }
                if (lockProvider.isClaiming(player)) {
                    if (block == null) {
                        if (event.player.inventory.itemInMainHand.amount === 1) {
                            event.player.inventory.itemInMainHand = null
                        } else {
                            event.player.inventory.itemInMainHand.amount = event.player.inventory.itemInMainHand.amount - 1
                        }
                        lockProvider.setLocked(event.clickedBlock, true)
                        for (item in event.player.inventory.addItem(lockProvider.getKeyFor(event.clickedBlock)).values) {
                            event.player.world.dropItem(event.player.location, item)
                        }
                        event.player.updateInventory()
                        event.player.sendMessage(plugin.core.messages["lock-successful"])
                    } else {
                        event.player.sendMessage(plugin.core.messages["lock-invalid-already-locked"])
                    }
                    event.isCancelled = true
                } else if (lockProvider.isUnclaiming(player)) {
                    if (block != null) {
                        if (hasKey(character, block)) {
                            lockProvider.setLocked(block, false)
                            event.player.sendMessage(plugin.core.messages["unlock-successful"])
                            removeKey(character, block)
                            event.player.inventory.addItem(lockProvider.lockItem)
                            event.player.updateInventory()
                        } else {
                            event.player.sendMessage(plugin.core.messages["unlock-invalid-no-key"])
                        }
                    } else {
                        event.player.sendMessage(plugin.core.messages["unlock-invalid-not-locked"])
                    }
                    lockProvider.setUnclaiming(player, false)
                    event.isCancelled = true
                } else if (lockProvider.isGettingKey(player)) {
                    if (block == null) {
                        event.player.sendMessage(plugin.core.messages["get-key-invalid-not-locked"])
                    } else {
                        for (item in event.player.inventory.addItem(lockProvider.getKeyFor(block)).values) {
                            event.player.world.dropItem(event.player.location, item)
                        }
                        event.player.updateInventory()
                        event.player.sendMessage(plugin.core.messages["get-key-successful"])
                    }
                    lockProvider.setGettingKey(player, false)
                    event.isCancelled = true
                } else {
                    if (block != null) {
                        if (hasKey(character, block)) return
                        if (!hasKey(character, block)) {
                            event.isCancelled = true
                            event.player.sendMessage(plugin.core.messages["block-locked", mapOf(
                                    Pair("block", block.type.toString().toLowerCase().replace('_', ' '))
                            )])
                        }
                    }
                }
            }
        }
    }

    fun hasKey(character: RPKCharacter, block: Block): Boolean {
        val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
        val keyringProvider = plugin.core.serviceManager.getServiceProvider(RPKKeyringProvider::class)
        val inventory = character.player?.bukkitPlayer?.player?.inventory
        if (inventory != null) {
            keyringProvider.getKeyring(character)
                    .filter { it.isSimilar(lockProvider.getKeyFor(block)) }
                    .forEach { return true }
            inventory.contents
                    .filter { it != null }
                    .filter { it.isSimilar(lockProvider.getKeyFor(block)) }
                    .forEach { return true }
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
        val inventory = character.player?.bukkitPlayer?.player?.inventory
        val inventoryContents = inventory?.contents
        if (inventory != null && inventoryContents != null) {
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
