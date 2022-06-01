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

package com.rpkit.banks.bukkit

import com.rpkit.banks.bukkit.vault.RPKBanksVaultEconomy
import com.rpkit.core.plugin.RPKPlugin
import net.milkbowl.vault.economy.Economy
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin

/**
 * Class to allow bank lib to load as a plugin.
 * This allows plugins requiring auctions or implementing banks to depend on the plugin.
 * With this plugin loaded, a Vault economy service is added for banks plugins on [ServicePriority.High].
 * If banks plugins wish to provide their own economy service, they should register on [ServicePriority.Highest]
 */
class RPKBankLibBukkit : JavaPlugin(), RPKPlugin {


    override fun onEnable() {
        Metrics(this, 4377)
        if (server.pluginManager.getPlugin("Vault") != null) {
            server.servicesManager.register(Economy::class.java, RPKBanksVaultEconomy(this), this, ServicePriority.High)
        }
    }

}