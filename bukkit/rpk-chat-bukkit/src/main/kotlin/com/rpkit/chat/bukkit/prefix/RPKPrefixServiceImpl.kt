/*
 * Copyright 2020 Ren Binden
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
import com.rpkit.permissions.bukkit.group.hasPermission
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.ChatColor
import java.util.concurrent.CompletableFuture

/**
 * Prefix service implementation.
 */
class RPKPrefixServiceImpl(override val plugin: RPKChatBukkit) : RPKPrefixService {

    override val prefixes: List<RPKPrefix> = plugin.config.getConfigurationSection("prefixes")
            ?.getKeys(false)
            ?.map { name ->
                RPKPrefixImpl(name, ChatColor.translateAlternateColorCodes('&', plugin.config.getString("prefixes.$name")
                        ?: ""))
            }
            ?: emptyList()

    override fun addPrefix(prefix: RPKPrefix) {
        val event = RPKBukkitPrefixCreateEvent(prefix, false)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.config.set("prefixes.${event.prefix.name}", event.prefix.prefix)
        plugin.saveConfig()
    }

    override fun updatePrefix(prefix: RPKPrefix) {
        val event = RPKBukkitPrefixUpdateEvent(prefix, false)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.config.set("prefixes.${event.prefix.name}", event.prefix.prefix)
        plugin.saveConfig()
    }

    override fun removePrefix(prefix: RPKPrefix) {
        val event = RPKBukkitPrefixDeleteEvent(prefix, false)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.config.set("prefixes.${event.prefix.name}", null)
        plugin.saveConfig()
    }

    override fun getPrefix(name: String): RPKPrefix? {
        return prefixes.firstOrNull { prefix -> prefix.name == name }
    }

    override fun getPrefix(profile: RPKProfile): CompletableFuture<String> {
        return CompletableFuture.supplyAsync {
            val prefixBuilder = StringBuilder()
            prefixes
                .filter { profile.hasPermission("rpkit.chat.prefix.${it.name}").join() }
                .forEach { prefixBuilder.append(it.prefix).append(' ') }
            return@supplyAsync prefixBuilder.toString()
        }
    }

}