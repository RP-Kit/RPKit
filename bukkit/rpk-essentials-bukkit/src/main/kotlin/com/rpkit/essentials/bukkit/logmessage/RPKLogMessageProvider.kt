package com.rpkit.essentials.bukkit.logmessage

import com.rpkit.core.service.ServiceProvider
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.table.RPKLogMessagesEnabledTable
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider


class RPKLogMessageProvider(private val plugin: RPKEssentialsBukkit): ServiceProvider {

    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("isLogMessagesEnabled(minecraftProfile)"))
    fun isLogMessagesEnabled(player: RPKPlayer): Boolean {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                return isLogMessagesEnabled(minecraftProfile)
            }
        }
        return false
    }

    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("setLogMessagesEnabled(minecraftProfile, enabled)"))
    fun setLogMessagesEnabled(player: RPKPlayer, enabled: Boolean) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                setLogMessagesEnabled(minecraftProfile, enabled)
            }
        }
    }

    fun isLogMessagesEnabled(minecraftProfile: RPKMinecraftProfile): Boolean {
        return plugin.core.database.getTable(RPKLogMessagesEnabledTable::class).get(minecraftProfile)?.enabled?:false
    }

    fun setLogMessagesEnabled(minecraftProfile: RPKMinecraftProfile, enabled: Boolean) {
        val logMessagesEnabledTable = plugin.core.database.getTable(RPKLogMessagesEnabledTable::class)
        var logMessagesEnabled = logMessagesEnabledTable.get(minecraftProfile)
        if (logMessagesEnabled != null) {
            logMessagesEnabled.enabled = enabled
            logMessagesEnabledTable.update(logMessagesEnabled)
        } else {
            logMessagesEnabled = RPKLogMessagesEnabled(minecraftProfile = minecraftProfile, enabled = enabled)
            logMessagesEnabledTable.insert(logMessagesEnabled)
        }
    }

}