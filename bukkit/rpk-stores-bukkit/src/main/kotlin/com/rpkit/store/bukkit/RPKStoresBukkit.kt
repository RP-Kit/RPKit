/*
 * Copyright 2018 Ross Binden
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

package com.rpkit.store.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.store.bukkit.command.ClaimCommand
import com.rpkit.store.bukkit.command.PurchaseCommand
import com.rpkit.store.bukkit.command.PurchasesCommand
import com.rpkit.store.bukkit.database.table.*
import com.rpkit.store.bukkit.purchase.RPKPurchaseProviderImpl
import com.rpkit.store.bukkit.storeitem.RPKStoreItemProviderImpl


class RPKStoresBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKPurchaseProviderImpl(this),
                RPKStoreItemProviderImpl(this)
        )
    }

    override fun registerCommands() {
        getCommand("purchase").executor = PurchaseCommand(this)
        getCommand("purchases").executor = PurchasesCommand(this)
        getCommand("claim").executor = ClaimCommand(this)
    }

    override fun createTables(database: Database) {
        database.addTable(RPKPurchaseTable(database, this))
        database.addTable(RPKConsumablePurchaseTable(database, this))
        database.addTable(RPKPermanentPurchaseTable(database, this))
        database.addTable(RPKTimedPurchaseTable(database, this))
        database.addTable(RPKStoreItemTable(database, this))
        database.addTable(RPKConsumableStoreItemTable(database, this))
        database.addTable(RPKPermanentStoreItemTable(database, this))
        database.addTable(RPKTimedStoreItemTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("no-permission-claim", "&cYou do not have permission to claim purchases.")
        messages.setDefault("claim-usage", "&cUsage: /claim [purchase id]")
        messages.setDefault("not-from-console", "&cYou may not use this command from console.")
        messages.setDefault("no-minecraft-profile-self", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-profile-self", "&cA profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("claim-purchase-id-invalid-integer", "&cYou must specify a valid purchase ID.")
        messages.setDefault("claim-purchase-id-invalid-purchase", "&cYou must specify a valid purchase ID.")
        messages.setDefault("claim-purchase-id-invalid-consumable", "&cThat purchase is not consumable.")
        messages.setDefault("claim-purchase-id-invalid-profile", "&cThat purchase is not yours to claim.")
        messages.setDefault("claim-plugin-not-installed", "&cThe plugin that purchase is associated with is not currently installed. Please contact a member of staff for support.")
        messages.setDefault("claim-plugin-cannot-claim", "&cThe plugin that purchase is associated with does not support claiming purchases. Please contact a member of staff for support.")
        messages.setDefault("claim-successful", "&aSuccessfully claimed your purchase.")
        messages.setDefault("only-from-console", "&cYou may only use this command from console.")
        messages.setDefault("purchase-usage", "&cUsage: /purchase [uuid] [store item id]")
        messages.setDefault("no-minecraft-profile-other", "&c\$name/\$uuid does not have a Minecraft profile.")
        messages.setDefault("no-profile-other", "&c\$name/\$uuid does not have a profile")
        messages.setDefault("purchase-store-item-id-invalid-integer", "&cYou must specify a valid store item ID.")
        messages.setDefault("purchase-store-item-id-invalid-item", "&cYou must specify a valid store item ID.")
        messages.setDefault("purchase-successful", "&aPurchase successful.")
        messages.setDefault("no-permission-purchases", "&cYou do not have permission to view your purchases.")
        messages.setDefault("purchases-title", "&fPurchases:")
        messages.setDefault("purchases-item", "&7\$purchase_id - \$store_item_plugin:\$store_item_identifier \$store_item_description - \$purchase_date")
    }

}