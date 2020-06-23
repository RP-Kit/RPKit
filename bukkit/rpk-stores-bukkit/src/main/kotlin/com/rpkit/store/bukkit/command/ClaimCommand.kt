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

import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.store.bukkit.RPKStoresBukkit
import com.rpkit.store.bukkit.purchase.RPKConsumablePurchase
import com.rpkit.store.bukkit.purchase.RPKPurchaseProvider
import com.rpkit.store.bukkit.resolver.RPKPurchaseResolverProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClaimCommand(private val plugin: RPKStoresBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.stores.command.claim")) {
            sender.sendMessage(plugin.messages["no-permission-claim"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["claim-usage"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            return true
        }
        val purchaseId = args[0].toIntOrNull()
        if (purchaseId == null) {
            sender.sendMessage(plugin.messages["claim-purchase-id-invalid-integer"])
            return true
        }
        val purchaseProvider = plugin.core.serviceManager.getServiceProvider(RPKPurchaseProvider::class)
        val purchase = purchaseProvider.getPurchase(purchaseId)
        if (purchase == null) {
            sender.sendMessage(plugin.messages["claim-purchase-id-invalid-purchase"])
            return true
        }
        if (purchase.profile != minecraftProfile.profile) {
            sender.sendMessage(plugin.messages["claim-purchase-id-invalid-profile"])
            return true
        }
        if (purchase !is RPKConsumablePurchase) {
            sender.sendMessage(plugin.messages["claim-purchase-id-invalid-consumable"])
            return true
        }
        val purchasePlugin = plugin.server.pluginManager.getPlugin(purchase.storeItem.plugin)
        if (purchasePlugin == null) {
            sender.sendMessage(plugin.messages["claim-plugin-not-installed"])
            return true
        }
        if (purchasePlugin !is RPKPurchaseResolverProvider) {
            sender.sendMessage(plugin.messages["claim-plugin-cannot-claim"])
            return true
        }
        purchasePlugin.getPurchaseResolver(purchase.storeItem.identifier).claim(purchase, minecraftProfile)
        purchase.remainingUses--
        purchaseProvider.updatePurchase(purchase)
        sender.sendMessage(plugin.messages["claim-successful"])
        return true
    }

}
