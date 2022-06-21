/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.locks.bukkit.command

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKLockService
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.toBukkitPlayer
import org.bukkit.Material.IRON_INGOT
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class CopyKeyCommand(private val plugin: RPKLocksBukkit) : RPKCommandExecutor {

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.locks.command.copykey")) {
            sender.sendMessage(plugin.messages.noPermissionCopyKey)
            return completedFuture(NoPermissionFailure("rpkit.locks.command.copykey"))
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        val lockService = Services[RPKLockService::class.java]
        if (lockService == null) {
            sender.sendMessage(plugin.messages.noLockService)
            return completedFuture(MissingServiceFailure(RPKLockService::class.java))
        }
        val bukkitPlayer = sender.toBukkitPlayer()
            ?: return completedFuture(NotAPlayerFailure())
        val itemInHand = bukkitPlayer.inventory.itemInMainHand
        if (!lockService.isKey(itemInHand)) {
            sender.sendMessage(plugin.messages.copyKeyInvalidNoKeyInHand)
            return completedFuture(NoKeyInHandFailure())
        }
        if (!bukkitPlayer.inventory.containsAtLeast(ItemStack(IRON_INGOT), 1)) {
            sender.sendMessage(plugin.messages.copyKeyInvalidNoMaterial)
            return completedFuture(NoMaterialFailure())
        }
        bukkitPlayer.inventory.removeItem(ItemStack(IRON_INGOT, 1))
        val newKey = ItemStack(itemInHand)
        newKey.amount = 1
        bukkitPlayer.inventory.addItem(newKey).forEach { (_, item) ->
            bukkitPlayer.world.dropItem(bukkitPlayer.location, item)
        }
        sender.sendMessage(plugin.messages.copyKeyValid)
        return completedFuture(CommandSuccess)
    }

    class NoKeyInHandFailure : CommandFailure()
    class NoMaterialFailure : CommandFailure()
}