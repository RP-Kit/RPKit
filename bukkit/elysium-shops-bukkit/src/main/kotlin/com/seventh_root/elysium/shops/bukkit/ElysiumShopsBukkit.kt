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

package com.seventh_root.elysium.shops.bukkit

import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.shops.bukkit.listener.BlockBreakListener
import com.seventh_root.elysium.shops.bukkit.listener.InventoryClickListener
import com.seventh_root.elysium.shops.bukkit.listener.PlayerInteractListener
import com.seventh_root.elysium.shops.bukkit.listener.SignChangeListener
import com.seventh_root.elysium.shops.bukkit.shopcount.ElysiumShopCountProviderImpl


class ElysiumShopsBukkit: ElysiumBukkitPlugin() {

    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        serviceProviders = arrayOf(
                ElysiumShopCountProviderImpl(this)
        )
    }

    override fun registerListeners() {
        registerListeners(
                SignChangeListener(this),
                BlockBreakListener(this),
                PlayerInteractListener(this),
                InventoryClickListener(this)
        )
    }

}