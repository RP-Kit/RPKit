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

package com.rpkit.permissions.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.permissions.bukkit.vault.RPKPermissionsVaultPermissions
import net.milkbowl.vault.permission.Permission
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.ServicePriority

/**
 * Class to allow permissions lib to load as a plugin.
 * This allows plugins requiring permissions or implementing permissions to depend on the plugin.
 */
class RPKPermissionsLibBukkit : RPKBukkitPlugin() {
    override fun onEnable() {
        Metrics(this, 4408)
        if (server.pluginManager.getPlugin("Vault") != null) {
            server.servicesManager.register(Permission::class.java, RPKPermissionsVaultPermissions(this), this, ServicePriority.Normal)
        }
    }
}