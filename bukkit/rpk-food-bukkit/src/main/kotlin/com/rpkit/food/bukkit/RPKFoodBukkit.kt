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

package com.rpkit.food.bukkit

import com.rpkit.core.bukkit.command.toBukkit
import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.food.bukkit.command.ExpiryCommand
import com.rpkit.food.bukkit.expiry.RPKExpiryService
import com.rpkit.food.bukkit.expiry.RPKExpiryServiceImpl
import com.rpkit.food.bukkit.listener.*
import com.rpkit.food.bukkit.messages.FoodMessages
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

/**
 * RPK food plugin default implementation.
 */
class RPKFoodBukkit : JavaPlugin(), RPKPlugin {

    lateinit var messages: FoodMessages

    override fun onEnable() {
        Metrics(this, 4397)
        saveDefaultConfig()

        messages = FoodMessages(this)
        messages.saveDefaultMessagesConfig()

        val expiryService = RPKExpiryServiceImpl(this)
        Services[RPKExpiryService::class.java] = expiryService
        Services[RPKExpiryServiceImpl::class.java] = expiryService

        registerListeners()
        registerCommands()
    }

    private fun registerListeners() {
        registerListeners(
                EntityDeathListener(),
                FurnaceSmeltListener(this),
                InventoryOpenListener(),
                PlayerFishListener(),
                PlayerItemConsumeListener(this),
                PlayerJoinListener(),
                EntityPickupItemListener(),
                PrepareItemCraftListener()
        )
    }

    private fun registerCommands() {
        getCommand("expiry")?.setExecutor(ExpiryCommand(this).toBukkit())
    }

}