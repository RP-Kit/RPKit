/*
 * Copyright 2019 Ross Binden
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

package com.rpkit.store.bukkit.command

import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.purchase.RPKConsumablePurchaseImpl
import com.rpkit.store.bukkit.purchase.RPKPermanentPurchaseImpl
import com.rpkit.store.bukkit.purchase.RPKPurchaseProvider
import com.rpkit.store.bukkit.purchase.RPKTimedPurchaseImpl
import com.rpkit.store.bukkit.storeitem.RPKConsumableStoreItem
import com.rpkit.store.bukkit.storeitem.RPKPermanentStoreItem
import com.rpkit.store.bukkit.storeitem.RPKStoreItemProvider
import com.rpkit.store.bukkit.storeitem.RPKTimedStoreItem
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.time.LocalDateTime
import java.util.*

class PurchaseCommand(private val plugin: RPKStoresBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender != plugin.server.consoleSender) {
            sender.sendMessage(plugin.messages["only-from-console"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["purchase-usage"])
            return true
        }
        val bukkitOfflinePlayer = try {
            val playerUUID = UUID.fromString(args[0])
            plugin.server.getOfflinePlayer(playerUUID)
        } catch (exception: IllegalArgumentException) {
            val playerName = args[0]
            plugin.server.getOfflinePlayer(playerName)
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-other", mapOf(
                    Pair("name", bukkitOfflinePlayer.name),
                    Pair("uuid", bukkitOfflinePlayer.uniqueId.toString())
            )])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile == null) {
            sender.sendMessage(plugin.messages["no-profile-other", mapOf(
                    Pair("name", minecraftProfile.minecraftUsername),
                    Pair("uuid", minecraftProfile.minecraftUUID.toString()),
                    Pair("minecraft-profile-id", minecraftProfile.id.toString())
            )])
            return true
        }
        val storeItemId = args[1].toIntOrNull()
        if (storeItemId == null) {
            sender.sendMessage(plugin.messages["purchase-store-item-id-invalid-integer"])
            return true
        }
        val storeItemProvider = plugin.core.serviceManager.getServiceProvider(RPKStoreItemProvider::class)
        val storeItem = storeItemProvider.getStoreItem(storeItemId)
        if (storeItem == null) {
            sender.sendMessage(plugin.messages["purchase-store-item-id-invalid-item"])
            return true
        }
        val purchaseProvider = plugin.core.serviceManager.getServiceProvider(RPKPurchaseProvider::class)
        val purchase = when (storeItem) {
            is RPKConsumableStoreItem -> RPKConsumablePurchaseImpl(
                    storeItem = storeItem,
                    remainingUses = storeItem.uses,
                    profile = profile,
                    purchaseDate = LocalDateTime.now()
            )
            is RPKPermanentStoreItem -> RPKPermanentPurchaseImpl(
                    storeItem = storeItem,
                    profile = profile,
                    purchaseDate = LocalDateTime.now()
            )
            is RPKTimedStoreItem -> RPKTimedPurchaseImpl(
                    storeItem = storeItem,
                    profile = profile,
                    purchaseDate = LocalDateTime.now()
            )
            else -> null
        }
        if (purchase == null) {
            sender.sendMessage(plugin.messages["purchase-store-item-id-invalid-item"])
            return true
        }
        purchaseProvider.addPurchase(purchase)
        sender.sendMessage(plugin.messages["purchase-successful", mapOf(
                Pair("player-name", bukkitOfflinePlayer.name),
                Pair("player-uuid", bukkitOfflinePlayer.uniqueId.toString()),
                Pair("profile-id", profile.id.toString()),
                Pair("profile-name", profile.name),
                Pair("store-item-identifier", storeItem.identifier), // order is important
                Pair("store-item-id", storeItemId.toString()),
                Pair("store-item-description", storeItem.description),
                Pair("store-item-plugin", storeItem.plugin),
                Pair("store-item-cost", String.format("%.02f", storeItem.cost / 100.0) + plugin.config.getString("payments.currency"))
        )])
        return true
    }

}
