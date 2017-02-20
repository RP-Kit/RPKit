/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.trade.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.trade.bukkit.listener.BlockBreakListener
import com.rpkit.trade.bukkit.listener.PlayerInteractListener
import com.rpkit.trade.bukkit.listener.SignChangeListener

/**
 * RPK trade plugin default implementation.
 */
class RPKTradeBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        // Migrate config 1.1 -> 1.2
        if (!config.isConfigurationSection("traders.minimum-price")) {
            val minimumPrice = config.getInt("traders.minimum-price")
            config.set("traders.minimum-price", null)
            config.set("traders.minimum-price.default", minimumPrice)
        }
        if (!config.isConfigurationSection("traders.maximum-price")) {
            val maximumPrice = config.getInt("traders.maximum-price")
            config.set("traders.maximum-price", null)
            config.set("traders.maximum-price.default", maximumPrice)
        }
        serviceProviders = arrayOf()
    }

    override fun registerListeners() {
        registerListeners(
                BlockBreakListener(this),
                SignChangeListener(this),
                PlayerInteractListener(this)
        )
    }

    override fun setDefaultMessages() {
        messages.setDefault("trader-buy", "&fBought \$quantity \$material from the trader for \$price")
        messages.setDefault("trader-sell", "&fSold \$quantity \$material to the trader for \$price")
        messages.setDefault("trader-buy-insufficient-funds", "&cYou can not afford to buy that.")
        messages.setDefault("trader-sell-insufficient-wallet-space", "&cYou could not fit the money from that in your wallet.")
        messages.setDefault("trader-sell-insufficient-items", "&cYou do not have enough of the required item.")
        messages.setDefault("trader-sign-invalid-material", "&cThat''s not a valid material. (Line 2)")
        messages.setDefault("trader-sign-invalid-price", "&cPrice must be formatted \"buy price | sell price\" (Line 3)")
        messages.setDefault("trader-sign-invalid-currency", "&cThat''s not a valid currency. (Line 4)")
        messages.setDefault("trader-sign-valid", "&aCreated a trader sign.")
        messages.setDefault("no-permission-trader-create", "&cYou do not have permission to create a trader.")
        messages.setDefault("no-permission-trader-destroy", "&cYou do not have permission to destroy traders.")
        messages.setDefault("no-permission-trader-buy", "&cYou do not have permission to buy from traders.")
        messages.setDefault("no-permission-trader-sell", "&cYou do not have permission to sell to traders.")
        messages.setDefault("no-character", "&cYou need a character to perform that action. Please create one.")
    }

}