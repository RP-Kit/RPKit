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

package com.rpkit.store.bukkit.command

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.purchase.RPKPurchaseService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.format.DateTimeFormatter

class PurchasesCommand(private val plugin: RPKStoresBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.stores.command.purchases")) {
            sender.sendMessage(plugin.messages["no-permission-purchases"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile-self"])
            return true
        }
        val purchaseService = Services[RPKPurchaseService::class.java]
        if (purchaseService == null) {
            sender.sendMessage(plugin.messages["no-purchase-service"])
            return true
        }
        sender.sendMessage(plugin.messages["purchases-title"])
        purchaseService.getPurchases(profile).forEach { purchase ->
            sender.sendMessage(plugin.messages["purchases-item", mapOf(
                    Pair("purchase_id", purchase.id.toString()),
                    Pair("purchase_date", DateTimeFormatter.ISO_DATE_TIME.format(purchase.purchaseDate)),
                    Pair("store_item_identifier", purchase.storeItem.identifier), // order is important, must be before id
                    Pair("store_item_id", purchase.storeItem.id.toString()),
                    Pair("store_item_plugin", purchase.storeItem.plugin),
                    Pair("store_item_description", purchase.storeItem.description),
                    Pair("store_item_cost", String.format("%.02f", purchase.storeItem.cost / 100.0) + plugin.config.getString("purchases.currency"))
            )])
        }
        return true
    }

}
