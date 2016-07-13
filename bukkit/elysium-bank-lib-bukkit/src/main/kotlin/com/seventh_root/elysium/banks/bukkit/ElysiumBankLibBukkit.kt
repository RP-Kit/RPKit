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

package com.seventh_root.elysium.banks.bukkit

import com.seventh_root.elysium.banks.bukkit.vault.ElysiumBanksVaultEconomy
import com.seventh_root.elysium.core.bukkit.plugin.ElysiumBukkitPlugin
import com.seventh_root.elysium.core.service.ServiceProvider
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.ServicePriority


class ElysiumBankLibBukkit: ElysiumBukkitPlugin() {

    override lateinit var serviceProviders: Array<ServiceProvider>

    override fun onEnable() {
        serviceProviders = arrayOf<ServiceProvider>()
        if (server.pluginManager.getPlugin("Vault") != null) {
            server.servicesManager.register(Economy::class.java, ElysiumBanksVaultEconomy(this), this, ServicePriority.High)
        }
    }

}