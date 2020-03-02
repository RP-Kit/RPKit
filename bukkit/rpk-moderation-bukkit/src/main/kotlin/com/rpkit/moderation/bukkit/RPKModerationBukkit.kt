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

package com.rpkit.moderation.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.moderation.bukkit.command.amivanished.AmIVanishedCommand
import com.rpkit.moderation.bukkit.command.onlinestaff.OnlineStaffCommand
import com.rpkit.moderation.bukkit.command.ticket.TicketCommand
import com.rpkit.moderation.bukkit.command.vanish.UnvanishCommand
import com.rpkit.moderation.bukkit.command.vanish.VanishCommand
import com.rpkit.moderation.bukkit.command.warn.WarningCommand
import com.rpkit.moderation.bukkit.command.warn.WarningCreateCommand
import com.rpkit.moderation.bukkit.command.warn.WarningRemoveCommand
import com.rpkit.moderation.bukkit.database.table.RPKTicketTable
import com.rpkit.moderation.bukkit.database.table.RPKVanishStateTable
import com.rpkit.moderation.bukkit.database.table.RPKWarningTable
import com.rpkit.moderation.bukkit.listener.PlayerJoinListener
import com.rpkit.moderation.bukkit.ticket.RPKTicketProviderImpl
import com.rpkit.moderation.bukkit.vanish.RPKVanishProviderImpl
import com.rpkit.moderation.bukkit.warning.RPKWarningProviderImpl
import org.bstats.bukkit.Metrics


class RPKModerationBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this, 4403)
        serviceProviders = arrayOf(
                RPKTicketProviderImpl(this),
                RPKVanishProviderImpl(this),
                RPKWarningProviderImpl(this)
        )
    }

    override fun registerCommands() {
        getCommand("amivanished")?.setExecutor(AmIVanishedCommand(this))
        getCommand("onlinestaff")?.setExecutor(OnlineStaffCommand(this))
        getCommand("ticket")?.setExecutor(TicketCommand(this))
        getCommand("warning")?.setExecutor(WarningCommand(this))
        getCommand("warn")?.setExecutor(WarningCreateCommand(this))
        getCommand("unwarn")?.setExecutor(WarningRemoveCommand(this))
        getCommand("vanish")?.setExecutor(VanishCommand(this))
        getCommand("unvanish")?.setExecutor(UnvanishCommand(this))
    }

    override fun registerListeners() {
        registerListeners(
                PlayerJoinListener(this)
        )
    }

    override fun setDefaultMessages() {
        messages.setDefault("not-from-console", "&cYou must be a player to perform that command.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-profile", "&cYour Minecraft profile is currently not linked to a profile. Please link it.")
        messages.setDefault("amivanished-vanished", "&fYou are currently vanished.")
        messages.setDefault("amivanished-unvanished", "&fYou are currently &cnot &avanished.")
        messages.setDefault("online-staff-title", "&fOnline staff: ")
        messages.setDefault("online-staff-item", "&7 - &f\$name")
        messages.setDefault("ticket-usage", "&cUsage: /ticket [close|create|list|teleport]")
        messages.setDefault("ticket-close-usage", "&cUsage: /ticket close [id]")
        messages.setDefault("ticket-close-valid", "&aTicket closed.")
        messages.setDefault("ticket-close-invalid-id", "&cPlease provide a valid ticket ID.")
        messages.setDefault("ticket-close-invalid-ticket", "&cThere is no ticket with that ID.")
        messages.setDefault("ticket-create-usage", "&cUsage: /ticket create [reason]")
        messages.setDefault("ticket-create-valid", "&aTicket created: #\$id - \$reason")
        messages.setDefault("ticket-list-usage", "&cUsage: /ticket list [open|closed]")
        messages.setDefault("ticket-list-title", "&fTickets:")
        messages.setDefault("ticket-list-item", "&7#\$id - &f\$reason (\$issuer, \$open-date)")
        messages.setDefault("ticket-teleport-usage", "&cUsage: /ticket teleport [id]")
        messages.setDefault("ticket-teleport-invalid-ticket", "&cThere is no ticket with that ID.")
        messages.setDefault("ticket-teleport-invalid-location", "&cThat ticket has no location.")
        messages.setDefault("ticket-teleport-valid", "&aTeleported to ticket.")
        messages.setDefault("vanish-invisible", "&aYou are currently invisible.")
        messages.setDefault("unvanish-valid", "&aUnvanished.")
        messages.setDefault("vanish-valid", "&aVanished.")
        messages.setDefault("warning-usage", "&cUsage: /warning [create|delete|list]")
        messages.setDefault("warning-create-usage", "&cUsage: /warning create [player] [reason]")
        messages.setDefault("warning-create-invalid-target", "&cThere is no player by that name online.")
        messages.setDefault("warning-create-valid", "&aWarning issued to \$profile for \$reason")
        messages.setDefault("warning-received", "&cYou have received a warning for \$reason at \$time. You have \$index warnings.")
        messages.setDefault("warning-list-title", "&fWarnings:")
        messages.setDefault("warning-list-item", "&7\$index &f\$reason &7\$time")
        messages.setDefault("warning-remove-usage", "&cUsage: /warning remove [player] [index]")
        messages.setDefault("warning-remove-invalid-target", "&cThere is no player by that name online.")
        messages.setDefault("warning-remove-invalid-index", "&cYou must specify a valid warning index.")
        messages.setDefault("warning-remove-valid", "&aWarning removed.")
        messages.setDefault("no-permission-amivanished", "&cYou do not have permission to check if you are vanished.")
        messages.setDefault("no-permission-ticket-close", "&cYou do not have permission to close tickets.")
        messages.setDefault("no-permission-ticket-create", "&cYou do not have permission to create tickets.")
        messages.setDefault("no-permission-ticket-list", "&cYou do not have permission to list tickets.")
        messages.setDefault("no-permission-ticket-list-closed", "&cYou do not have permission to list closed tickets.")
        messages.setDefault("no-permission-ticket-list-open", "&cYou do not have permission to list open tickets.")
        messages.setDefault("no-permission-ticket-teleport", "&cYou do not have permission to teleport to tickets.")
        messages.setDefault("no-permission-unvanish", "&cYou do not have permission to unvanish.")
        messages.setDefault("no-permission-vanish", "&cYou do not have permission to vanish.")
        messages.setDefault("no-permission-warning", "&cYou do not have permission to use warnings.")
        messages.setDefault("no-permission-warning-create", "&cYou do not have permission to issue warnings.")
        messages.setDefault("no-permission-warning-list", "&cYou do not have permission to list your warnings.")
        messages.setDefault("no-permission-warning-remove", "&cYou do not have permission to remove warnings.")
    }

    override fun createTables(database: Database) {
        database.addTable(RPKTicketTable(database, this))
        database.addTable(RPKVanishStateTable(database, this))
        database.addTable(RPKWarningTable(database, this))
    }

}