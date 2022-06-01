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

package com.rpkit.economy.bukkit

import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.economy.bukkit.vault.RPKEconomyVaultEconomy
import net.milkbowl.vault.economy.Economy
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin

/**
 * Class to allow economy lib to load as a plugin.
 * This allows plugins requiring economy or implementing economy to depend on the plugin.
 * With this plugin loaded a Vault chat service is added for chat plugins on [ServicePriority.Normal].
 * If economy plugins wish to provide their own economy service, they should register on [ServicePriority.Highest] in
 * order to override bank implementation supplied in rpk-bank-lib
 */
class RPKEconomyLibBukkit : JavaPlugin(), RPKPlugin {

    override fun onEnable() {
        Metrics(this, 4391)
        if (server.pluginManager.getPlugin("Vault") != null) {
            server.servicesManager.register(Economy::class.java, RPKEconomyVaultEconomy(this), this, ServicePriority.Normal)
        }
    }

}