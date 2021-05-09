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

package com.rpkit.locks.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.keyring.RPKKeyringService
import com.rpkit.locks.bukkit.lock.RPKLockService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PlayerInteractListener(private val plugin: RPKLocksBukkit) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            event.isCancelled = true
            return
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            event.isCancelled = true
            return
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player)
        if (minecraftProfile == null) {
            event.isCancelled = true
            return
        }
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
        if (character == null) {
            event.isCancelled = true
            return
        }
        val lockService = Services[RPKLockService::class.java]
        if (lockService == null) {
            event.isCancelled = true
            return
        }
        var block: Block? = null
        xLoop@ for (x in clickedBlock.x - 1..clickedBlock.x + 1) {
            for (y in clickedBlock.y - 1..clickedBlock.y + 1) {
                for (z in clickedBlock.z - 1..clickedBlock.z + 1) {
                    if (lockService.isLocked(clickedBlock.world.getBlockAt(x, y, z))) {
                        block = clickedBlock.world.getBlockAt(x, y, z)
                        break@xLoop
                    }
                }
            }
        }
        if (lockService.isClaiming(minecraftProfile)) {
            if (block != null) {
                event.player.sendMessage(plugin.messages["lock-invalid-already-locked"])
                return
            }
            if (event.player.inventory.itemInMainHand.amount == 1) {
                event.player.inventory.setItemInMainHand(null)
            } else {
                event.player.inventory.itemInMainHand.amount = event.player.inventory.itemInMainHand.amount - 1
            }
            lockService.setLocked(clickedBlock, true)
            for (item in event.player.inventory.addItem(lockService.getKeyFor(clickedBlock)).values) {
                event.player.world.dropItem(event.player.location, item)
            }
            event.player.updateInventory()
            event.player.sendMessage(plugin.messages["lock-successful"])
            event.isCancelled = true
        } else if (lockService.isUnclaiming(minecraftProfile)) {
            if (block != null) {
                if (hasKey(character, block)) {
                    lockService.setLocked(block, false).thenRun {
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            event.player.sendMessage(plugin.messages["unlock-successful"])
                            removeKey(character, block)
                            event.player.inventory.addItem(lockService.lockItem)
                            event.player.updateInventory()
                        })
                    }
                } else {
                    event.player.sendMessage(plugin.messages["unlock-invalid-no-key"])
                }
            } else {
                event.player.sendMessage(plugin.messages["unlock-invalid-not-locked"])
            }
            lockService.setUnclaiming(minecraftProfile, false)
            event.isCancelled = true
        } else if (lockService.isGettingKey(minecraftProfile)) {
            if (block == null) {
                event.player.sendMessage(plugin.messages["get-key-invalid-not-locked"])
            } else {
                for (item in event.player.inventory.addItem(lockService.getKeyFor(block)).values) {
                    event.player.world.dropItem(event.player.location, item)
                }
                event.player.updateInventory()
                event.player.sendMessage(plugin.messages["get-key-successful"])
            }
            lockService.setGettingKey(minecraftProfile, false)
            event.isCancelled = true
        } else {
            if (block == null) return
            if (hasKey(character, block)) return
            event.isCancelled = true
            event.player.sendMessage(plugin.messages["block-locked", mapOf(
                "block" to block.type.toString().toLowerCase().replace('_', ' ')
            )])
        }
    }

    private fun hasKey(character: RPKCharacter, block: Block): Boolean {
        val lockService = Services[RPKLockService::class.java] ?: return false
        val keyringService = Services[RPKKeyringService::class.java] ?: return false
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val offlineBukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            val bukkitPlayer = offlineBukkitPlayer.player
            if (bukkitPlayer != null) {
                val inventory = bukkitPlayer.inventory
                val inventoryContents = inventory.contents
                return keyringService.getPreloadedKeyring(character)
                        ?.any { it.isSimilar(lockService.getKeyFor(block)) } ?: false
                            || inventoryContents
                        .filterNotNull()
                        .any { it.isSimilar(lockService.getKeyFor(block)) }
            }
        }
        return false
    }

    private fun removeKey(character: RPKCharacter, block: Block) {
        val lockService = Services[RPKLockService::class.java] ?: return
        val keyringService = Services[RPKKeyringService::class.java] ?: return
        val keyring = keyringService.getPreloadedKeyring(character)
        if (keyring != null) {
            val iterator = keyring.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                if (key.isSimilar(lockService.getKeyFor(block))) {
                    if (key.amount > 1) {
                        key.amount = key.amount - 1
                    } else {
                        iterator.remove()
                    }
                    return
                }
            }
            keyringService.setKeyring(character, keyring)
        }
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val offlineBukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
            val bukkitPlayer = offlineBukkitPlayer.player
            if (bukkitPlayer != null) {
                val inventory = bukkitPlayer.inventory
                val inventoryContents = inventory.contents
                for (key in inventoryContents) {
                    if (key.isSimilar(lockService.getKeyFor(block))) {
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
