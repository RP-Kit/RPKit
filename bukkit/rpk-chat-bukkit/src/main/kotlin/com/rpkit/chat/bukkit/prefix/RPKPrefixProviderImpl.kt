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

package com.rpkit.chat.bukkit.prefix

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.event.prefix.RPKBukkitPrefixCreateEvent
import com.rpkit.chat.bukkit.event.prefix.RPKBukkitPrefixDeleteEvent
import com.rpkit.chat.bukkit.event.prefix.RPKBukkitPrefixUpdateEvent
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.ChatColor

/**
 * Prefix provider implementation.
 */
class RPKPrefixProviderImpl(private val plugin: RPKChatBukkit): RPKPrefixProvider {

    override val prefixes: List<RPKPrefix> = plugin.config.getConfigurationSection("prefixes")
            .getKeys(false)
            .mapIndexed { id, name ->
                    RPKPrefixImpl(id, name, ChatColor.translateAlternateColorCodes('&', plugin.config.getString("prefixes.$name")))
            }

    override fun addPrefix(prefix: RPKPrefix) {
        val event = RPKBukkitPrefixCreateEvent(prefix)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.config.set("prefixes.${event.prefix.name}", event.prefix.prefix)
        plugin.saveConfig()
    }

    override fun updatePrefix(prefix: RPKPrefix) {
        val event = RPKBukkitPrefixUpdateEvent(prefix)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.config.set("prefixes.${event.prefix.name}", event.prefix.prefix)
        plugin.saveConfig()
    }

    override fun removePrefix(prefix: RPKPrefix) {
        val event = RPKBukkitPrefixDeleteEvent(prefix)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.config.set("prefixes.${event.prefix.name}", null)
        plugin.saveConfig()
    }

    override fun getPrefix(name: String): RPKPrefix? {
        return prefixes.firstOrNull { prefix -> prefix.name == name }
    }

    override fun getPrefix(player: RPKPlayer): String {
        val prefixBuilder = StringBuilder()
        for (prefix in prefixes) {
            if (player.bukkitPlayer?.isOnline == true) {
                if (player.bukkitPlayer?.player?.hasPermission("rpkit.chat.prefix.${prefix.name}") == true) {
                    prefixBuilder.append(prefix.prefix).append(' ')
                }
            }
        }
        return prefixBuilder.toString()
    }

    override fun getPrefix(profile: RPKProfile): String {
        val groupProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
        val prefixBuilder = StringBuilder()
        prefixes
                .filter { groupProvider.hasPermission(profile, "rpkit.chat.prefix.${it.name}") }
                .forEach { prefixBuilder.append(it.prefix).append(' ') }
        return prefixBuilder.toString()
    }

}