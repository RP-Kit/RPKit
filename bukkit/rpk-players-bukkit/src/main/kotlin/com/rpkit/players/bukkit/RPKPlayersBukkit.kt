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
import com.rpkit.players.bukkit.database.table.*
import com.rpkit.players.bukkit.listener.PlayerJoinListener
import com.rpkit.players.bukkit.listener.PlayerLoginListener
import com.rpkit.players.bukkit.player.RPKPlayerProviderImpl
import com.rpkit.players.bukkit.profile.RPKGitHubProfileProviderImpl
import com.rpkit.players.bukkit.profile.RPKIRCProfileProviderImpl
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProviderImpl
import com.rpkit.players.bukkit.profile.RPKProfileProviderImpl
import com.rpkit.players.bukkit.servlet.*
import org.passay.*
import org.passay.dictionary.WordListDictionary
import org.passay.dictionary.WordLists
import java.io.Reader
import java.sql.SQLException

/**
 * RPK players plugin default implementation.
 */
class RPKPlayersBukkit: RPKBukkitPlugin() {

    lateinit var passwordValidator: PasswordValidator

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
        passwordValidator = PasswordValidator(listOf(
                LengthRule(8, 36),
                CharacterRule(EnglishCharacterData.UpperCase, 2),
                CharacterRule(EnglishCharacterData.LowerCase, 2),
                CharacterRule(EnglishCharacterData.Digit, 2),
                CharacterRule(EnglishCharacterData.Special, 2),
                DictionarySubstringRule(
                        WordListDictionary(
                                WordLists.createFromReader(
                                        arrayOf<Reader>(
                                                getResource("words.txt").reader(Charsets.UTF_8)
                                        ),
                                        false
                                )
                        )
                ),
                IllegalSequenceRule(EnglishSequenceData.Alphabetical),
                IllegalSequenceRule(EnglishSequenceData.Numerical),
                IllegalSequenceRule(EnglishSequenceData.USQwerty),
                UsernameRule(true, true),
                RepeatCharacterRegexRule()
        ))
    }

    override fun onPostEnable() {
        core.web.navigationBar.add(NavigationLink("Players", "/players/"))
        core.web.navigationBar.add(NavigationLink("Profiles", "/profiles/"))
    }

    override fun registerCommands() {
        getCommand("account").executor = AccountCommand(this)
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
        messages.setDefault("account-link-usage", "&cUsage: /account link [irc]")
        messages.setDefault("account-link-irc-usage", "&cUsage: /account link irc [nick]")
        messages.setDefault("account-link-irc-invalid-already-linked", "&cThat IRC user is already linked to a Minecraft user.")
        messages.setDefault("account-link-irc-invalid-nick", "&cThere is no IRC user by that name online.")
        messages.setDefault("account-link-irc-invalid-no-irc-provider", "&cThere is no IRC provider registered, so IRC accounts cannot be linked.")
        messages.setDefault("account-link-irc-invalid-no-player-provider", "&cThere is no player provider registered, so IRC accounts cannot be linked.")
        messages.setDefault("account-link-irc-valid", "&aAccount linked.")
        messages.setDefault("kick-no-profile", "&cYour account is not linked to a profile.\n" +
                "Please visit the server's web UI and link your Minecraft account with the token:\n" +
                "\$token")
        messages.setDefault("no-profile", "&cYour Minecraft profile is not linked to a profile. Please link it on the server's web UI.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
    }

}
