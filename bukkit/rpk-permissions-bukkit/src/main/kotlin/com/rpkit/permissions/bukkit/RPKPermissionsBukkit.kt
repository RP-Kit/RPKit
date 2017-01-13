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

package com.rpkit.permissions.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.permissions.bukkit.command.group.GroupCommand
import com.rpkit.permissions.bukkit.database.table.PlayerGroupTable
import com.rpkit.permissions.bukkit.group.RPKGroupImpl
import com.rpkit.permissions.bukkit.group.RPKGroupProviderImpl
import com.rpkit.permissions.bukkit.listener.PlayerJoinListener
import com.rpkit.permissions.bukkit.listener.PlayerQuitListener
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

/**
 * RPK permissions plugin default implementation.
 */
class RPKPermissionsBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        ConfigurationSerialization.registerClass(RPKGroupImpl::class.java, "RPKGroupImpl")
        saveDefaultConfig()
        config.options().pathSeparator('/')
        val groupProvider = RPKGroupProviderImpl(this)
        serviceProviders = arrayOf(groupProvider)
        groupProvider.groups.forEach { group ->
            server.pluginManager.addPermission(Permission(
                    "rpkit.permissions.command.group.add.${group.name}",
                    "Allows adding the ${group.name} group to players",
                    PermissionDefault.OP
            ))
            server.pluginManager.addPermission(Permission(
                    "rpkit.permissions.command.group.remove.${group.name}",
                    "Allows removing the ${group.name} group from players",
                    PermissionDefault.OP
            ))
        }
    }

    override fun registerCommands() {
        getCommand("group").executor = GroupCommand(this)
    }

    override fun registerListeners() {
        registerListeners(PlayerJoinListener(this), PlayerQuitListener(this))
    }

    override fun createTables(database: Database) {
        database.addTable(PlayerGroupTable(database, this))
    }
}