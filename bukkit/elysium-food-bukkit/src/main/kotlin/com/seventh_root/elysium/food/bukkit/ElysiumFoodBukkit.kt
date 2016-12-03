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

package com.seventh_root.elysium.food.bukkit

import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.food.bukkit.expiry.ExpiryProvider
import com.seventh_root.elysium.food.bukkit.listener.*

/**
 * Elysium food plugin default implementation.
 */
class ElysiumFoodBukkit: ElysiumBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        serviceProviders = arrayOf(
                ExpiryProvider(this)
        )
    }

    override fun registerListeners() {
        registerListeners(
                EntityDeathListener(this),
                FurnaceSmeltListener(this),
                PlayerFishListener(this),
                PlayerItemConsumeListener(this),
                PlayerPickupItemListener(this),
                PrepareItemCraftListener(this)
        )
    }

}