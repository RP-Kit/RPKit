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
import org.bstats.bukkit.Metrics
import java.sql.SQLException

/**
 * RPK players plugin default implementation.
 */
class RPKPlayersBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this)
        saveDefaultConfig()
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
                com.rpkit.players.bukkit.servlet.api.v1.MinecraftProfileAPIServlet(this),
                com.rpkit.players.bukkit.servlet.api.v1.SignInServlet(this)
        )
    }

    override fun onPostEnable() {
        core.web.navigationBar.add(NavigationLink("Players", "/players/"))
        core.web.navigationBar.add(NavigationLink("Profiles", "/profiles/"))
    }

    override fun registerCommands() {
        getCommand("account")?.setExecutor(AccountCommand(this))
        getCommand("profile")?.setExecutor(ProfileCommand(this))
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
        database.addTable(RPKMinecraftProfileLinkRequestTable(database, this))
        database.addTable(RPKProfileTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("account-usage", "&cUsage: /account [link|confirmlink|denylink]")
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
        messages.setDefault("account-link-minecraft-valid", "&aAccount link request placed. Please log in to your other account and accept the request.")
        messages.setDefault("account-confirm-link-usage", "&cUsage: /account confirmlink [type] [id]")
        messages.setDefault("account-confirm-link-invalid-id", "&cInvalid ID.")
        messages.setDefault("account-confirm-link-invalid-already-linked", "&cYour Minecraft profile is already linked to a profile. You may not link to more than one profile.")
        messages.setDefault("account-confirm-link-invalid-request", "&cThat profile has not requested to link this Minecraft profile.")
        messages.setDefault("account-confirm-link-valid", "&aAccount linked.")
        messages.setDefault("account-confirm-link-invalid-type", "&cInvalid account type.")
        messages.setDefault("account-deny-link-usage", "&cUsage: /account denylink [type] [id]")
        messages.setDefault("account-deny-link-invalid-id", "&cInvalid ID.")
        messages.setDefault("account-deny-link-invalid-request", "&cThat profile has not requested to link this Minecraft profile.")
        messages.setDefault("account-deny-link-valid", "&aLink request denied.")
        messages.setDefault("account-deny-link-profile-created", "&aThere are no outstanding link requests for this account, so a new profile has been created for it.")
        messages.setDefault("account-deny-link-invalid-type", "&cInvalid account type.")
        messages.setDefault("profile-name-usage", "&cUsage: /profile name [name]")
        messages.setDefault("profile-name-invalid-name", "&cName must be between 3 and 16 characters and contain alphanumerics and underscores only.")
        messages.setDefault("profile-name-valid", "&aProfile name set to \$name.")
        messages.setDefault("profile-password-usage", "&cUsage: /profile password [password]")
        messages.setDefault("profile-password-valid", "&aProfile password set.")
        messages.setDefault("profile-usage", "&cUsage: /profile [name|password]")
        messages.setDefault("no-profile", "&cA profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("profile-link-request", "&fWould you like to link this Minecraft account to \$profile?")
        messages.setDefault("yes", "Yes")
        messages.setDefault("no", "No")
        messages.setDefault("no-permission-account-link", "&cYou do not have permission to link accounts.")
        messages.setDefault("no-permission-account-link-irc", "&cYou do not have permission to link IRC accounts.")
        messages.setDefault("no-permission-account-link-minecraft", "&cYou do not have permission to link Minecraft accounts.")
        messages.setDefault("no-permission-profile-create", "&cYou do not have permission to create profiles.")
        messages.setDefault("no-permission-profile-login", "&cYou do not have permission to login to profiles.")
    }

}
