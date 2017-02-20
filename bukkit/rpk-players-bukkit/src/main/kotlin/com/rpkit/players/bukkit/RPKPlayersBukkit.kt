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

package com.rpkit.players.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.service.ServiceProvider
import com.rpkit.core.web.NavigationLink
import com.rpkit.players.bukkit.command.account.AccountCommand
import com.rpkit.players.bukkit.database.table.RPKPlayerTable
import com.rpkit.players.bukkit.listener.PlayerJoinListener
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.players.bukkit.player.RPKPlayerProviderImpl
import com.rpkit.players.bukkit.servlet.PlayerServlet
import com.rpkit.players.bukkit.servlet.PlayersServlet
import com.rpkit.players.bukkit.servlet.api.v0_4.PlayerAPIServlet
import java.sql.SQLException

/**
 * RPK players plugin default implementation.
 */
class RPKPlayersBukkit: RPKBukkitPlugin() {

    private lateinit var playerProvider: RPKPlayerProvider

    override fun onEnable() {
        playerProvider = RPKPlayerProviderImpl(this)
        serviceProviders = arrayOf<ServiceProvider>(playerProvider)
        servlets = arrayOf(PlayersServlet(this), PlayerServlet(this), PlayerAPIServlet(this))
    }

    override fun onPostEnable() {
        core.web.navigationBar.add(NavigationLink("Players", "/players/"))
    }

    override fun registerCommands() {
        getCommand("account").executor = AccountCommand(this)
    }

    override fun registerListeners() {
        registerListeners(PlayerJoinListener(this))
    }

    @Throws(SQLException::class)
    override fun createTables(database: Database) {
        database.addTable(RPKPlayerTable(this, database))
    }

    override fun setDefaultMessages() {
        messages.setDefault("account-usage", "&cUsage: /account [link]")
        messages.setDefault("account-link-usage", "&cUsage: /account link [irc]")
        messages.setDefault("account-link-irc-usage", "&cUsage: /account link irc [nick]")
        messages.setDefault("account-link-irc-invalid-already-linked", "&cThat IRC user is already linked to a Minecraft user.")
        messages.setDefault("account-link-irc-invalid-nick", "&cThere is no IRC user by that name online.")
        messages.setDefault("account-link-irc-invalid-no-irc-provider", "&cThere is no IRC provider registered, so IRC accounts cannot be linked.")
        messages.setDefault("account-link-irc-invalid-no-player-provider", "&cThere is no player provider registered, so IRC accounts cannot be linked.")
        messages.setDefault("account-link-irc-valid", "&aAccount linked.")
    }

}
