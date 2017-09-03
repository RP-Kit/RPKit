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
import com.rpkit.core.web.NavigationLink
import com.rpkit.players.bukkit.command.account.AccountCommand
import com.rpkit.players.bukkit.command.profile.ProfileCommand
import com.rpkit.players.bukkit.database.table.*
import com.rpkit.players.bukkit.listener.PlayerJoinListener
import com.rpkit.players.bukkit.listener.PlayerLoginListener
import com.rpkit.players.bukkit.player.RPKPlayerProviderImpl
import com.rpkit.players.bukkit.profile.RPKGitHubProfileProviderImpl
import com.rpkit.players.bukkit.profile.RPKIRCProfileProviderImpl
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProviderImpl
import com.rpkit.players.bukkit.profile.RPKProfileProviderImpl
import com.rpkit.players.bukkit.servlet.*
import java.sql.SQLException

/**
 * RPK players plugin default implementation.
 */
class RPKPlayersBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        serviceProviders = arrayOf(
                RPKPlayerProviderImpl(this),
                RPKGitHubProfileProviderImpl(this),
                RPKIRCProfileProviderImpl(this),
                RPKMinecraftProfileProviderImpl(this),
                RPKProfileProviderImpl(this)
        )
        servlets = arrayOf(
                PlayersServlet(this),
                PlayerServlet(this),
                ProfilesServlet(this),
                ProfileServlet(this),
                ProfileSignInServlet(this),
                ProfileSignOutServlet(this),
                ProfileSignUpServlet(this),
                // API v0.4
                com.rpkit.players.bukkit.servlet.api.v0_4.PlayerAPIServlet(this),
                // API v1
                com.rpkit.players.bukkit.servlet.api.v1.PlayerAPIServlet(this),
                com.rpkit.players.bukkit.servlet.api.v1.ProfileAPIServlet(this),
                com.rpkit.players.bukkit.servlet.api.v1.GitHubProfileAPIServlet(this),
                com.rpkit.players.bukkit.servlet.api.v1.IRCProfileAPIServlet(this),
                com.rpkit.players.bukkit.servlet.api.v1.MinecraftProfileAPIServlet(this)
        )
    }

    override fun onPostEnable() {
        core.web.navigationBar.add(NavigationLink("Players", "/players/"))
        core.web.navigationBar.add(NavigationLink("Profiles", "/profiles/"))
    }

    override fun registerCommands() {
        getCommand("account").executor = AccountCommand(this)
        getCommand("profile").executor = ProfileCommand(this)
    }

    override fun registerListeners() {
        registerListeners(PlayerJoinListener(this), PlayerLoginListener(this))
    }

    @Throws(SQLException::class)
    override fun createTables(database: Database) {
        database.addTable(RPKPlayerTable(this, database))
        database.addTable(RPKGitHubProfileTable(database, this))
        database.addTable(RPKIRCProfileTable(database, this))
        database.addTable(RPKMinecraftProfileTable(database, this))
        database.addTable(RPKMinecraftProfileTokenTable(database, this))
        database.addTable(RPKProfileTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("account-usage", "&cUsage: /account [link]")
        messages.setDefault("account-link-usage", "&cUsage: /account link [irc|minecraft]")
        messages.setDefault("account-link-irc-usage", "&cUsage: /account link irc [nick]")
        messages.setDefault("account-link-irc-invalid-already-linked", "&cThat IRC user is already linked to a Minecraft user.")
        messages.setDefault("account-link-irc-invalid-nick", "&cThere is no IRC user by that name online.")
        messages.setDefault("account-link-irc-invalid-no-irc-provider", "&cThere is no IRC provider registered, so IRC accounts cannot be linked.")
        messages.setDefault("account-link-irc-invalid-no-player-provider", "&cThere is no player provider registered, so IRC accounts cannot be linked.")
        messages.setDefault("account-link-irc-valid", "&aAccount linked.")
        messages.setDefault("account-link-minecraft-usage", "&cUsage: /account link minecraft [name] [token]")
        messages.setDefault("account-link-minecraft-invalid-player", "&cThere is no player by that name.")
        messages.setDefault("account-link-minecraft-invalid-minecraft-profile", "&cThat account has no Minecraft profile. Please get them to relog, or contact the server owner if this error persists.")
        messages.setDefault("account-link-minecraft-invalid-token", "&cThat token is invalid. Please use the token exactly as provided on your other account.")
        messages.setDefault("account-link-minecraft-valid", "&aAccount linked.")
        messages.setDefault("profile-usage", "&cUsage: /profile [create|login]")
        messages.setDefault("profile-create-invalid-profile", "&cYou already have a profile.")
        messages.setDefault("profile-create-usage", "&cUsage: /profile create [name] [password]")
        messages.setDefault("profile-create-valid", "&aProfile created. Please keep your password somewhere safe.")
        messages.setDefault("profile-login-invalid-profile-already-set", "&cThis account has already been logged in to a profile.")
        messages.setDefault("profile-login-usage", "&cUsage: /profile login [name] [password]")
        messages.setDefault("profile-login-invalid-profile", "&cInvalid username or password.")
        messages.setDefault("profile-login-invalid-password", "&cInvalid username or password.")
        messages.setDefault("profile-login-valid", "&aLogged in.")
        messages.setDefault("kick-no-profile", "&cYour account is not linked to a profile.\n" +
                "Please visit the server's web UI and link your Minecraft account with the token:\n" +
                "\$token")
        messages.setDefault("no-profile", "&cYour Minecraft profile is not linked to a profile. Please link it on the server's web UI.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("profile-link-info", "&cYour Minecraft profile is not linked to a profile. This will cause functionality to be limited. If you have previously logged in from a different Minecraft account, please use '/profile login [name] [password]'. Otherwise, create a profile with '/profile create [name] [password]'.")
        messages.setDefault("no-permission-account-link", "&cYou do not have permission to link accounts.")
        messages.setDefault("no-permission-account-link-irc", "&cYou do not have permission to link IRC accounts.")
        messages.setDefault("no-permission-account-link-minecraft", "&cYou do not have permission to link Minecraft accounts.")
        messages.setDefault("no-permission-profile-create", "&cYou do not have permission to create profiles.")
        messages.setDefault("no-permission-profile-login", "&cYou do not have permission to login to profiles.")
    }

}
