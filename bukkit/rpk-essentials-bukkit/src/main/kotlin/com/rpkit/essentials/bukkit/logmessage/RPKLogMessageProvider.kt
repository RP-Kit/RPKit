package com.rpkit.essentials.bukkit.logmessage

import com.rpkit.core.service.ServiceProvider
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.table.RPKLogMessagesEnabledTable
import com.rpkit.players.bukkit.player.RPKPlayer


class RPKLogMessageProvider(private val plugin: RPKEssentialsBukkit): ServiceProvider {

    fun isLogMessagesEnabled(player: RPKPlayer): Boolean {
        return plugin.core.database.getTable(RPKLogMessagesEnabledTable::class).get(player)?.enabled?:false
    }

    fun setLogMessagesEnabled(player: RPKPlayer, enabled: Boolean) {
        val logMessagesEnabledTable = plugin.core.database.getTable(RPKLogMessagesEnabledTable::class)
        var logMessagesEnabled = logMessagesEnabledTable.get(player)
        if (logMessagesEnabled != null) {
            logMessagesEnabled.enabled = enabled
            logMessagesEnabledTable.update(logMessagesEnabled)
        } else {
            logMessagesEnabled = RPKLogMessagesEnabled(player = player, enabled = enabled)
            logMessagesEnabledTable.insert(logMessagesEnabled)
        }
    }

}