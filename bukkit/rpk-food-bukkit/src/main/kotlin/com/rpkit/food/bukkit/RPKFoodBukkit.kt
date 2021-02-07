/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.service.Services
import com.rpkit.food.bukkit.expiry.RPKExpiryService
import com.rpkit.food.bukkit.expiry.RPKExpiryServiceImpl
import com.rpkit.food.bukkit.listener.EntityDeathListener
import com.rpkit.food.bukkit.listener.EntityPickupItemListener
import com.rpkit.food.bukkit.listener.FurnaceSmeltListener
import com.rpkit.food.bukkit.listener.InventoryOpenListener
import com.rpkit.food.bukkit.listener.PlayerFishListener
import com.rpkit.food.bukkit.listener.PlayerItemConsumeListener
import com.rpkit.food.bukkit.listener.PlayerJoinListener
import com.rpkit.food.bukkit.listener.PrepareItemCraftListener
import org.bstats.bukkit.Metrics

/**
 * RPK food plugin default implementation.
 */
class RPKFoodBukkit : RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this, 4397)
        saveDefaultConfig()

        val expiryService = RPKExpiryServiceImpl(this)
        Services[RPKExpiryService::class.java] = expiryService
        Services[RPKExpiryServiceImpl::class.java] = expiryService

        registerListeners()
    }

    fun registerListeners() {
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

}