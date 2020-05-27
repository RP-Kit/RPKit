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

package com.rpkit.chat.bukkit.vault

import com.rpkit.chat.bukkit.RPKChatLibBukkit
import com.rpkit.chat.bukkit.prefix.RPKPrefixProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.permission.Permission

/**
 * A Vault [Chat] implementation for chat plugins.
 */
class RPKChatVaultChat(private val plugin: RPKChatLibBukkit): Chat(plugin.server.servicesManager.getRegistration(Permission::class.java)?.provider) {

    override fun getGroupPrefix(world: String, group: String): String {
        val prefixProvider = plugin.core.serviceManager.getServiceProvider(RPKPrefixProvider::class)
        return prefixProvider.getPrefix(group)?.prefix?:""
    }

    override fun getName(): String {
        return "rpk-chat"
    }

    override fun getGroupInfoString(world: String, group: String, node: String, defaultValue: String): String {
        throw UnsupportedOperationException("rpk-chat does not support group info nodes")
    }

    override fun setPlayerInfoBoolean(world: String, playerName: String, node: String, value: Boolean) {
        throw UnsupportedOperationException("rpk-chat does not support player info nodes")
    }

    override fun getGroupInfoBoolean(world: String, group: String, node: String, defaultValue: Boolean): Boolean {
        throw UnsupportedOperationException("rpk-chat does not support group info nodes")
    }

    override fun setPlayerPrefix(world: String, playerName: String, prefix: String) {
        throw UnsupportedOperationException("rpk-chat does not support individual player prefixes")
    }

    override fun setPlayerSuffix(world: String, playerName: String, suffix: String) {
        throw UnsupportedOperationException("rpk-chat does not support suffixes")
    }

    override fun getPlayerPrefix(world: String, playerName: String): String {
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val prefixProvider = plugin.core.serviceManager.getServiceProvider(RPKPrefixProvider::class)
        val bukkitOfflinePlayer = plugin.server.getOfflinePlayer(playerName)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitOfflinePlayer)
        if (minecraftProfile != null) {
            val profile = minecraftProfile.profile
            if (profile is RPKProfile) {
                return prefixProvider.getPrefix(profile)
            }
        }
        return ""
    }

    override fun setGroupPrefix(world: String, group: String, prefix: String) {
        val prefixProvider = plugin.core.serviceManager.getServiceProvider(RPKPrefixProvider::class)
        prefixProvider.getPrefix(group)?.prefix = prefix
    }

    override fun setPlayerInfoDouble(world: String, playerName: String, node: String, value: Double) {
        throw UnsupportedOperationException("rpk-chat does not support player info nodes")
    }

    override fun setGroupInfoDouble(world: String, group: String, node: String, value: Double) {
        throw UnsupportedOperationException("rpk-chat does not support group info nodes")
    }

    override fun getPlayerSuffix(world: String, playerName: String): String {
        throw UnsupportedOperationException("rpk-chat does not support player info nodes")
    }

    override fun setGroupSuffix(world: String, group: String, suffix: String) {
        throw UnsupportedOperationException("rpk-chat does not support suffixes")
    }

    override fun getPlayerInfoInteger(world: String, playerName: String, node: String, defaultValue: Int): Int {
        throw UnsupportedOperationException("rpk-chat does not support player info nodes")
    }

    override fun setPlayerInfoInteger(world: String, playerName: String, node: String, value: Int) {
        throw UnsupportedOperationException("rpk-chat does not support player info nodes")
    }

    override fun getPlayerInfoDouble(world: String, playerName: String, node: String, defaultValue: Double): Double {
        throw UnsupportedOperationException("rpk-chat does not support player info nodes")
    }

    override fun getPlayerInfoString(world: String, playerName: String, node: String, defaultValue: String): String {
        throw UnsupportedOperationException("rpk-chat does not support player info nodes")
    }

    override fun getGroupInfoDouble(world: String, group: String, node: String, defaultValue: Double): Double {
        throw UnsupportedOperationException("rpk-chat does not support group info nodes")
    }

    override fun getPlayerInfoBoolean(world: String, playerName: String, node: String, defaultValue: Boolean): Boolean {
        throw UnsupportedOperationException("rpk-chat does not support player info nodes")
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun setGroupInfoBoolean(world: String, group: String, node: String, value: Boolean) {
        throw UnsupportedOperationException("rpk-chat does not support group info nodes")
    }

    override fun setGroupInfoInteger(world: String, group: String, node: String, value: Int) {
        throw UnsupportedOperationException("rpk-chat does not support group info nodes")
    }

    override fun getGroupSuffix(world: String, group: String): String {
        throw UnsupportedOperationException("rpk-chat does not support suffixes")
    }

    override fun setPlayerInfoString(world: String, playerName: String, node: String, value: String) {
        throw UnsupportedOperationException("rpk-chat does not support player info nodes")
    }

    override fun getGroupInfoInteger(world: String, group: String, node: String, defaultValue: Int): Int {
        throw UnsupportedOperationException("rpk-chat does not support group info nodes")
    }

    override fun setGroupInfoString(world: String, group: String, node: String, value: String) {
        throw UnsupportedOperationException("rpk-chat does not support group info nodes")
    }

}
