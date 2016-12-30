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
import com.rpkit.players.bukkit.player.RPKPlayer
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
        plugin.config.set("prefixes.${prefix.name}", prefix.prefix)
        plugin.saveConfig()
    }

    override fun updatePrefix(prefix: RPKPrefix) {
        plugin.config.set("prefixes.${prefix.name}", prefix.prefix)
        plugin.saveConfig()
    }

    override fun removePrefix(prefix: RPKPrefix) {
        plugin.config.set("prefixes.${prefix.name}", null)
        plugin.saveConfig()
    }

    override fun getPrefix(name: String): RPKPrefix? {
        return prefixes.filter { prefix -> prefix.name == name }.firstOrNull()
    }

    override fun getPrefix(player: RPKPlayer): String {
        val prefixBuilder = StringBuilder()
        for (prefix in prefixes) {
            if (player.bukkitPlayer?.isOnline?:false) {
                if (player.bukkitPlayer?.player?.hasPermission("rpkit.chat.prefix.${prefix.name}")?:false) {
                    prefixBuilder.append(prefix.prefix).append(' ')
                }
            }
        }
        return prefixBuilder.toString()
    }

}